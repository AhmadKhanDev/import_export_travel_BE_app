package com.marketplace.booking.controller;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminBookingDetailResponse;
import com.marketplace.admin.service.AdminBookingService;
import com.marketplace.booking.dto.AdminBookingResponse;
import com.marketplace.booking.dto.BookingFilter;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.service.BookingService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Bookings", description = "Admin booking management")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {

    private final BookingService bookingService;
    private final AdminBookingService adminBookingService;

    @GetMapping
    @Operation(summary = "Search all bookings with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminBookingResponse>>> search(
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(required = false) UUID travellerId,
            @RequestParam(required = false) UUID buyerRequestId,
            @RequestParam(required = false) UUID travellerTripId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        BookingFilter filter = BookingFilter.builder()
                .buyerId(buyerId)
                .travellerId(travellerId)
                .buyerRequestId(buyerRequestId)
                .travellerTripId(travellerTripId)
                .status(status)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .build();
        Page<AdminBookingResponse> page = bookingService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking detail with payment, dispute, and review summary")
    public ResponseEntity<ApiResponse<AdminBookingDetailResponse>> getDetail(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(adminBookingService.getDetail(bookingId)));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking (blocked if payment is held)")
    public ResponseEntity<ApiResponse<AdminBookingResponse>> cancel(
            @PathVariable UUID bookingId,
            @Valid @RequestBody AdminActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled",
                adminBookingService.cancel(bookingId, request)));
    }
}
