package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminRefundPaymentRequest;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.payment.dto.AdminPaymentResponse;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.mapper.PaymentMapper;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentOperationsService {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public AdminPaymentResponse getById(UUID paymentId) {
        return paymentMapper.toAdminResponse(getPayment(paymentId));
    }

    @Transactional
    public AdminPaymentResponse release(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        paymentService.release(paymentId);
        payment = getPayment(paymentId);

        auditLogService.logAdminAction(AuditAction.ADMIN_PAYMENT_RELEASED, "PAYMENT", paymentId,
                "bookingId=" + payment.getBooking().getId());
        log.info("Admin released payment: paymentId={}", paymentId);
        return paymentMapper.toAdminResponse(payment);
    }

    @Transactional
    public AdminPaymentResponse refund(UUID paymentId, AdminRefundPaymentRequest request) {
        Payment payment = getPayment(paymentId);
        paymentService.refundPaymentForBooking(payment.getBooking().getId(), request.getReason().trim());
        payment = getPayment(paymentId);

        auditLogService.logAdminAction(AuditAction.ADMIN_PAYMENT_REFUNDED, "PAYMENT", paymentId,
                "reason=" + request.getReason().trim() + ",bookingId=" + payment.getBooking().getId());
        log.info("Admin refunded payment: paymentId={}, reason={}", paymentId, request.getReason());
        return paymentMapper.toAdminResponse(payment);
    }

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
}
