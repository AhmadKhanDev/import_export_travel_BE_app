package com.marketplace.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID bookingId;
    private UUID reviewerId;
    private String reviewerName;
    private UUID revieweeId;
    private String revieweeName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
