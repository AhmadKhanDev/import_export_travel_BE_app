package com.marketplace.review.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.review.dto.CreateReviewRequest;
import com.marketplace.review.dto.RatingSummaryResponse;
import com.marketplace.review.dto.ReviewResponse;
import com.marketplace.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review and rating APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Create a review for a completed booking")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody CreateReviewRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        ReviewResponse response = reviewService.createReview(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Review created", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get paginated reviews received by a user")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getUserReviews(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> page = reviewService.getReviewsReceivedByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get reviews for a booking")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getBookingReviews(@PathVariable UUID bookingId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        List<ReviewResponse> reviews = reviewService.getReviewsByBooking(bookingId, principal);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/my-received")
    @Operation(summary = "Get reviews received by the logged-in user")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyReceivedReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<ReviewResponse> page = reviewService.getMyReceivedReviews(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/my-given")
    @Operation(summary = "Get reviews given by the logged-in user")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyGivenReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<ReviewResponse> page = reviewService.getMyGivenReviews(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get rating summary for a user")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(@PathVariable UUID userId) {
        RatingSummaryResponse summary = reviewService.getRatingSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
