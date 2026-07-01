package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminBuyerRequestResponse;
import com.marketplace.admin.dto.AdminTravellerTripResponse;
import com.marketplace.admin.mapper.AdminMapper;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.listing.dto.BuyerRequestFilter;
import com.marketplace.listing.dto.TravellerTripFilter;
import com.marketplace.listing.entity.BuyerRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.entity.TravellerTrip;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.listing.repository.BuyerRequestSpecification;
import com.marketplace.listing.repository.TravellerTripRepository;
import com.marketplace.listing.repository.TravellerTripSpecification;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminListingService {

    private final BuyerRequestRepository buyerRequestRepository;
    private final TravellerTripRepository travellerTripRepository;
    private final AdminMapper adminMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AdminBuyerRequestResponse> searchBuyerRequests(
            UUID buyerId, BuyerRequestFilter filter, Pageable pageable) {
        BuyerRequestFilter effectiveFilter = filter != null ? filter : BuyerRequestFilter.builder().build();
        Specification<BuyerRequest> spec = BuyerRequestSpecification.withFilters(effectiveFilter, buyerId, Role.ADMIN);
        return buyerRequestRepository.findAll(spec, pageable).map(adminMapper::toBuyerRequestResponse);
    }

    @Transactional(readOnly = true)
    public Page<AdminTravellerTripResponse> searchTravellerTrips(
            UUID travellerId, TravellerTripFilter filter, Pageable pageable) {
        TravellerTripFilter effectiveFilter = filter != null ? filter : TravellerTripFilter.builder().build();
        Specification<TravellerTrip> spec = TravellerTripSpecification.withFilters(
                effectiveFilter, travellerId, Role.ADMIN);
        return travellerTripRepository.findAll(spec, pageable).map(adminMapper::toTravellerTripResponse);
    }

    @Transactional
    public AdminBuyerRequestResponse cancelBuyerRequest(UUID id, AdminActionReasonRequest request) {
        BuyerRequest entity = buyerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer request not found"));

        if (entity.getStatus() == BuyerRequestStatus.COMPLETED) {
            throw new InvalidStatusException("Completed buyer requests cannot be cancelled");
        }
        if (entity.getStatus() == BuyerRequestStatus.CANCELLED) {
            throw new InvalidStatusException("Buyer request is already cancelled");
        }

        entity.setStatus(BuyerRequestStatus.CANCELLED);
        entity = buyerRequestRepository.save(entity);

        auditLogService.logAdminAction(AuditAction.ADMIN_BUYER_REQUEST_CANCELLED, "BUYER_REQUEST", id,
                "reason=" + request.getReason().trim());
        notificationService.notifyUser(entity.getBuyer().getId(),
                com.marketplace.notification.entity.NotificationType.SYSTEM_ALERT,
                "Buyer request cancelled",
                "Your buyer request was cancelled by an administrator. Reason: " + request.getReason().trim(),
                com.marketplace.notification.entity.NotificationReferenceType.BUYER_REQUEST, id);

        log.info("Admin cancelled buyer request: id={}, reason={}", id, request.getReason());
        return adminMapper.toBuyerRequestResponse(entity);
    }

    @Transactional
    public AdminTravellerTripResponse cancelTravellerTrip(UUID id, AdminActionReasonRequest request) {
        TravellerTrip entity = travellerTripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traveller trip not found"));

        if (entity.getStatus() == TravellerTripStatus.CLOSED) {
            throw new InvalidStatusException("Closed traveller trips cannot be cancelled");
        }
        if (entity.getStatus() == TravellerTripStatus.CANCELLED) {
            throw new InvalidStatusException("Traveller trip is already cancelled");
        }

        entity.setStatus(TravellerTripStatus.CANCELLED);
        entity = travellerTripRepository.save(entity);

        auditLogService.logAdminAction(AuditAction.ADMIN_TRAVELLER_TRIP_CANCELLED, "TRAVELLER_TRIP", id,
                "reason=" + request.getReason().trim());
        notificationService.notifyUser(entity.getTraveller().getId(),
                com.marketplace.notification.entity.NotificationType.SYSTEM_ALERT,
                "Traveller trip cancelled",
                "Your traveller trip was cancelled by an administrator. Reason: " + request.getReason().trim(),
                com.marketplace.notification.entity.NotificationReferenceType.TRAVELLER_TRIP, id);

        log.info("Admin cancelled traveller trip: id={}, reason={}", id, request.getReason());
        return adminMapper.toTravellerTripResponse(entity);
    }
}
