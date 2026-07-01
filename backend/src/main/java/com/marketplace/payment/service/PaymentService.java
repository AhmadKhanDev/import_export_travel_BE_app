package com.marketplace.payment.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ConflictException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.offer.entity.Offer;
import com.marketplace.payment.dto.AdminPaymentResponse;
import com.marketplace.payment.dto.PayRequest;
import com.marketplace.payment.dto.PaymentFilter;
import com.marketplace.payment.dto.PaymentResponse;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentProviderType;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.mapper.PaymentMapper;
import com.marketplace.payment.provider.PaymentProvider;
import com.marketplace.payment.provider.PaymentProviderChargeRequest;
import com.marketplace.payment.provider.PaymentProviderFactory;
import com.marketplace.payment.provider.PaymentProviderRefundRequest;
import com.marketplace.payment.provider.PaymentProviderReleaseRequest;
import com.marketplace.payment.provider.PaymentProviderResult;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.payment.repository.PaymentSpecification;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final PaymentProviderType DEFAULT_PROVIDER = PaymentProviderType.FAKE;

    private static final Set<BookingStatus> RELEASE_ELIGIBLE_BOOKING_STATUSES = EnumSet.of(
            BookingStatus.DELIVERED,
            BookingStatus.DELIVERED_PENDING_VERIFICATION);

    private static final Set<PaymentStatus> REFUNDABLE_PAYMENT_STATUSES = EnumSet.of(
            PaymentStatus.PENDING,
            PaymentStatus.HELD);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse pay(UUID bookingId, UUID buyerId, PayRequest payRequest, String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        if (normalizedKey != null) {
            return paymentRepository.findByIdempotencyKey(normalizedKey)
                    .map(paymentMapper::toResponse)
                    .orElseGet(() -> processPayment(bookingId, buyerId, payRequest, normalizedKey));
        }

        return paymentRepository.findByBooking_Id(bookingId)
                .map(existing -> handleExistingPaymentOnPay(existing, buyerId))
                .orElseGet(() -> processPayment(bookingId, buyerId, payRequest, null));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByBookingId(UUID bookingId, UserPrincipal principal) {
        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        assertCanView(payment, principal);
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> findMyPayments(UUID userId, PaymentStatus status, Pageable pageable) {
        PaymentFilter filter = PaymentFilter.builder().status(status).build();
        Specification<Payment> spec = PaymentSpecification.forParticipant(userId, filter);
        return paymentRepository.findAll(spec, pageable).map(paymentMapper::toResponse);
    }

    @Transactional
    public PaymentResponse release(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        return paymentMapper.toResponse(executeRelease(payment));
    }

    @Transactional
    public PaymentResponse releasePaymentForVerifiedDelivery(UUID bookingId) {
        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return paymentMapper.toResponse(executeRelease(payment));
    }

    @Transactional
    public PaymentResponse refund(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        return paymentMapper.toResponse(executeRefund(payment, "Admin refund"));
    }

    @Transactional
    public PaymentResponse refundPaymentForBooking(UUID bookingId, String reason) {
        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return paymentMapper.toResponse(executeRefund(payment, reason));
    }

    @Transactional(readOnly = true)
    public Page<AdminPaymentResponse> adminSearch(PaymentFilter filter, Pageable pageable) {
        return paymentRepository.findAll(PaymentSpecification.withFilter(filter), pageable)
                .map(paymentMapper::toAdminResponse);
    }

    private PaymentResponse processPayment(UUID bookingId, UUID buyerId, PayRequest payRequest, String idempotencyKey) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        assertBuyerOwnsBooking(booking, buyerId);
        assertBookingReadyForPayment(booking);

        if (paymentRepository.existsByBooking_Id(bookingId)) {
            Payment existing = paymentRepository.findByBooking_Id(bookingId).orElseThrow();
            return handleExistingPaymentOnPay(existing, buyerId);
        }

        PaymentProviderType providerType = resolveProviderType(payRequest);
        Offer offer = booking.getOffer();

        BigDecimal amount = offer.getTotalAmount();
        BigDecimal platformFee = offer.getPlatformFee();
        BigDecimal travellerPayout = offer.getItemPrice().add(offer.getTravellerFee());
        String currency = offer.getCurrency();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .buyer(booking.getBuyer())
                .traveller(booking.getTraveller())
                .amount(amount)
                .platformFee(platformFee)
                .travellerPayout(travellerPayout)
                .currency(currency)
                .paymentProvider(providerType)
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        PaymentProvider provider = paymentProviderFactory.getProvider(providerType);
        PaymentProviderResult result = provider.charge(PaymentProviderChargeRequest.builder()
                .bookingId(bookingId)
                .buyerId(buyerId)
                .travellerId(booking.getTraveller().getId())
                .amount(amount)
                .currency(currency)
                .idempotencyKey(idempotencyKey)
                .build());

        if (!result.isSuccess()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getFailureReason() != null
                    ? result.getFailureReason()
                    : "Payment provider failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment provider failed: " + payment.getFailureReason());
        }

        return paymentMapper.toResponse(completeSuccessfulPayment(payment, booking, result.getProviderPaymentId()));
    }

    private Payment completeSuccessfulPayment(Payment payment, Booking booking, String providerPaymentId) {
        payment.setStatus(PaymentStatus.HELD);
        payment.setProviderPaymentId(providerPaymentId);
        payment.setPaidAt(Instant.now());
        payment.setFailureReason(null);
        payment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PAYMENT_HELD);
        bookingRepository.save(booking);

        logPaymentHeld(payment);
        notificationService.notifyPaymentHeld(payment.getBuyer().getId(), payment.getTraveller().getId(), payment.getId());
        // TODO: audit log PAYMENT_HELD when audit module is available

        return payment;
    }

    private Payment executeRelease(Payment payment) {
        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new InvalidStatusException("Payment is not held");
        }

        Booking booking = payment.getBooking();
        if (!RELEASE_ELIGIBLE_BOOKING_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Booking is not eligible for payment release");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Booking is already completed");
        }

        PaymentProvider provider = paymentProviderFactory.getProvider(payment.getPaymentProvider());
        PaymentProviderResult result = provider.release(PaymentProviderReleaseRequest.builder()
                .paymentId(payment.getId())
                .providerPaymentId(payment.getProviderPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build());

        if (!result.isSuccess()) {
            throw new BadRequestException("Payment provider failed: "
                    + (result.getFailureReason() != null ? result.getFailureReason() : "release failed"));
        }

        Instant now = Instant.now();
        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(now);
        payment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(now);
        bookingRepository.save(booking);

        log.info("Payment released: paymentId={}, bookingId={}", payment.getId(), booking.getId());
        notificationService.notifyPaymentReleased(payment.getBuyer().getId(), payment.getTraveller().getId(), payment.getId());
        // TODO: audit log PAYMENT_RELEASED

        return payment;
    }

    private Payment executeRefund(Payment payment, String reason) {
        if (!REFUNDABLE_PAYMENT_STATUSES.contains(payment.getStatus())) {
            throw new InvalidStatusException("Payment cannot be refunded in status " + payment.getStatus());
        }

        Booking booking = payment.getBooking();
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Booking is already completed");
        }

        PaymentProvider provider = paymentProviderFactory.getProvider(payment.getPaymentProvider());
        PaymentProviderResult result = provider.refund(PaymentProviderRefundRequest.builder()
                .paymentId(payment.getId())
                .providerPaymentId(payment.getProviderPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reason(reason)
                .build());

        if (!result.isSuccess()) {
            throw new BadRequestException("Payment provider failed: "
                    + (result.getFailureReason() != null ? result.getFailureReason() : "refund failed"));
        }

        Instant now = Instant.now();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(now);
        payment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
        bookingRepository.save(booking);

        log.info("Payment refunded: paymentId={}, bookingId={}, reason={}", payment.getId(), booking.getId(), reason);
        notificationService.notifyPaymentRefunded(payment.getBuyer().getId(), payment.getTraveller().getId(), payment.getId());
        // TODO: audit log PAYMENT_REFUNDED

        return payment;
    }

    private PaymentResponse handleExistingPaymentOnPay(Payment payment, UUID buyerId) {
        assertBuyerOwnsPayment(payment, buyerId);

        return switch (payment.getStatus()) {
            case HELD, AUTHORIZED -> paymentMapper.toResponse(payment);
            case RELEASED -> throw new ConflictException("Payment is already released");
            case REFUNDED -> throw new ConflictException("Payment is already refunded");
            case FAILED -> throw new BadRequestException("Payment failed previously. Contact support.");
            case PENDING -> throw new ConflictException("Payment already exists for this booking");
        };
    }

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private void assertBuyerOwnsBooking(Booking booking, UUID buyerId) {
        if (!booking.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("Only buyer can pay for this booking");
        }
    }

    private void assertBuyerOwnsPayment(Payment payment, UUID buyerId) {
        if (!payment.getBuyer().getId().equals(buyerId)) {
            throw new OwnershipException("Only buyer can pay for this booking");
        }
    }

    private void assertBookingReadyForPayment(Booking booking) {
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new BadRequestException("Booking is not ready for payment");
        }
    }

    private void assertCanView(Payment payment, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!payment.getBuyer().getId().equals(userId) && !payment.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to this payment");
        }
    }

    private PaymentProviderType resolveProviderType(PayRequest payRequest) {
        if (payRequest == null || payRequest.getPaymentProvider() == null) {
            return DEFAULT_PROVIDER;
        }
        if (payRequest.getPaymentProvider() != PaymentProviderType.FAKE) {
            throw new BadRequestException("Payment provider not available: " + payRequest.getPaymentProvider());
        }
        return payRequest.getPaymentProvider();
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }

    private void logPaymentHeld(Payment payment) {
        log.info("Payment held in escrow: paymentId={}, bookingId={}, buyerId={}, travellerId={}, amount={} {}",
                payment.getId(), payment.getBooking().getId(), payment.getBuyer().getId(),
                payment.getTraveller().getId(), payment.getAmount(), payment.getCurrency());
    }
}