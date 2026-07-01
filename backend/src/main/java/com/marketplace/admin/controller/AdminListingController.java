package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminBuyerRequestResponse;
import com.marketplace.admin.dto.AdminTravellerTripResponse;
import com.marketplace.admin.service.AdminListingService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.listing.dto.BuyerRequestFilter;
import com.marketplace.listing.dto.TravellerTripFilter;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.entity.TravellerTripStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Listings", description = "Admin listing moderation")
@SecurityRequirement(name = "bearerAuth")
public class AdminListingController {

    private final AdminListingService adminListingService;

    @GetMapping("/api/v1/admin/buyer-requests")
    @Operation(summary = "List buyer requests")
    public ResponseEntity<ApiResponse<PagedResponse<AdminBuyerRequestResponse>>> searchBuyerRequests(
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(required = false) BuyerRequestStatus status,
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) String itemCategory,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        BuyerRequestFilter filter = BuyerRequestFilter.builder()
                .status(status)
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .itemCategory(itemCategory)
                .build();
        Page<AdminBuyerRequestResponse> page = adminListingService.searchBuyerRequests(buyerId, filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping("/api/v1/admin/buyer-requests/{id}/cancel")
    @Operation(summary = "Cancel a buyer request")
    public ResponseEntity<ApiResponse<AdminBuyerRequestResponse>> cancelBuyerRequest(
            @PathVariable UUID id,
            @Valid @RequestBody AdminActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Buyer request cancelled",
                adminListingService.cancelBuyerRequest(id, request)));
    }

    @GetMapping("/api/v1/admin/traveller-trips")
    @Operation(summary = "List traveller trips")
    public ResponseEntity<ApiResponse<PagedResponse<AdminTravellerTripResponse>>> searchTravellerTrips(
            @RequestParam(required = false) UUID travellerId,
            @RequestParam(required = false) TravellerTripStatus status,
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        TravellerTripFilter filter = TravellerTripFilter.builder()
                .status(status)
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .travelDateFrom(travelDateFrom)
                .travelDateTo(travelDateTo)
                .build();
        Page<AdminTravellerTripResponse> page = adminListingService.searchTravellerTrips(travellerId, filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping("/api/v1/admin/traveller-trips/{id}/cancel")
    @Operation(summary = "Cancel a traveller trip")
    public ResponseEntity<ApiResponse<AdminTravellerTripResponse>> cancelTravellerTrip(
            @PathVariable UUID id,
            @Valid @RequestBody AdminActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Traveller trip cancelled",
                adminListingService.cancelTravellerTrip(id, request)));
    }
}
