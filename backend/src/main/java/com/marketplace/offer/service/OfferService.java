package com.marketplace.offer.service;

import com.marketplace.booking.dto.BookingResponse;
import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.mapper.BookingMapper;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ConflictException;
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
import com.marketplace.matching.entity.Match;
import com.marketplace.matching.entity.MatchStatus;
import com.marketplace.matching.repository.MatchRepository;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.offer.dto.AdminOfferResponse;
import com.marketplace.offer.dto.CreateOfferRequest;
import com.marketplace.offer.dto.OfferFilter;
import com.marketplace.offer.dto.OfferResponse;
import com.marketplace.offer.entity.Offer;
import com.marketplace.offer.entity.OfferStatus;
import com.marketplace.offer.mapper.OfferMapper;
import com.marketplace.offer.repository.OfferRepository;
import com.marketplace.offer.repository.OfferSpecification;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfferService {

    private static final String DEFAULT_CURRENCY = "PKR";
    private static final Set<BuyerRequestStatus> ELIGIBLE_BUYER_REQUEST_STATUSES = EnumSet.of(
            BuyerRequestStatus.PUBLISHED, BuyerRequestStatus.MATCHED);
    private static final Set<TravellerTripStatus> ELIGIBLE_TRIP_STATUSES = EnumSet.of(
            TravellerTripStatus.PUBLISHED, TravellerTripStatus.MATCHED);

    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final BuyerRequestRepository buyerRequestRepository;
    private final TravellerTripRepository travellerTripRepository;
    private final MatchRepository matchRepository;
    private final KycService kycService;
    private final OfferMapper offerMapper;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    @Transactional
    public OfferResponse create(CreateOfferRequest request, UUID travellerId) {
        kycService.requireApprovedKyc(travellerId);

        BuyerRequest buyerRequest = buyerRequestRepository.findById(request.getBuyerRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer request not found"));
        TravellerTrip travellerTrip = travellerTripRepository.findById(request.getTravellerTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Traveller trip not found"));

        assertTravellerOwnsTrip(travellerTrip, travellerId);
        assertEligibleBuyerRequest(buyerRequest);
        assertEligibleTravellerTrip(travellerTrip);
        assertDifferentUsers(buyerRequest, travellerId);

        if (offerRepository.existsByBuyerRequest_IdAndTravellerTrip_IdAndTraveller_IdAndStatus(
                buyerRequest.getId(), travellerTrip.getId(), travellerId, OfferStatus.SENT)) {
            throw new ConflictException("An active offer already exists for this buyer request and trip");
        }

        Match match = resolveMatch(request.getMatchId(), buyerRequest, travellerTrip, travellerId);

        String currency = request.getCurrency() != null && !request.getCurrency().isBlank()
                ? request.getCurrency().trim().toUpperCase()
                : DEFAULT_CURRENCY;
        BigDecimal totalAmount = request.getItemPrice()
                .add(request.getTravellerFee())
                .add(request.getPlatformFee());

        Offer offer = Offer.builder()
                .id(UUID.randomUUID())
                .buyerRequest(buyerRequest)
                .travellerTrip(travellerTrip)
                .match(match)
                .traveller(travellerTrip.getTraveller())
                .buyer(buyerRequest.getBuyer())
                .itemPrice(request.getItemPrice())
                .travellerFee(request.getTravellerFee())
                .platformFee(request.getPlatformFee())
                .totalAmount(totalAmount)
                .currency(currency)
                .message(request.getMessage())
                .status(OfferStatus.SENT)
                .expiresAt(request.getExpiresAt())
                .build();

        offer = offerRepository.save(offer);

        if (match != null && match.getStatus() != MatchStatus.OFFER_SENT) {
            match.setStatus(MatchStatus.OFFER_SENT);
            matchRepository.save(match);
        }

        if (buyerRequest.getStatus() == BuyerRequestStatus.PUBLISHED) {
            buyerRequest.setStatus(BuyerRequestStatus.MATCHED);
            buyerRequestRepository.save(buyerRequest);
        }

        notificationService.notifyOfferSent(buyerRequest.getBuyer().getId(), offer.getId());
        return offerMapper.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public OfferResponse getById(UUID offerId, UserPrincipal principal) {
        Offer offer = getOffer(offerId);
        assertCanView(offer, principal);
        return offerMapper.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> findMyOffers(UUID userId, OfferStatus status, Pageable pageable) {
        OfferFilter filter = OfferFilter.builder().status(status).build();
        Specification<Offer> spec = OfferSpecification.forParticipant(userId, filter);
        return offerRepository.findAll(spec, pageable).map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> findByBuyerRequest(UUID buyerRequestId, UUID buyerId, OfferStatus status, Pageable pageable) {
        BuyerRequest buyerRequest = buyerRequestRepository.findById(buyerRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer request not found"));
        if (!buyerRequest.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("You can only view offers for your own buyer requests");
        }
        OfferFilter filter = OfferFilter.builder().buyerRequestId(buyerRequestId).status(status).build();
        return offerRepository.findAll(OfferSpecification.withFilter(filter), pageable).map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> findByTravellerTrip(UUID travellerTripId, UUID travellerId, OfferStatus status, Pageable pageable) {
        TravellerTrip trip = travellerTripRepository.findById(travellerTripId)
                .orElseThrow(() -> new ResourceNotFoundException("Traveller trip not found"));
        if (!trip.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("You can only view offers for your own traveller trips");
        }
        OfferFilter filter = OfferFilter.builder().travellerTripId(travellerTripId).status(status).build();
        return offerRepository.findAll(OfferSpecification.withFilter(filter), pageable).map(offerMapper::toResponse);
    }

    @Transactional
    public BookingResponse accept(UUID offerId, UUID buyerId) {
        Offer offer = getOffer(offerId);
        assertBuyerOwnsRequest(offer, buyerId);
        assertOfferSent(offer);
        assertNotExpired(offer);

        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .offer(offer)
                .buyer(offer.getBuyer())
                .traveller(offer.getTraveller())
                .buyerRequest(offer.getBuyerRequest())
                .travellerTrip(offer.getTravellerTrip())
                .status(BookingStatus.PAYMENT_PENDING)
                .acceptedAt(Instant.now())
                .build();
        booking = bookingRepository.save(booking);

        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        BuyerRequest buyerRequest = offer.getBuyerRequest();
        buyerRequest.setStatus(BuyerRequestStatus.BOOKED);
        buyerRequestRepository.save(buyerRequest);

        TravellerTrip travellerTrip = offer.getTravellerTrip();
        travellerTrip.setStatus(TravellerTripStatus.MATCHED);
        travellerTripRepository.save(travellerTrip);

        rejectOtherSentOffers(buyerRequest.getId(), offer.getId());

        notificationService.notifyOfferAccepted(offer.getTraveller().getId(), offer.getId());
        notificationService.notifyBookingCreated(offer.getBuyer().getId(), offer.getTraveller().getId(), booking.getId());

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public OfferResponse reject(UUID offerId, UUID buyerId) {
        Offer offer = getOffer(offerId);
        assertBuyerOwnsRequest(offer, buyerId);
        assertOfferSent(offer);
        offer.setStatus(OfferStatus.REJECTED);
        offer = offerRepository.save(offer);
        notificationService.notifyOfferRejected(offer.getTraveller().getId(), offer.getId());
        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse cancel(UUID offerId, UUID travellerId) {
        Offer offer = getOffer(offerId);
        if (!offer.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("You can only cancel offers you created");
        }
        assertOfferSent(offer);
        offer.setStatus(OfferStatus.CANCELLED);
        offer = offerRepository.save(offer);
        notificationService.notifyOfferCancelled(offer.getBuyer().getId(), offer.getId());
        return offerMapper.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public Page<AdminOfferResponse> adminSearch(OfferFilter filter, Pageable pageable) {
        return offerRepository.findAll(OfferSpecification.withFilter(filter), pageable)
                .map(this::toAdminResponse);
    }

    private void rejectOtherSentOffers(UUID buyerRequestId, UUID acceptedOfferId) {
        List<Offer> sentOffers = offerRepository.findByBuyerRequest_IdAndStatus(buyerRequestId, OfferStatus.SENT);
        for (Offer other : sentOffers) {
            if (!other.getId().equals(acceptedOfferId)) {
                other.setStatus(OfferStatus.REJECTED);
                offerRepository.save(other);
            }
        }
    }

    private Match resolveMatch(UUID matchId, BuyerRequest buyerRequest, TravellerTrip travellerTrip, UUID travellerId) {
        if (matchId == null) {
            return null;
        }
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        if (!match.getBuyerRequest().getId().equals(buyerRequest.getId())
                || !match.getTravellerTrip().getId().equals(travellerTrip.getId())) {
            throw new BadRequestException("Match does not belong to the provided buyer request and traveller trip");
        }
        if (!match.getTravellerTrip().getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("Match does not belong to your traveller trip");
        }
        return match;
    }

    private Offer getOffer(UUID offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    }

    private void assertTravellerOwnsTrip(TravellerTrip trip, UUID travellerId) {
        if (!trip.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("You can only send offers from your own traveller trips");
        }
    }

    private void assertEligibleBuyerRequest(BuyerRequest buyerRequest) {
        if (!ELIGIBLE_BUYER_REQUEST_STATUSES.contains(buyerRequest.getStatus())) {
            throw new BadRequestException("Buyer request must be PUBLISHED or MATCHED to receive offers");
        }
    }

    private void assertEligibleTravellerTrip(TravellerTrip trip) {
        if (!ELIGIBLE_TRIP_STATUSES.contains(trip.getStatus())) {
            throw new BadRequestException("Traveller trip must be PUBLISHED or MATCHED to send offers");
        }
    }

    private void assertDifferentUsers(BuyerRequest buyerRequest, UUID travellerId) {
        User buyer = buyerRequest.getBuyer();
        if (buyer.getId().equals(travellerId)) {
            throw new BadRequestException("Buyer and traveller cannot be the same user");
        }
    }

    private void assertBuyerOwnsRequest(Offer offer, UUID buyerId) {
        if (!offer.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("Only the buyer can perform this action on the offer");
        }
    }

    private void assertCanView(Offer offer, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!offer.getBuyer().getId().equals(userId) && !offer.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to this offer");
        }
    }

    private void assertOfferSent(Offer offer) {
        if (offer.getStatus() != OfferStatus.SENT) {
            throw new InvalidStatusException("Offer must be in SENT status");
        }
    }

    private void assertNotExpired(Offer offer) {
        if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(Instant.now())) {
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.save(offer);
            throw new InvalidStatusException("Offer has expired and cannot be accepted");
        }
    }

    private AdminOfferResponse toAdminResponse(Offer offer) {
        UUID matchId = offer.getMatch() != null ? offer.getMatch().getId() : null;
        return AdminOfferResponse.builder()
                .id(offer.getId())
                .buyerRequestId(offer.getBuyerRequest().getId())
                .buyerRequestTitle(offer.getBuyerRequest().getTitle())
                .travellerTripId(offer.getTravellerTrip().getId())
                .matchId(matchId)
                .buyerId(offer.getBuyer().getId())
                .buyerName(offer.getBuyer().getFullName())
                .buyerEmail(offer.getBuyer().getEmail())
                .travellerId(offer.getTraveller().getId())
                .travellerName(offer.getTraveller().getFullName())
                .travellerEmail(offer.getTraveller().getEmail())
                .itemPrice(offer.getItemPrice())
                .travellerFee(offer.getTravellerFee())
                .platformFee(offer.getPlatformFee())
                .totalAmount(offer.getTotalAmount())
                .currency(offer.getCurrency())
                .message(offer.getMessage())
                .status(offer.getStatus())
                .expiresAt(offer.getExpiresAt())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}