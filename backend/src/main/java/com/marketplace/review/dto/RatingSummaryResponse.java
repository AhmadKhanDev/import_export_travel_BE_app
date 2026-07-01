package com.marketplace.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummaryResponse {

    private UUID userId;
    private double averageRating;
    private long totalReviews;
    private long fiveStarCount;
    private long fourStarCount;
    private long threeStarCount;
    private long twoStarCount;
    private long oneStarCount;
}
