package com.marketplace.listing.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.listing.dto.CreateTravellerTripRequest;
import com.marketplace.listing.dto.TravellerTripFilter;
import com.marketplace.listing.dto.TravellerTripResponse;
import com.marketplace.listing.dto.UpdateTravellerTripRequest;
import com.marketplace.listing.entity.TravellerTripStatus;
import com.marketplace.listing.service.TravellerTripService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/traveller-trips")
@RequiredArgsConstructor
@Tag(name = "Traveller Trips", description = "Traveller trip listings")
@SecurityRequirement(name = "bearerAuth")
public class TravellerTripController {

    private final TravellerTripService travellerTripService;

    @PostMapping
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Create a traveller trip as draft")
    public ResponseEntity<ApiResponse<TravellerTripResponse>> create(
            @Valid @RequestBody CreateTravellerTripRequest request) {
        TravellerTripResponse response = travellerTripService.create(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Traveller trip created", response));
    }

    @GetMapping
    @Operation(summary = "Search published traveller trips with filters")
    public ResponseEntity<ApiResponse<PagedResponse<TravellerTripResponse>>> search(
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) TravellerTripStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        TravellerTripFilter filter = TravellerTripFilter.builder()
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .status(status)
                .travelDateFrom(travelDateFrom)
                .travelDateTo(travelDateTo)
                .build();
        UserPrincipal viewer = SecurityUtils.getCurrentUser();
        Page<TravellerTripResponse> page = travellerTripService.search(filter, viewer, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "List logged-in traveller's own trips")
    public ResponseEntity<ApiResponse<PagedResponse<TravellerTripResponse>>> myTrips(
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) TravellerTripStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant travelDateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        TravellerTripFilter filter = TravellerTripFilter.builder()
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .status(status)
                .travelDateFrom(travelDateFrom)
                .travelDateTo(travelDateTo)
                .build();
        Page<TravellerTripResponse> page = travellerTripService.findMyTrips(
                SecurityUtils.getCurrentUserId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get traveller trip details")
    public ResponseEntity<ApiResponse<TravellerTripResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(travellerTripService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Update own traveller trip")
    public ResponseEntity<ApiResponse<TravellerTripResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTravellerTripRequest request) {
        TravellerTripResponse response = travellerTripService.update(id, request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Traveller trip updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Delete own draft traveller trip")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        travellerTripService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Traveller trip deleted", null));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Publish own draft traveller trip (requires approved KYC)")
    public ResponseEntity<ApiResponse<TravellerTripResponse>> publish(@PathVariable UUID id) {
        TravellerTripResponse response = travellerTripService.publish(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Traveller trip published", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Cancel own traveller trip")
    public ResponseEntity<ApiResponse<TravellerTripResponse>> cancel(@PathVariable UUID id) {
        TravellerTripResponse response = travellerTripService.cancel(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Traveller trip cancelled", response));
    }
}