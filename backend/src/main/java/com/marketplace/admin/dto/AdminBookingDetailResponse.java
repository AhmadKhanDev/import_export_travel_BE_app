package com.marketplace.admin.dto;

import com.marketplace.booking.dto.AdminBookingResponse;
import com.marketplace.dispute.entity.DisputeStatus;
import com.marketplace.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingDetailResponse {

    private AdminBookingResponse booking;
    private OfferSummary offer;
    private ParticipantSummary buyer;
    private ParticipantSummary traveller;
    private PaymentSummary payment;
    private DisputeSummary dispute;
    private ReviewSummary reviewSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferSummary {
        private UUID id;
        private BigDecimal totalAmount;
        private String currency;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantSummary {
        private UUID id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        private UUID id;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private Instant paidAt;
        private Instant releasedAt;
        private Instant refundedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisputeSummary {
        private UUID id;
        private DisputeStatus status;
        private String reason;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummary {
        private long reviewCount;
        private Double averageRating;
    }
}
