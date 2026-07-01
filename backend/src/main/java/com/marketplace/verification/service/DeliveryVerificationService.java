package com.marketplace.verification.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.chat.service.ChatService;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.payment.dto.PaymentResponse;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.payment.service.PaymentService;
import com.marketplace.user.entity.Role;
import com.marketplace.verification.dto.DeliveryCodeStatusResponse;
import com.marketplace.verification.dto.DeliveryVerificationResultResponse;
import com.marketplace.verification.dto.GenerateDeliveryCodeResponse;
import com.marketplace.verification.entity.DeliveryCodeStatus;
import com.marketplace.verification.entity.DeliveryVerificationCode;
import com.marketplace.verification.mapper.DeliveryVerificationMapper;
import com.marketplace.verification.repository.DeliveryVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Set<BookingStatus> GENERATE_ALLOWED_STATUSES = EnumSet.of(
            BookingStatus.PAYMENT_HELD,
            BookingStatus.IN_TRANSIT
    );
    private static final Set<BookingStatus> VERIFY_ALLOWED_STATUSES = EnumSet.of(
            BookingStatus.PAYMENT_HELD,
            BookingStatus.IN_TRANSIT,
            BookingStatus.DELIVERED_PENDING_VERIFICATION
    );
    private static final Set<BookingStatus> DISALLOWED_FINAL_STATUSES = EnumSet.of(
            BookingStatus.COMPLETED,
            BookingStatus.CANCELLED,
            BookingStatus.DISPUTED
    );

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final DeliveryVerificationCodeRepository codeRepository;
    private final DeliveryCodeHashService hashService;
    private final DeliveryVerificationMapper mapper;
    private final NotificationService notificationService;
    private final ChatService chatService;

    @Transactional
    public GenerateDeliveryCodeResponse generateCode(UUID bookingId, UUID buyerId) {
        Booking booking = getBooking(bookingId);
        assertBuyerOwnsBooking(booking, buyerId);
        assertBookingValidForVerification(booking);
        if (!GENERATE_ALLOWED_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Booking is not eligible for delivery code generation");
        }
        assertPaymentHeld(bookingId);

        expireActiveCodeIfPresent(bookingId);

        String code = generateSixDigitCode();
        Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);

        DeliveryVerificationCode entity = DeliveryVerificationCode.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .codeHash(hashService.hashCode(bookingId, code))
                .status(DeliveryCodeStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();
        entity = codeRepository.save(entity);

        log.info("Delivery verification code generated for bookingId={}, buyerId={}", bookingId, buyerId);
        notificationService.notifyDeliveryCodeGenerated(buyerId, bookingId, code);

        return GenerateDeliveryCodeResponse.builder()
                .id(entity.getId())
                .bookingId(bookingId)
                .code(code)
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .message("Delivery code generated. In production this should be sent only to the buyer via notification.")
                .build();
    }

    @Transactional
    public DeliveryVerificationResultResponse verifyCode(UUID bookingId, UUID travellerId, String rawCode) {
        Booking booking = getBooking(bookingId);
        assertTravellerOwnsBooking(booking, travellerId);
        assertBookingValidForVerification(booking);
        if (!VERIFY_ALLOWED_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Booking is not eligible for delivery verification");
        }
        assertPaymentHeld(bookingId);

        DeliveryVerificationCode activeCode = codeRepository.findByBooking_IdAndStatus(bookingId, DeliveryCodeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active delivery verification code found"));

        if (activeCode.getExpiresAt().isBefore(Instant.now())) {
            activeCode.setStatus(DeliveryCodeStatus.EXPIRED);
            codeRepository.save(activeCode);
            throw new BadRequestException("Delivery verification code has expired");
        }

        if (!hashService.matches(bookingId, rawCode, activeCode.getCodeHash())) {
            throw new BadRequestException("Invalid delivery verification code");
        }

        Instant now = Instant.now();
        activeCode.setStatus(DeliveryCodeStatus.USED);
        activeCode.setVerifiedAt(now);
        codeRepository.save(activeCode);

        booking.setStatus(BookingStatus.DELIVERED);
        if (booking.getDeliveredAt() == null) {
            booking.setDeliveredAt(now);
        }
        bookingRepository.save(booking);

        PaymentResponse paymentResponse = paymentService.releasePaymentForVerifiedDelivery(bookingId);

        log.info("Delivery verification succeeded for bookingId={}, travellerId={}", bookingId, travellerId);
        notificationService.notifyDeliveryVerified(booking.getBuyer().getId(), booking.getTraveller().getId(), bookingId);
        chatService.createSystemMessage(bookingId, "Delivery verified and payment released.");

        return DeliveryVerificationResultResponse.builder()
                .bookingId(bookingId)
                .paymentId(paymentResponse.getId())
                .codeStatus(activeCode.getStatus())
                .bookingStatus(BookingStatus.COMPLETED)
                .paymentStatus(PaymentStatus.RELEASED)
                .verifiedAt(activeCode.getVerifiedAt())
                .paymentReleasedAt(paymentResponse.getReleasedAt())
                .message("Delivery verified successfully. Payment released to traveller.")
                .build();
    }

    @Transactional(readOnly = true)
    public DeliveryCodeStatusResponse getStatus(UUID bookingId, UserPrincipal principal) {
        Booking booking = getBooking(bookingId);
        assertCanViewBooking(booking, principal);

        Optional<DeliveryVerificationCode> activeCode = codeRepository.findByBooking_IdAndStatus(bookingId, DeliveryCodeStatus.ACTIVE);
        if (activeCode.isPresent()) {
            DeliveryVerificationCode code = activeCode.get();
            if (code.getExpiresAt().isBefore(Instant.now())) {
                return DeliveryCodeStatusResponse.builder()
                        .bookingId(bookingId)
                        .hasActiveCode(false)
                        .status(DeliveryCodeStatus.EXPIRED)
                        .expiresAt(code.getExpiresAt())
                        .verifiedAt(code.getVerifiedAt())
                        .createdAt(code.getCreatedAt())
                        .build();
            }
            return mapper.toStatusResponse(code, true);
        }

        DeliveryVerificationCode latest = codeRepository.findTopByBooking_IdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No active delivery verification code found"));
        return mapper.toStatusResponse(latest, false);
    }

    @Transactional
    public DeliveryCodeStatusResponse expireActiveCode(UUID bookingId) {
        expireActiveCodeIfPresent(bookingId);

        DeliveryVerificationCode latest = codeRepository.findTopByBooking_IdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No active delivery verification code found"));
        return mapper.toStatusResponse(latest, false);
    }

    private void expireActiveCodeIfPresent(UUID bookingId) {
        codeRepository.findByBooking_IdAndStatus(bookingId, DeliveryCodeStatus.ACTIVE)
                .ifPresent(code -> {
                    code.setStatus(DeliveryCodeStatus.EXPIRED);
                    codeRepository.save(code);
                });
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void assertBuyerOwnsBooking(Booking booking, UUID buyerId) {
        if (!booking.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("Only buyer can generate delivery code for this booking");
        }
    }

    private void assertTravellerOwnsBooking(Booking booking, UUID travellerId) {
        if (!booking.getTraveller().getId().equals(travellerId)) {
            throw new OwnershipException("Only traveller can verify delivery code for this booking");
        }
    }

    private void assertCanViewBooking(Booking booking, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to this booking");
        }
    }

    private void assertBookingValidForVerification(Booking booking) {
        if (DISALLOWED_FINAL_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Booking is not eligible for delivery verification");
        }
    }

    private void assertPaymentHeld(UUID bookingId) {
        PaymentStatus status = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"))
                .getStatus();
        if (status != PaymentStatus.HELD) {
            throw new BadRequestException("Payment is not held");
        }
    }

    private String generateSixDigitCode() {
        int value = 100000 + SECURE_RANDOM.nextInt(900000);
        return Integer.toString(value);
    }
}