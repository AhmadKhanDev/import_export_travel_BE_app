package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AdminReviewFilter;
import com.marketplace.admin.service.AdminReviewService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Reviews", description = "Admin review moderation")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    @Operation(summary = "List reviews with filters")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> search(
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) UUID revieweeId,
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        AdminReviewFilter filter = AdminReviewFilter.builder()
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .bookingId(bookingId)
                .rating(rating)
                .build();
        Page<ReviewResponse> page = adminReviewService.search(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(adminReviewService.getById(reviewId)));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID reviewId) {
        adminReviewService.delete(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
