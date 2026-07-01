package com.marketplace.review.repository;

import com.marketplace.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {

    boolean existsByBooking_IdAndReviewer_IdAndReviewee_Id(UUID bookingId, UUID reviewerId, UUID revieweeId);

    boolean existsByBooking_IdAndReviewer_Id(UUID bookingId, UUID reviewerId);

    Page<Review> findByReviewee_IdOrderByCreatedAtDesc(UUID revieweeId, Pageable pageable);

    Page<Review> findByReviewer_IdOrderByCreatedAtDesc(UUID reviewerId, Pageable pageable);

    List<Review> findByBooking_IdOrderByCreatedAtDesc(UUID bookingId);

    long countByReviewee_Id(UUID revieweeId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.reviewee.id = :revieweeId")
    Double averageRatingByRevieweeId(@Param("revieweeId") UUID revieweeId);

    long countByReviewee_IdAndRating(UUID revieweeId, int rating);
}
