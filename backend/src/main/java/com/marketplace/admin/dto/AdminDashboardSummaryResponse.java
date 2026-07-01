package com.marketplace.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private long totalUsers;
    private long totalBuyers;
    private long totalTravellers;
    private long totalAdmins;
    private long activeUsers;
    private long disabledUsers;
    private long pendingKycCount;
    private long approvedKycCount;
    private long rejectedKycCount;
    private long publishedBuyerRequests;
    private long publishedTravellerTrips;
    private long totalMatches;
    private long totalOffers;
    private long totalBookings;
    private long paymentPendingBookings;
    private long paymentHeldBookings;
    private long completedBookings;
    private long disputedBookings;
    private long totalPayments;
    private long heldPayments;
    private long releasedPayments;
    private long refundedPayments;
    private long openDisputes;
    private long underReviewDisputes;
    private long resolvedDisputes;
    private long totalReviews;
    private long totalChatRooms;
}
