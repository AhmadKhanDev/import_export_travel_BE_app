package com.marketplace.notification.service;

import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.notification.dto.AdminSendNotificationRequest;
import com.marketplace.notification.dto.CreateNotificationCommand;
import com.marketplace.notification.dto.NotificationFilter;
import com.marketplace.notification.dto.NotificationResponse;
import com.marketplace.notification.dto.NotificationSendRequest;
import com.marketplace.notification.dto.NotificationSendResult;
import com.marketplace.notification.dto.UnreadCountResponse;
import com.marketplace.notification.entity.Notification;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationStatus;
import com.marketplace.notification.entity.NotificationType;
import com.marketplace.notification.mapper.NotificationMapper;
import com.marketplace.notification.provider.NotificationProvider;
import com.marketplace.notification.provider.NotificationProviderFactory;
import com.marketplace.notification.repository.NotificationRepository;
import com.marketplace.notification.repository.NotificationSpecification;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationProviderFactory notificationProviderFactory;
    private final NotificationMapper notificationMapper;

    @Transactional
    public List<NotificationResponse> createNotification(CreateNotificationCommand command) {
        User user = getUser(command.getUserId());
        List<NotificationResponse> responses = new ArrayList<>();

        for (NotificationChannel channel : command.getChannels()) {
            NotificationProvider provider = notificationProviderFactory.getProvider(channel);
            NotificationSendResult result = provider.send(NotificationSendRequest.builder()
                    .userId(command.getUserId())
                    .notificationType(command.getNotificationType())
                    .title(command.getTitle())
                    .message(command.getMessage())
                    .channel(channel)
                    .referenceType(command.getReferenceType())
                    .referenceId(command.getReferenceId())
                    .build());

            Instant now = Instant.now();
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .notificationType(command.getNotificationType())
                    .title(command.getTitle())
                    .message(command.getMessage())
                    .channel(channel)
                    .status(result.getStatus())
                    .referenceType(command.getReferenceType())
                    .referenceId(command.getReferenceId())
                    .failureReason(result.getFailureReason())
                    .sentAt(result.getStatus() == NotificationStatus.SENT ? now : null)
                    .build();
            responses.add(notificationMapper.toResponse(notificationRepository.save(notification)));
        }

        return responses;
    }

    @Transactional
    public NotificationResponse sendManual(AdminSendNotificationRequest request) {
        return createNotification(CreateNotificationCommand.builder()
                .userId(request.getUserId())
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .message(request.getMessage())
                .channels(List.of(request.getChannel()))
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .build()).get(0);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID userId, NotificationFilter filter, Pageable pageable) {
        NotificationFilter effectiveFilter = NotificationFilter.builder()
                .userId(userId)
                .status(filter.getStatus())
                .channel(filter.getChannel())
                .type(filter.getType())
                .referenceType(filter.getReferenceType())
                .referenceId(filter.getReferenceId())
                .unreadOnly(filter.isUnreadOnly())
                .build();
        return notificationRepository.findAll(NotificationSpecification.withFilter(effectiveFilter), pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID notificationId, UserPrincipal principal) {
        Notification notification = getNotification(notificationId);
        if (principal.getRole() != Role.ADMIN && !notification.getUser().getId().equals(principal.getId())) {
            throw new OwnershipException("You do not have access to this notification");
        }
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = getNotification(notificationId);
        if (!notification.getUser().getId().equals(userId)) {
            throw new OwnershipException("You can only mark your own notifications as read");
        }
        if (notification.getStatus() == NotificationStatus.READ) {
            return notificationMapper.toResponse(notification);
        }
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllInAppAsRead(userId, Instant.now());
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUser_IdAndStatusAndChannel(
                userId, NotificationStatus.SENT, NotificationChannel.IN_APP);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> adminSearch(NotificationFilter filter, Pageable pageable) {
        return notificationRepository.findAll(NotificationSpecification.withFilter(filter), pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional
    public void notifyUser(UUID userId, NotificationType type, String title, String message,
                           NotificationReferenceType referenceType, UUID referenceId) {
        createNotification(CreateNotificationCommand.builder()
                .userId(userId)
                .notificationType(type)
                .title(title)
                .message(message)
                .channels(List.of(NotificationChannel.IN_APP))
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build());
    }

    @Transactional
    public void notifyUsers(List<UUID> userIds, NotificationType type, String title, String message,
                            NotificationReferenceType referenceType, UUID referenceId) {
        for (UUID userId : userIds) {
            notifyUser(userId, type, title, message, referenceType, referenceId);
        }
    }

    @Transactional
    public void notifyKycSubmitted(UUID adminUserId, UUID kycId) {
        notifyUser(adminUserId, NotificationType.KYC_SUBMITTED,
                "New KYC submitted", "A traveller has submitted KYC for review.",
                NotificationReferenceType.KYC, kycId);
    }

    @Transactional
    public void notifyKycApproved(UUID travellerUserId, UUID kycId) {
        notifyUser(travellerUserId, NotificationType.KYC_APPROVED,
                "KYC approved", "Your KYC has been approved.",
                NotificationReferenceType.KYC, kycId);
    }

    @Transactional
    public void notifyKycRejected(UUID travellerUserId, UUID kycId, String reason) {
        notifyUser(travellerUserId, NotificationType.KYC_REJECTED,
                "KYC rejected", "Your KYC was rejected. Reason: " + reason,
                NotificationReferenceType.KYC, kycId);
    }

    @Transactional
    public void notifyOfferSent(UUID buyerUserId, UUID offerId) {
        notifyUser(buyerUserId, NotificationType.OFFER_SENT,
                "New offer received", "A traveller has sent you an offer.",
                NotificationReferenceType.OFFER, offerId);
    }

    @Transactional
    public void notifyOfferAccepted(UUID travellerUserId, UUID offerId) {
        notifyUser(travellerUserId, NotificationType.OFFER_ACCEPTED,
                "Offer accepted", "Your offer has been accepted by the buyer.",
                NotificationReferenceType.OFFER, offerId);
    }

    @Transactional
    public void notifyOfferRejected(UUID travellerUserId, UUID offerId) {
        notifyUser(travellerUserId, NotificationType.OFFER_REJECTED,
                "Offer rejected", "Your offer has been rejected by the buyer.",
                NotificationReferenceType.OFFER, offerId);
    }

    @Transactional
    public void notifyOfferCancelled(UUID buyerUserId, UUID offerId) {
        notifyUser(buyerUserId, NotificationType.OFFER_CANCELLED,
                "Offer cancelled", "A traveller cancelled their offer.",
                NotificationReferenceType.OFFER, offerId);
    }

    @Transactional
    public void notifyBookingCreated(UUID buyerUserId, UUID travellerUserId, UUID bookingId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.BOOKING_CREATED,
                "Booking created", "A booking has been created successfully.",
                NotificationReferenceType.BOOKING, bookingId);
    }

    @Transactional
    public void notifyPaymentHeld(UUID buyerUserId, UUID travellerUserId, UUID paymentId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.PAYMENT_HELD,
                "Payment held in escrow", "Payment has been received and held in escrow.",
                NotificationReferenceType.PAYMENT, paymentId);
    }

    @Transactional
    public void notifyDeliveryCodeGenerated(UUID buyerUserId, UUID bookingId, String code) {
        notifyUser(buyerUserId, NotificationType.DELIVERY_CODE_GENERATED,
                "Delivery code generated",
                "Your delivery verification code is " + code + ". In production this should only be sent to the buyer.",
                NotificationReferenceType.DELIVERY_CODE, bookingId);
    }

    @Transactional
    public void notifyDeliveryVerified(UUID buyerUserId, UUID travellerUserId, UUID bookingId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.DELIVERY_VERIFIED,
                "Delivery verified", "Delivery has been verified successfully.",
                NotificationReferenceType.BOOKING, bookingId);
    }

    @Transactional
    public void notifyPaymentReleased(UUID buyerUserId, UUID travellerUserId, UUID paymentId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.PAYMENT_RELEASED,
                "Payment released", "Escrow payment has been released to the traveller.",
                NotificationReferenceType.PAYMENT, paymentId);
    }

    @Transactional
    public void notifyPaymentRefunded(UUID buyerUserId, UUID travellerUserId, UUID paymentId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.PAYMENT_REFUNDED,
                "Payment refunded", "Payment has been refunded.",
                NotificationReferenceType.PAYMENT, paymentId);
    }

    @Transactional
    public void notifyReviewReceived(UUID revieweeUserId, UUID reviewId) {
        notifyUser(revieweeUserId, NotificationType.REVIEW_RECEIVED,
                "New review received", "You received a new rating.",
                NotificationReferenceType.BOOKING, reviewId);
    }

    @Transactional
    public void notifyDisputeCreated(UUID bookingId, UUID disputeId, UUID otherParticipantId) {
        List<UUID> adminIds = getAdminUserIds();
        if (!adminIds.isEmpty()) {
            notifyUsers(adminIds, NotificationType.DISPUTE_CREATED,
                    "New dispute created",
                    "A new dispute has been created for booking " + bookingId + ".",
                    NotificationReferenceType.DISPUTE, disputeId);
        } else {
            log.warn("No admin users found to notify for dispute {}", disputeId);
        }
        notifyUser(otherParticipantId, NotificationType.DISPUTE_CREATED,
                "Dispute created", "A dispute has been created for your booking.",
                NotificationReferenceType.DISPUTE, disputeId);
    }

    @Transactional
    public void notifyDisputeUnderReview(UUID buyerUserId, UUID travellerUserId, UUID disputeId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.DISPUTE_UNDER_REVIEW,
                "Dispute under review", "Your dispute is now under admin review.",
                NotificationReferenceType.DISPUTE, disputeId);
    }

    @Transactional
    public void notifyDisputeResolved(UUID buyerUserId, UUID travellerUserId, UUID disputeId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.DISPUTE_RESOLVED,
                "Dispute resolved", "Your dispute has been resolved by an admin.",
                NotificationReferenceType.DISPUTE, disputeId);
    }

    @Transactional
    public void notifyDisputeRejected(UUID buyerUserId, UUID travellerUserId, UUID disputeId) {
        notifyUsers(List.of(buyerUserId, travellerUserId), NotificationType.DISPUTE_REJECTED,
                "Dispute rejected", "Your dispute has been rejected by an admin.",
                NotificationReferenceType.DISPUTE, disputeId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getAdminUserIds() {
        return userRepository.findByRole(Role.ADMIN).stream().map(User::getId).toList();
    }

    private Notification getNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}