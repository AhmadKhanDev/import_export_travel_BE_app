package com.marketplace.admin.repository;

import com.marketplace.admin.dto.AdminReviewFilter;
import com.marketplace.review.entity.Review;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AdminReviewSpecification {

    private AdminReviewSpecification() {
    }

    public static Specification<Review> withFilter(AdminReviewFilter filter) {
        return Specification.where(byReviewerId(filter.getReviewerId()))
                .and(byRevieweeId(filter.getRevieweeId()))
                .and(byBookingId(filter.getBookingId()))
                .and(byRating(filter.getRating()));
    }

    private static Specification<Review> byReviewerId(UUID reviewerId) {
        if (reviewerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("reviewer").get("id"), reviewerId);
    }

    private static Specification<Review> byRevieweeId(UUID revieweeId) {
        if (revieweeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("reviewee").get("id"), revieweeId);
    }

    private static Specification<Review> byBookingId(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    private static Specification<Review> byRating(Integer rating) {
        if (rating == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("rating"), rating);
    }
}
