package com.marketplace.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewFilter {

    private UUID reviewerId;
    private UUID revieweeId;
    private UUID bookingId;
    private Integer rating;
}
