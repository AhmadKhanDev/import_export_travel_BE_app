package com.marketplace.listing.service;

import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.kyc.service.KycService;
import com.marketplace.listing.dto.CreateTravellerTripRequest;
import com.marketplace.listing.dto.TravellerTripFilter;
import com.marketplace.listing.dto.TravellerTripResponse;
import com.marketplace.listing.dto.UpdateTravellerTripRequest;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.listing.mapper.ListingMapper;
import com.marketplace.listing.repository.TravellerTripRepository;
import com.marketplace.listing.repository.TravellerTripSpecification;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravellerTripService {

    private static final Set<TravellerTripStatus> NON_UPDATABLE_STATUSES = EnumSet.of(
            TravellerTripStatus.MATCHED,
            TravellerTripStatus.CLOSED,
            TravellerTripStatus.CANCELLED
    );

    private final TravellerTripRepository travellerTripRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;
    private final KycService kycService;

    @Transactional
    public TravellerTripResponse create(CreateTravellerTripRequest request, UUID travellerId) {
        User traveller = getUser(travellerId);
        TravellerTrip entity = listingMapper.toTravellerTrip(request, traveller);
        return listingMapper.toTravellerTripResponse(travellerTripRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<TravellerTripResponse> search(TravellerTripFilter filter, UserPrincipal viewer, Pageable pageable) {
        Specification<TravellerTrip> spec = TravellerTripSpecification.withFilters(filter, null, viewer.getRole());
        return travellerTripRepository.findAll(spec, pageable)
                .map(listingMapper::toTravellerTripResponse);
    }

    @Transactional(readOnly = true)
    public Page<TravellerTripResponse> findMyTrips(UUID travellerId, TravellerTripFilter filter, Pageable pageable) {
        Specification<TravellerTrip> spec = TravellerTripSpecification.withFilters(filter, travellerId, Role.TRAVELLER);
        return travellerTripRepository.findAll(spec, pageable)
                .map(listingMapper::toTravellerTripResponse);
    }

    @Transactional(readOnly = true)
    public TravellerTripResponse getById(UUID id) {
        return listingMapper.toTravellerTripResponse(getEntity(id));
    }

    @Transactional
    public TravellerTripResponse update(UUID id, UpdateTravellerTripRequest request, UUID travellerId) {
        TravellerTrip entity = getEntityForOwner(id, travellerId);
        assertUpdatable(entity);
        listingMapper.updateTravellerTrip(entity, request);
        return listingMapper.toTravellerTripResponse(travellerTripRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id, UUID travellerId) {
        TravellerTrip entity = getEntityForOwner(id, travellerId);
        if (entity.getStatus() != TravellerTripStatus.DRAFT) {
            throw new InvalidStatusException("Only draft traveller trips can be deleted");
        }
        travellerTripRepository.delete(entity);
    }

    @Transactional
    public TravellerTripResponse publish(UUID id, UUID travellerId) {
        kycService.requireApprovedKyc(travellerId);
        TravellerTrip entity = getEntityForOwner(id, travellerId);
        if (entity.getStatus() != TravellerTripStatus.DRAFT) {
            throw new InvalidStatusException("Only draft traveller trips can be published");
        }
        entity.setStatus(TravellerTripStatus.PUBLISHED);
        return listingMapper.toTravellerTripResponse(travellerTripRepository.save(entity));
    }

    @Transactional
    public TravellerTripResponse cancel(UUID id, UUID travellerId) {
        TravellerTrip entity = getEntityForOwner(id, travellerId);
        if (entity.getStatus() == TravellerTripStatus.CLOSED) {
            throw new InvalidStatusException("Closed traveller trips cannot be cancelled");
        }
        if (entity.getStatus() == TravellerTripStatus.CANCELLED) {
            throw new InvalidStatusException("Traveller trip is already cancelled");
        }
        entity.setStatus(TravellerTripStatus.CANCELLED);
        return listingMapper.toTravellerTripResponse(travellerTripRepository.save(entity));
    }

    private TravellerTrip getEntity(UUID id) {
        return travellerTripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traveller trip not found"));
    }

    private TravellerTrip getEntityForOwner(UUID id, UUID travellerId) {
        TravellerTrip entity = getEntity(id);
        if (!entity.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("You can only manage your own traveller trips");
        }
        return entity;
    }

    private void assertUpdatable(TravellerTrip entity) {
        if (NON_UPDATABLE_STATUSES.contains(entity.getStatus())) {
            throw new InvalidStatusException(
                    "Traveller trip cannot be updated when status is " + entity.getStatus());
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}