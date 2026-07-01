package com.marketplace.verification.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.verification.dto.DeliveryCodeStatusResponse;
import com.marketplace.verification.service.DeliveryVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/delivery-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Delivery Verification", description = "Admin delivery code management")
@SecurityRequirement(name = "bearerAuth")
public class AdminDeliveryVerificationController {

    private final DeliveryVerificationService deliveryVerificationService;

    @PostMapping("/{bookingId}/expire")
    @Operation(summary = "Expire the active delivery verification code for a booking")
    public ResponseEntity<ApiResponse<DeliveryCodeStatusResponse>> expire(@PathVariable UUID bookingId) {
        DeliveryCodeStatusResponse response = deliveryVerificationService.expireActiveCode(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Delivery verification code expired", response));
    }
}
