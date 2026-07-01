package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminChangeRoleRequest;
import com.marketplace.admin.dto.AdminProfileSummary;
import com.marketplace.admin.dto.AdminUserDetailResponse;
import com.marketplace.admin.dto.AdminUserFilter;
import com.marketplace.admin.dto.AdminUserResponse;
import com.marketplace.admin.mapper.AdminMapper;
import com.marketplace.admin.repository.AdminUserSpecification;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.kyc.entity.KycDocument;
import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.kyc.repository.KycDocumentRepository;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.listing.repository.TravellerTripRepository;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.review.repository.ReviewRepository;
import com.marketplace.user.entity.AccountStatus;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final BuyerRequestRepository buyerRequestRepository;
    private final TravellerTripRepository travellerTripRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final AdminMapper adminMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> search(AdminUserFilter filter, Pageable pageable) {
        return userRepository.findAll(AdminUserSpecification.withFilter(filter), pageable)
                .map(adminMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getDetail(UUID userId) {
        User user = getUser(userId);
        KycStatus kycStatus = kycDocumentRepository.findFirstByUser_Id(userId)
                .map(KycDocument::getStatus)
                .orElse(null);

        double averageRating = reviewRepository.averageRatingByRevieweeId(userId);
        long totalReviews = reviewRepository.countByReviewee_Id(userId);

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .profileCompleted(user.isProfileCompleted())
                .profile(AdminProfileSummary.builder()
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .profileCompleted(user.isProfileCompleted())
                        .build())
                .kycStatus(kycStatus)
                .buyerRequestCount(buyerRequestRepository.countByBuyer_Id(userId))
                .travellerTripCount(travellerTripRepository.countByTraveller_Id(userId))
                .bookingAsBuyerCount(bookingRepository.countByBuyer_Id(userId))
                .bookingAsTravellerCount(bookingRepository.countByTraveller_Id(userId))
                .averageRating(Math.round(averageRating * 100.0) / 100.0)
                .totalReviews(totalReviews)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public AdminUserResponse disable(UUID userId, AdminActionReasonRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        assertNotSelf(adminId, userId, "Admin cannot disable their own account");

        User user = getUser(userId);
        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            return adminMapper.toUserResponse(user);
        }

        user.setAccountStatus(AccountStatus.DISABLED);
        user = userRepository.save(user);

        auditLogService.logAdminAction(AuditAction.ADMIN_USER_DISABLED, "USER", userId,
                "reason=" + request.getReason().trim());
        notificationService.notifyAccountStatusChanged(userId, "Account disabled",
                "Your account has been disabled. Reason: " + request.getReason().trim());

        log.info("Admin disabled user: userId={}, adminId={}", userId, adminId);
        return adminMapper.toUserResponse(user);
    }

    @Transactional
    public AdminUserResponse enable(UUID userId) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        User user = getUser(userId);
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            return adminMapper.toUserResponse(user);
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        auditLogService.logAdminAction(AuditAction.ADMIN_USER_ENABLED, "USER", userId, null);
        notificationService.notifyAccountStatusChanged(userId, "Account enabled",
                "Your account has been re-enabled by an administrator.");

        log.info("Admin enabled user: userId={}, adminId={}", userId, adminId);
        return adminMapper.toUserResponse(user);
    }

    @Transactional
    public AdminUserResponse changeRole(UUID userId, AdminChangeRoleRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        assertNotSelf(adminId, userId, "Admin cannot change their own role");

        User user = getUser(userId);
        Role previousRole = user.getRole();
        if (previousRole == request.getRole()) {
            return adminMapper.toUserResponse(user);
        }

        user.setRole(request.getRole());
        user = userRepository.save(user);

        auditLogService.logAdminAction(AuditAction.ADMIN_USER_ROLE_CHANGED, "USER", userId,
                "from=" + previousRole + ",to=" + request.getRole());
        notificationService.notifyAccountStatusChanged(userId, "Role updated",
                "Your account role has been changed to " + request.getRole() + ".");

        log.info("Admin changed user role: userId={}, from={}, to={}", userId, previousRole, request.getRole());
        return adminMapper.toUserResponse(user);
    }

    private void assertNotSelf(UUID adminId, UUID targetUserId, String message) {
        if (adminId != null && adminId.equals(targetUserId)) {
            throw new BadRequestException(message);
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
