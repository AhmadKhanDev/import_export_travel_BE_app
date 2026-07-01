package com.marketplace.admin.service;

import com.marketplace.admin.dto.AdminReviewFilter;
import com.marketplace.admin.repository.AdminReviewSpecification;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.review.dto.ReviewResponse;
import com.marketplace.review.entity.Review;
import com.marketplace.review.mapper.ReviewMapper;
import com.marketplace.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> search(AdminReviewFilter filter, Pageable pageable) {
        AdminReviewFilter effectiveFilter = filter != null ? filter : AdminReviewFilter.builder().build();
        return reviewRepository.findAll(AdminReviewSpecification.withFilter(effectiveFilter), pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getById(UUID reviewId) {
        return reviewMapper.toResponse(getReview(reviewId));
    }

    @Transactional
    public void delete(UUID reviewId) {
        Review review = getReview(reviewId);
        reviewRepository.delete(review);
        auditLogService.logAdminAction(AuditAction.ADMIN_REVIEW_DELETED, "REVIEW", reviewId,
                "bookingId=" + review.getBooking().getId());
        log.info("Admin deleted review: reviewId={}", reviewId);
    }

    private Review getReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }
}
