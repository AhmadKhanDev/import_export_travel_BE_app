package com.marketplace.tracking.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.tracking.dto.TrackingLocationUpdateRequest;
import com.marketplace.tracking.dto.TrackingSessionResponse;
import com.marketplace.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings/{bookingId}/tracking")
@RequiredArgsConstructor
@Tag(name = "Live Tracking", description = "Traveller live location sharing during active bookings")
@SecurityRequirement(name = "bearerAuth")
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER', 'ADMIN')")
    @Operation(summary = "Get current live tracking state for a booking")
    public ResponseEntity<ApiResponse<TrackingSessionResponse>> getState(@PathVariable UUID bookingId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(
                trackingService.getTrackingState(bookingId, principal)));
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Start live location sharing for a booking")
    public ResponseEntity<ApiResponse<TrackingSessionResponse>> start(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Live tracking started",
                trackingService.startTracking(bookingId, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/stop")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Stop live location sharing for a booking")
    public ResponseEntity<ApiResponse<TrackingSessionResponse>> stop(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Live tracking stopped",
                trackingService.stopTracking(bookingId, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/location")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Update current live location for a booking")
    public ResponseEntity<ApiResponse<TrackingSessionResponse>> updateLocation(
            @PathVariable UUID bookingId,
            @Valid @RequestBody TrackingLocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                trackingService.updateLocation(bookingId, SecurityUtils.getCurrentUserId(), request)));
    }
}
