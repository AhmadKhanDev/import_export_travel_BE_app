package com.marketplace.matching.service;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.kyc.service.KycService;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.listing.repository.TravellerTripRepository;
import com.marketplace.matching.dto.AdminMatchResponse;
import com.marketplace.matching.dto.MatchFilter;
import com.marketplace.matching.dto.MatchResponse;
import com.marketplace.matching.entity.Match;
import com.marketplace.matching.entity.MatchStatus;
import com.marketplace.matching.mapper.MatchMapper;
import com.marketplace.matching.repository.MatchRepository;
import com.marketplace.matching.repository.MatchSpecification;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MatchRepository matchRepository;
    private final BuyerRequestRepository buyerRequestRepository;
    private final TravellerTripRepository travellerTripRepository;
    private final MatchScoringService matchScoringService;
    private final MatchMapper matchMapper;
    private final KycService kycService;

    @Transactional
    public List<MatchResponse> generateByBuyerRequest(UUID buyerRequestId, UUID buyerId) {
        BuyerRequest buyerRequest = getBuyerRequestForOwner(buyerRequestId, buyerId);
        assertPublishedBuyerRequest(buyerRequest);

        List<TravellerTrip> candidates = travellerTripRepository
                .findByStatusAndSourceCountryIgnoreCaseAndDestinationCountryIgnoreCase(
                        TravellerTripStatus.PUBLISHED,
                        buyerRequest.getSourceCountry(),
                        buyerRequest.getDestinationCountry());

        List<Match> matches = new ArrayList<>();
        for (TravellerTrip trip : candidates) {
            processCandidate(buyerRequest, trip, matches);
        }

        return matches.stream()
                .sorted(Comparator.comparing(Match::getMatchScore).reversed())
                .map(matchMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<MatchResponse> generateByTravellerTrip(UUID travellerTripId, UUID travellerId) {
        kycService.requireApprovedKyc(travellerId);
        TravellerTrip travellerTrip = getTravellerTripForOwner(travellerTripId, travellerId);
        assertPublishedTravellerTrip(travellerTrip);

        List<BuyerRequest> candidates = buyerRequestRepository
                .findByStatusAndSourceCountryIgnoreCaseAndDestinationCountryIgnoreCase(
                        BuyerRequestStatus.PUBLISHED,
                        travellerTrip.getSourceCountry(),
                        travellerTrip.getDestinationCountry());

        List<Match> matches = new ArrayList<>();
        for (BuyerRequest request : candidates) {
            processCandidate(request, travellerTrip, matches);
        }

        return matches.stream()
                .sorted(Comparator.comparing(Match::getMatchScore).reversed())
                .map(matchMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MatchResponse> findByBuyerRequest(
            UUID buyerRequestId, UUID buyerId, MatchStatus status, Pageable pageable) {
        getBuyerRequestForOwner(buyerRequestId, buyerId);
        boolean excludeRejected = status == null;
        Specification<Match> spec = MatchSpecification.byBuyerRequest(buyerRequestId, status, excludeRejected);
        return matchRepository.findAll(spec, pageable).map(matchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MatchResponse> findByTravellerTrip(
            UUID travellerTripId, UUID travellerId, MatchStatus status, Pageable pageable) {
        getTravellerTripForOwner(travellerTripId, travellerId);
        boolean excludeRejected = status == null;
        Specification<Match> spec = MatchSpecification.byTravellerTrip(travellerTripId, status, excludeRejected);
        return matchRepository.findAll(spec, pageable).map(matchMapper::toResponse);
    }

    @Transactional
    public MatchResponse getById(UUID matchId, UserPrincipal principal) {
        Match match = getMatch(matchId);
        assertCanView(match, principal);

        if (principal.getRole() != Role.ADMIN && match.getStatus() == MatchStatus.SUGGESTED) {
            match.setStatus(MatchStatus.VIEWED);
            match = matchRepository.save(match);
        }

        return matchMapper.toResponse(match);
    }

    @Transactional
    public MatchResponse reject(UUID matchId, UserPrincipal principal) {
        Match match = getMatch(matchId);
        assertCanReject(match, principal);

        if (match.getStatus() == MatchStatus.REJECTED) {
            throw new InvalidStatusException("Match is already rejected");
        }
        if (match.getStatus() == MatchStatus.OFFER_SENT) {
            throw new InvalidStatusException("Cannot reject a match after an offer has been sent");
        }

        match.setStatus(MatchStatus.REJECTED);
        return matchMapper.toResponse(matchRepository.save(match));
    }

    @Transactional(readOnly = true)
    public Page<AdminMatchResponse> adminSearch(MatchFilter filter, Pageable pageable) {
        Specification<Match> spec = MatchSpecification.withFilters(
                filter.getBuyerRequestId(),
                filter.getTravellerTripId(),
                filter.getStatus(),
                false);
        return matchRepository.findAll(spec, pageable).map(matchMapper::toAdminResponse);
    }

    private void processCandidate(BuyerRequest buyerRequest, TravellerTrip travellerTrip, List<Match> results) {
        Optional<Match> existing = matchRepository.findByBuyerRequest_IdAndTravellerTrip_Id(
                buyerRequest.getId(), travellerTrip.getId());

        if (existing.isPresent()) {
            Match match = existing.get();
            if (match.getStatus() != MatchStatus.REJECTED) {
                results.add(match);
            }
            return;
        }

        if (!matchScoringService.isEligible(buyerRequest, travellerTrip)) {
            return;
        }

        BigDecimal score = matchScoringService.calculateScore(buyerRequest, travellerTrip);
        Match match = Match.builder()
                .id(UUID.randomUUID())
                .buyerRequest(buyerRequest)
                .travellerTrip(travellerTrip)
                .matchScore(score)
                .status(MatchStatus.SUGGESTED)
                .build();
        results.add(matchRepository.save(match));
    }

    private BuyerRequest getBuyerRequestForOwner(UUID buyerRequestId, UUID buyerId) {
        BuyerRequest buyerRequest = buyerRequestRepository.findById(buyerRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer request not found"));
        if (!buyerRequest.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("You can only manage matches for your own buyer requests");
        }
        return buyerRequest;
    }

    private TravellerTrip getTravellerTripForOwner(UUID travellerTripId, UUID travellerId) {
        TravellerTrip travellerTrip = travellerTripRepository.findById(travellerTripId)
                .orElseThrow(() -> new ResourceNotFoundException("Traveller trip not found"));
        if (!travellerTrip.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("You can only manage matches for your own traveller trips");
        }
        return travellerTrip;
    }

    private Match getMatch(UUID matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
    }

    private void assertPublishedBuyerRequest(BuyerRequest buyerRequest) {
        if (buyerRequest.getStatus() != BuyerRequestStatus.PUBLISHED) {
            throw new BadRequestException("Buyer request must be PUBLISHED before generating matches");
        }
    }

    private void assertPublishedTravellerTrip(TravellerTrip travellerTrip) {
        if (travellerTrip.getStatus() != TravellerTripStatus.PUBLISHED) {
            throw new BadRequestException("Traveller trip must be PUBLISHED before generating matches");
        }
    }

    private void assertCanView(Match match, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        boolean isBuyer = match.getBuyerRequest().getBuyer().getId().equals(userId);
        boolean isTraveller = match.getTravellerTrip().getTraveller().getId().equals(userId);
        if (!isBuyer && !isTraveller) {
            throw new OwnershipException("You do not have access to this match");
        }
    }

    private void assertCanReject(Match match, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        boolean isBuyer = match.getBuyerRequest().getBuyer().getId().equals(userId);
        boolean isTraveller = match.getTravellerTrip().getTraveller().getId().equals(userId);
        if (!isBuyer && !isTraveller) {
            throw new OwnershipException("You can only reject matches you are part of");
        }
    }
}