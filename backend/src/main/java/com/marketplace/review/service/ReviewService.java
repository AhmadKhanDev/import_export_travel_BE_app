package com.marketplace.review.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ConflictException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.review.dto.CreateReviewRequest;
import com.marketplace.review.dto.RatingSummaryResponse;
import com.marketplace.review.dto.ReviewResponse;
import com.marketplace.review.entity.Review;
import com.marketplace.review.mapper.ReviewMapper;
import com.marketplace.review.repository.ReviewRepository;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin cannot create reviews");
        }
        if (principal.getRole() != Role.BUYER && principal.getRole() != Role.TRAVELLER) {
            throw new BadRequestException("Only buyers and travellers can create reviews");
        }

        Booking booking = getBooking(request.getBookingId());
        assertParticipant(booking, principal.getId());

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidStatusException("Reviews can only be created for completed bookings");
        }

        User reviewer = getUser(principal.getId());
        User reviewee = resolveReviewee(booking, principal.getId());

        if (reviewer.getId().equals(reviewee.getId())) {
            throw new BadRequestException("You cannot review yourself");
        }

        if (reviewRepository.existsByBooking_IdAndReviewer_Id(booking.getId(), reviewer.getId())) {
            throw new ConflictException("You have already submitted a review for this booking");
        }

        if (reviewRepository.existsByBooking_IdAndReviewer_IdAndReviewee_Id(
                booking.getId(), reviewer.getId(), reviewee.getId())) {
            throw new ConflictException("You have already reviewed this user for this booking");
        }

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);

        log.info("Review created: reviewId={}, bookingId={}, reviewerId={}, revieweeId={}, rating={}",
                review.getId(), booking.getId(), reviewer.getId(), reviewee.getId(), review.getRating());

        notificationService.notifyReviewReceived(reviewee.getId(), review.getId());

        return reviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsReceivedByUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByReviewee_IdOrderByCreatedAtDesc(userId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByBooking(UUID bookingId, UserPrincipal principal) {
        Booking booking = getBooking(bookingId);
        assertCanViewBooking(booking, principal);
        return reviewRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReceivedReviews(UUID userId, Pageable pageable) {
        return getReviewsReceivedByUser(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyGivenReviews(UUID userId, Pageable pageable) {
        return reviewRepository.findByReviewer_IdOrderByCreatedAtDesc(userId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getRatingSummary(UUID userId) {
        getUser(userId);

        double average = reviewRepository.averageRatingByRevieweeId(userId);
        long total = reviewRepository.countByReviewee_Id(userId);

        return RatingSummaryResponse.builder()
                .userId(userId)
                .averageRating(Math.round(average * 100.0) / 100.0)
                .totalReviews(total)
                .fiveStarCount(reviewRepository.countByReviewee_IdAndRating(userId, 5))
                .fourStarCount(reviewRepository.countByReviewee_IdAndRating(userId, 4))
                .threeStarCount(reviewRepository.countByReviewee_IdAndRating(userId, 3))
                .twoStarCount(reviewRepository.countByReviewee_IdAndRating(userId, 2))
                .oneStarCount(reviewRepository.countByReviewee_IdAndRating(userId, 1))
                .build();
    }

    private User resolveReviewee(Booking booking, UUID reviewerId) {
        if (booking.getBuyer().getId().equals(reviewerId)) {
            return booking.getTraveller();
        }
        if (booking.getTraveller().getId().equals(reviewerId)) {
            return booking.getBuyer();
        }
        throw new OwnershipException("Only booking participants can create reviews");
    }

    private void assertParticipant(Booking booking, UUID userId) {
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("Only booking participants can create reviews");
        }
    }

    private void assertCanViewBooking(Booking booking, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to reviews for this booking");
        }
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
