package com.marketplace.booking.controller;

import com.marketplace.booking.dto.BookingResponse;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.service.BookingService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/my")
    @Operation(summary = "List bookings where logged-in user is buyer or traveller")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> myBookings(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> page = bookingService.findMyBookings(
                SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(bookingService.getById(id, principal)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER', 'ADMIN')")
    @Operation(summary = "Cancel booking before payment is held")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        BookingResponse response = bookingService.cancel(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", response));
    }

    @PostMapping("/{id}/mark-in-transit")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Mark booking as in transit (requires PAYMENT_HELD)")
    public ResponseEntity<ApiResponse<BookingResponse>> markInTransit(@PathVariable UUID id) {
        BookingResponse response = bookingService.markInTransit(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Booking marked in transit", response));
    }

    @PostMapping("/{id}/mark-delivered")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Mark booking as delivered pending verification")
    public ResponseEntity<ApiResponse<BookingResponse>> markDelivered(@PathVariable UUID id) {
        BookingResponse response = bookingService.markDelivered(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Booking marked as delivered", response));
    }
}