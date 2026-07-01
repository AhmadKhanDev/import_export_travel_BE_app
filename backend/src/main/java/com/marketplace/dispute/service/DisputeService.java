package com.marketplace.dispute.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ConflictException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.dispute.dto.AdminDisputeResponse;
import com.marketplace.dispute.dto.CreateDisputeRequest;
import com.marketplace.dispute.dto.DisputeFilter;
import com.marketplace.dispute.dto.DisputeResponse;
import com.marketplace.dispute.dto.RejectDisputeRequest;
import com.marketplace.dispute.dto.ResolveDisputeRequest;
import com.marketplace.dispute.entity.Dispute;
import com.marketplace.dispute.entity.DisputeStatus;
import com.marketplace.dispute.mapper.DisputeMapper;
import com.marketplace.dispute.repository.DisputeRepository;
import com.marketplace.dispute.repository.DisputeSpecification;
import com.marketplace.chat.service.ChatService;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    private static final Set<DisputeStatus> ACTIVE_DISPUTE_STATUSES = EnumSet.of(
            DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW);

    private static final Set<DisputeStatus> RESOLVABLE_STATUSES = EnumSet.of(
            DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW);

    private static final Set<BookingStatus> PRE_PAYMENT_OR_CANCELLED_STATUSES = EnumSet.of(
            BookingStatus.PENDING_OFFER,
            BookingStatus.OFFER_SENT,
            BookingStatus.ACCEPTED,
            BookingStatus.PAYMENT_PENDING,
            BookingStatus.CANCELLED);

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DisputeMapper disputeMapper;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final AuditLogService auditLogService;

    @Transactional
    public DisputeResponse createDispute(CreateDisputeRequest request, UserPrincipal principal) {
        if (principal.getRole() != Role.BUYER && principal.getRole() != Role.TRAVELLER) {
            throw new BadRequestException("Only buyers and travellers can create disputes");
        }

        Booking booking = getBooking(request.getBookingId());
        assertParticipant(booking, principal.getId());

        if (PRE_PAYMENT_OR_CANCELLED_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException(
                    "Disputes cannot be created for bookings cancelled before payment or without payment");
        }

        if (disputeRepository.existsByBooking_IdAndStatusIn(booking.getId(), ACTIVE_DISPUTE_STATUSES)) {
            throw new ConflictException("An active dispute already exists for this booking");
        }

        User raisedBy = getUser(principal.getId());

        Dispute dispute = Dispute.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .raisedByUser(raisedBy)
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(DisputeStatus.OPEN)
                .build();
        dispute = disputeRepository.save(dispute);

        booking.setStatus(BookingStatus.DISPUTED);
        bookingRepository.save(booking);

        log.info("Dispute created: disputeId={}, bookingId={}, raisedByUserId={}, reason={}",
                dispute.getId(), booking.getId(), raisedBy.getId(), request.getReason());

        UUID otherParticipantId = booking.getBuyer().getId().equals(principal.getId())
                ? booking.getTraveller().getId()
                : booking.getBuyer().getId();

        notificationService.notifyDisputeCreated(
                booking.getId(), dispute.getId(), otherParticipantId);
        chatService.createSystemMessage(booking.getId(), "A dispute has been created for this booking.");

        return disputeMapper.toResponse(dispute);
    }

    @Transactional(readOnly = true)
    public Page<DisputeResponse> getMyDisputes(UUID userId, DisputeStatus status, Pageable pageable) {
        DisputeFilter filter = DisputeFilter.builder().status(status).build();
        return disputeRepository.findAll(DisputeSpecification.forParticipant(userId, filter), pageable)
                .map(disputeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DisputeResponse getById(UUID disputeId, UserPrincipal principal) {
        Dispute dispute = getDispute(disputeId);
        assertCanView(dispute, principal);
        return disputeMapper.toResponse(dispute);
    }

    @Transactional(readOnly = true)
    public AdminDisputeResponse adminGetById(UUID disputeId) {
        return disputeMapper.toAdminResponse(getDispute(disputeId));
    }

    @Transactional(readOnly = true)
    public Page<AdminDisputeResponse> adminSearch(DisputeFilter filter, Pageable pageable) {
        return disputeRepository.findAll(DisputeSpecification.withFilter(filter), pageable)
                .map(disputeMapper::toAdminResponse);
    }

    @Transactional
    public DisputeResponse markUnderReview(UUID disputeId, UUID adminId) {
        Dispute dispute = getDispute(disputeId);
        assertOpen(dispute);

        User admin = getUser(adminId);
        assertAdmin(admin);

        dispute.setStatus(DisputeStatus.UNDER_REVIEW);
        dispute = disputeRepository.save(dispute);

        log.info("Dispute marked under review: disputeId={}, adminId={}", disputeId, adminId);

        Booking booking = dispute.getBooking();
        notificationService.notifyDisputeUnderReview(
                booking.getBuyer().getId(), booking.getTraveller().getId(), dispute.getId());
        auditLogService.logAdminAction(AuditAction.ADMIN_DISPUTE_UNDER_REVIEW, "DISPUTE", disputeId, null);

        return disputeMapper.toResponse(dispute);
    }

    @Transactional
    public DisputeResponse resolve(UUID disputeId, UUID adminId, ResolveDisputeRequest request) {
        Dispute dispute = getDispute(disputeId);
        assertResolvable(dispute);

        User admin = getUser(adminId);
        assertAdmin(admin);

        Instant now = Instant.now();
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedByAdmin(admin);
        dispute.setResolutionNote(request.getResolutionNote().trim());
        dispute.setResolvedAt(now);
        dispute = disputeRepository.save(dispute);

        applyBookingStatusOnResolve(dispute.getBooking());

        log.info("Dispute resolved: disputeId={}, adminId={}", disputeId, adminId);

        Booking booking = dispute.getBooking();
        notificationService.notifyDisputeResolved(
                booking.getBuyer().getId(), booking.getTraveller().getId(), dispute.getId());
        auditLogService.logAdminAction(AuditAction.ADMIN_DISPUTE_RESOLVED, "DISPUTE", disputeId,
                "resolutionNote=" + request.getResolutionNote().trim());

        return disputeMapper.toResponse(dispute);
    }

    @Transactional
    public DisputeResponse reject(UUID disputeId, UUID adminId, RejectDisputeRequest request) {
        Dispute dispute = getDispute(disputeId);
        assertResolvable(dispute);

        User admin = getUser(adminId);
        assertAdmin(admin);

        Instant now = Instant.now();
        dispute.setStatus(DisputeStatus.REJECTED);
        dispute.setResolvedByAdmin(admin);
        dispute.setResolutionNote(request.getResolutionNote().trim());
        dispute.setResolvedAt(now);
        dispute = disputeRepository.save(dispute);

        log.info("Dispute rejected: disputeId={}, adminId={}", disputeId, adminId);

        Booking booking = dispute.getBooking();
        notificationService.notifyDisputeRejected(
                booking.getBuyer().getId(), booking.getTraveller().getId(), dispute.getId());
        auditLogService.logAdminAction(AuditAction.ADMIN_DISPUTE_REJECTED, "DISPUTE", disputeId,
                "resolutionNote=" + request.getResolutionNote().trim());

        return disputeMapper.toResponse(dispute);
    }

    private void applyBookingStatusOnResolve(Booking booking) {
        Payment payment = paymentRepository.findByBooking_Id(booking.getId()).orElse(null);
        if (payment != null
                && payment.getStatus() == PaymentStatus.RELEASED
                && booking.getStatus() == BookingStatus.DISPUTED) {
            booking.setStatus(BookingStatus.COMPLETED);
            if (booking.getCompletedAt() == null) {
                booking.setCompletedAt(Instant.now());
            }
            bookingRepository.save(booking);
        }
    }

    private void assertOpen(Dispute dispute) {
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new InvalidStatusException("Dispute must be OPEN to mark as under review");
        }
    }

    private void assertResolvable(Dispute dispute) {
        if (!RESOLVABLE_STATUSES.contains(dispute.getStatus())) {
            throw new InvalidStatusException("Dispute cannot be changed in status " + dispute.getStatus());
        }
    }

    private void assertAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only admins can perform this action");
        }
    }

    private void assertParticipant(Booking booking, UUID userId) {
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("Only booking participants can create disputes");
        }
    }

    private void assertCanView(Dispute dispute, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        Booking booking = dispute.getBooking();
        if (!booking.getBuyer().getId().equals(userId)
                && !booking.getTraveller().getId().equals(userId)
                && !dispute.getRaisedByUser().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to this dispute");
        }
    }

    private Dispute getDispute(UUID disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
