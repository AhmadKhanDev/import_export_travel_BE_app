package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminDashboardSummaryResponse;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.chat.repository.ChatRoomRepository;
import com.marketplace.dispute.entity.DisputeStatus;
import com.marketplace.dispute.repository.DisputeRepository;
import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.kyc.repository.KycDocumentRepository;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.listing.repository.BuyerRequestRepository;
import com.marketplace.listing.repository.TravellerTripRepository;
import com.marketplace.matching.repository.MatchRepository;
import com.marketplace.offer.repository.OfferRepository;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.review.repository.ReviewRepository;
import com.marketplace.user.entity.AccountStatus;
import com.marketplace.user.entity.Role;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final BuyerRequestRepository buyerRequestRepository;
    private final TravellerTripRepository travellerTripRepository;
    private final MatchRepository matchRepository;
    private final OfferRepository offerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeRepository disputeRepository;
    private final ReviewRepository reviewRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        return AdminDashboardSummaryResponse.builder()
                .totalUsers(userRepository.count())
                .totalBuyers(userRepository.countByRole(Role.BUYER))
                .totalTravellers(userRepository.countByRole(Role.TRAVELLER))
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .activeUsers(userRepository.countByAccountStatus(AccountStatus.ACTIVE))
                .disabledUsers(userRepository.countByAccountStatus(AccountStatus.DISABLED))
                .pendingKycCount(kycDocumentRepository.countByStatus(KycStatus.PENDING_REVIEW))
                .approvedKycCount(kycDocumentRepository.countByStatus(KycStatus.APPROVED))
                .rejectedKycCount(kycDocumentRepository.countByStatus(KycStatus.REJECTED))
                .publishedBuyerRequests(buyerRequestRepository.countByStatus(BuyerRequestStatus.PUBLISHED))
                .publishedTravellerTrips(travellerTripRepository.countByStatus(TravellerTripStatus.PUBLISHED))
                .totalMatches(matchRepository.count())
                .totalOffers(offerRepository.count())
                .totalBookings(bookingRepository.count())
                .paymentPendingBookings(bookingRepository.countByStatus(BookingStatus.PAYMENT_PENDING))
                .paymentHeldBookings(bookingRepository.countByStatus(BookingStatus.PAYMENT_HELD))
                .completedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED))
                .disputedBookings(bookingRepository.countByStatus(BookingStatus.DISPUTED))
                .totalPayments(paymentRepository.count())
                .heldPayments(paymentRepository.countByStatus(PaymentStatus.HELD))
                .releasedPayments(paymentRepository.countByStatus(PaymentStatus.RELEASED))
                .refundedPayments(paymentRepository.countByStatus(PaymentStatus.REFUNDED))
                .openDisputes(disputeRepository.countByStatus(DisputeStatus.OPEN))
                .underReviewDisputes(disputeRepository.countByStatus(DisputeStatus.UNDER_REVIEW))
                .resolvedDisputes(disputeRepository.countByStatus(DisputeStatus.RESOLVED))
                .totalReviews(reviewRepository.count())
                .totalChatRooms(chatRoomRepository.count())
                .build();
    }
}
