package com.marketplace.listing.service;

import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.listing.dto.BuyerRequestFilter;
import com.marketplace.listing.dto.BuyerRequestResponse;
import com.marketplace.listing.dto.CreateBuyerRequestRequest;
import com.marketplace.listing.dto.UpdateBuyerRequestRequest;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.mapper.ListingMapper;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.listing.repository.BuyerRequestSpecification;
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
public class BuyerRequestService {

    private static final Set<BuyerRequestStatus> NON_UPDATABLE_STATUSES = EnumSet.of(
            BuyerRequestStatus.BOOKED,
            BuyerRequestStatus.COMPLETED,
            BuyerRequestStatus.CANCELLED
    );

    private final BuyerRequestRepository buyerRequestRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;

    @Transactional
    public BuyerRequestResponse create(CreateBuyerRequestRequest request, UUID buyerId) {
        User buyer = getUser(buyerId);
        BuyerRequest entity = listingMapper.toBuyerRequest(request, buyer);
        return listingMapper.toBuyerRequestResponse(buyerRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<BuyerRequestResponse> search(BuyerRequestFilter filter, UserPrincipal viewer, Pageable pageable) {
        Specification<BuyerRequest> spec = BuyerRequestSpecification.withFilters(filter, null, viewer.getRole());
        return buyerRequestRepository.findAll(spec, pageable)
                .map(listingMapper::toBuyerRequestResponse);
    }

    @Transactional(readOnly = true)
    public Page<BuyerRequestResponse> findMyRequests(UUID buyerId, BuyerRequestFilter filter, Pageable pageable) {
        Specification<BuyerRequest> spec = BuyerRequestSpecification.withFilters(filter, buyerId, Role.BUYER);
        return buyerRequestRepository.findAll(spec, pageable)
                .map(listingMapper::toBuyerRequestResponse);
    }

    @Transactional(readOnly = true)
    public BuyerRequestResponse getById(UUID id) {
        return listingMapper.toBuyerRequestResponse(getEntity(id));
    }

    @Transactional
    public BuyerRequestResponse update(UUID id, UpdateBuyerRequestRequest request, UUID buyerId) {
        BuyerRequest entity = getEntityForOwner(id, buyerId);
        assertUpdatable(entity);
        listingMapper.updateBuyerRequest(entity, request);
        return listingMapper.toBuyerRequestResponse(buyerRequestRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id, UUID buyerId) {
        BuyerRequest entity = getEntityForOwner(id, buyerId);
        if (entity.getStatus() != BuyerRequestStatus.DRAFT) {
            throw new InvalidStatusException("Only draft buyer requests can be deleted");
        }
        buyerRequestRepository.delete(entity);
    }

    @Transactional
    public BuyerRequestResponse publish(UUID id, UUID buyerId) {
        BuyerRequest entity = getEntityForOwner(id, buyerId);
        if (entity.getStatus() != BuyerRequestStatus.DRAFT) {
            throw new InvalidStatusException("Only draft buyer requests can be published");
        }
        entity.setStatus(BuyerRequestStatus.PUBLISHED);
        return listingMapper.toBuyerRequestResponse(buyerRequestRepository.save(entity));
    }

    @Transactional
    public BuyerRequestResponse cancel(UUID id, UUID buyerId) {
        BuyerRequest entity = getEntityForOwner(id, buyerId);
        if (entity.getStatus() == BuyerRequestStatus.COMPLETED) {
            throw new InvalidStatusException("Completed buyer requests cannot be cancelled");
        }
        if (entity.getStatus() == BuyerRequestStatus.CANCELLED) {
            throw new InvalidStatusException("Buyer request is already cancelled");
        }
        entity.setStatus(BuyerRequestStatus.CANCELLED);
        return listingMapper.toBuyerRequestResponse(buyerRequestRepository.save(entity));
    }

    private BuyerRequest getEntity(UUID id) {
        return buyerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer request not found"));
    }

    private BuyerRequest getEntityForOwner(UUID id, UUID buyerId) {
        BuyerRequest entity = getEntity(id);
        if (!entity.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("You can only manage your own buyer requests");
        }
        return entity;
    }

    private void assertUpdatable(BuyerRequest entity) {
        if (NON_UPDATABLE_STATUSES.contains(entity.getStatus())) {
            throw new InvalidStatusException(
                    "Buyer request cannot be updated when status is " + entity.getStatus());
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}