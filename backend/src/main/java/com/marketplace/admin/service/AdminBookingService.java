package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminBookingDetailResponse;
import com.marketplace.booking.dto.AdminBookingResponse;
import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.mapper.BookingMapper;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.dispute.entity.Dispute;
import com.marketplace.dispute.repository.DisputeRepository;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationType;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.offer.entity.Offer;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeRepository disputeRepository;
    private final ReviewRepository reviewRepository;
    private final BookingMapper bookingMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public AdminBookingDetailResponse getDetail(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        Offer offer = booking.getOffer();

        Payment payment = paymentRepository.findByBooking_Id(bookingId).orElse(null);
        List<Dispute> disputes = disputeRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId);
        Dispute latestDispute = disputes.isEmpty() ? null : disputes.get(0);

        long reviewCount = reviewRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId).size();
        Double averageRating = null;
        if (reviewCount > 0) {
            double buyerAvg = reviewRepository.averageRatingByRevieweeId(booking.getBuyer().getId());
            double travellerAvg = reviewRepository.averageRatingByRevieweeId(booking.getTraveller().getId());
            averageRating = Math.round(((buyerAvg + travellerAvg) / 2.0) * 100.0) / 100.0;
        }

        AdminBookingResponse bookingResponse = bookingMapper.toAdminResponse(booking);

        return AdminBookingDetailResponse.builder()
                .booking(bookingResponse)
                .offer(AdminBookingDetailResponse.OfferSummary.builder()
                        .id(offer.getId())
                        .totalAmount(offer.getTotalAmount())
                        .currency(offer.getCurrency())
                        .status(offer.getStatus().name())
                        .build())
                .buyer(AdminBookingDetailResponse.ParticipantSummary.builder()
                        .id(booking.getBuyer().getId())
                        .name(booking.getBuyer().getFullName())
                        .email(booking.getBuyer().getEmail())
                        .build())
                .traveller(AdminBookingDetailResponse.ParticipantSummary.builder()
                        .id(booking.getTraveller().getId())
                        .name(booking.getTraveller().getFullName())
                        .email(booking.getTraveller().getEmail())
                        .build())
                .payment(payment != null ? AdminBookingDetailResponse.PaymentSummary.builder()
                        .id(payment.getId())
                        .status(payment.getStatus())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .paidAt(payment.getPaidAt())
                        .releasedAt(payment.getReleasedAt())
                        .refundedAt(payment.getRefundedAt())
                        .build() : null)
                .dispute(latestDispute != null ? AdminBookingDetailResponse.DisputeSummary.builder()
                        .id(latestDispute.getId())
                        .status(latestDispute.getStatus())
                        .reason(latestDispute.getReason().name())
                        .createdAt(latestDispute.getCreatedAt())
                        .build() : null)
                .reviewSummary(AdminBookingDetailResponse.ReviewSummary.builder()
                        .reviewCount(reviewCount)
                        .averageRating(averageRating)
                        .build())
                .build();
    }

    @Transactional
    public AdminBookingResponse cancel(UUID bookingId, AdminActionReasonRequest request) {
        Booking booking = getBooking(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidStatusException("Completed bookings cannot be cancelled");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusException("Booking is already cancelled");
        }

        Payment payment = paymentRepository.findByBooking_Id(bookingId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.HELD) {
            throw new BadRequestException("Booking has held payment. Refund payment before cancellation.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking = bookingRepository.save(booking);

        auditLogService.logAdminAction(AuditAction.ADMIN_BOOKING_CANCELLED, "BOOKING", bookingId,
                "reason=" + request.getReason().trim());
        notificationService.notifyUsers(
                List.of(booking.getBuyer().getId(), booking.getTraveller().getId()),
                NotificationType.SYSTEM_ALERT,
                "Booking cancelled",
                "A booking was cancelled by an administrator. Reason: " + request.getReason().trim(),
                NotificationReferenceType.BOOKING,
                bookingId);

        log.info("Admin cancelled booking: bookingId={}, reason={}", bookingId, request.getReason());
        return bookingMapper.toAdminResponse(booking);
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }
}
