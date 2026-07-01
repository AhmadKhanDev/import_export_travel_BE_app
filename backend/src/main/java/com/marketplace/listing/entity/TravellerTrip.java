package com.marketplace.listing.entity;

import com.marketplace.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "traveller_trips")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravellerTrip {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "traveller_id", nullable = false)
    private User traveller;

    @Column(name = "source_country", nullable = false)
    private String sourceCountry;

    @Column(name = "source_city", nullable = false)
    private String sourceCity;

    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(name = "destination_city", nullable = false)
    private String destinationCity;

    @Column(name = "travel_date", nullable = false)
    private Instant travelDate;

    @Column(name = "available_capacity_kg", precision = 10, scale = 2)
    private BigDecimal availableCapacityKg;

    @Column(name = "allowed_item_types", columnDefinition = "TEXT")
    private String allowedItemTypes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TravellerTripStatus status = TravellerTripStatus.DRAFT;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}