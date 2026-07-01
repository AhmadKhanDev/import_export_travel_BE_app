package com.marketplace.admin.dto;

import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.user.entity.AccountStatus;
import com.marketplace.user.entity.Role;
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
public class AdminUserDetailResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private AccountStatus accountStatus;
    private boolean profileCompleted;
    private AdminProfileSummary profile;
    private KycStatus kycStatus;
    private long buyerRequestCount;
    private long travellerTripCount;
    private long bookingAsBuyerCount;
    private long bookingAsTravellerCount;
    private double averageRating;
    private long totalReviews;
    private Instant createdAt;
    private Instant updatedAt;
}
