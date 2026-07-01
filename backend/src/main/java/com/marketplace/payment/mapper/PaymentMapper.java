package com.marketplace.payment.mapper;

import com.marketplace.payment.dto.AdminPaymentResponse;
import com.marketplace.payment.dto.PaymentResponse;
import com.marketplace.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .buyerId(payment.getBuyer().getId())
                .buyerName(payment.getBuyer().getFullName())
                .travellerId(payment.getTraveller().getId())
                .travellerName(payment.getTraveller().getFullName())
                .amount(payment.getAmount())
                .platformFee(payment.getPlatformFee())
                .travellerPayout(payment.getTravellerPayout())
                .currency(payment.getCurrency())
                .paymentProvider(payment.getPaymentProvider())
                .providerPaymentId(payment.getProviderPaymentId())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .releasedAt(payment.getReleasedAt())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public AdminPaymentResponse toAdminResponse(Payment payment) {
        return AdminPaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .buyerId(payment.getBuyer().getId())
                .buyerName(payment.getBuyer().getFullName())
                .buyerEmail(payment.getBuyer().getEmail())
                .travellerId(payment.getTraveller().getId())
                .travellerName(payment.getTraveller().getFullName())
                .travellerEmail(payment.getTraveller().getEmail())
                .amount(payment.getAmount())
                .platformFee(payment.getPlatformFee())
                .travellerPayout(payment.getTravellerPayout())
                .currency(payment.getCurrency())
                .paymentProvider(payment.getPaymentProvider())
                .providerPaymentId(payment.getProviderPaymentId())
                .idempotencyKey(payment.getIdempotencyKey())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .releasedAt(payment.getReleasedAt())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}