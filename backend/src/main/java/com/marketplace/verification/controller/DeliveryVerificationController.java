package com.marketplace.verification.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.verification.dto.DeliveryCodeStatusResponse;
import com.marketplace.verification.dto.DeliveryVerificationResultResponse;
import com.marketplace.verification.dto.GenerateDeliveryCodeResponse;
import com.marketplace.verification.dto.VerifyDeliveryCodeRequest;
import com.marketplace.verification.service.DeliveryVerificationService;
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
@RequestMapping("/api/v1/delivery-codes")
@RequiredArgsConstructor
@Tag(name = "Delivery Verification", description = "Secure delivery verification code flow")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryVerificationController {

    private final DeliveryVerificationService deliveryVerificationService;

    @PostMapping("/{bookingId}/generate")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Generate delivery verification code for a booking")
    public ResponseEntity<ApiResponse<GenerateDeliveryCodeResponse>> generate(@PathVariable UUID bookingId) {
        GenerateDeliveryCodeResponse response = deliveryVerificationService.generateCode(
                bookingId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Delivery verification code generated", response));
    }

    @PostMapping("/{bookingId}/verify")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Verify delivery code and release payment")
    public ResponseEntity<ApiResponse<DeliveryVerificationResultResponse>> verify(
            @PathVariable UUID bookingId,
            @Valid @RequestBody VerifyDeliveryCodeRequest request) {
        DeliveryVerificationResultResponse response = deliveryVerificationService.verifyCode(
                bookingId, SecurityUtils.getCurrentUserId(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Delivery verified", response));
    }

    @GetMapping("/{bookingId}/status")
    @Operation(summary = "Get delivery code status without returning plain code")
    public ResponseEntity<ApiResponse<DeliveryCodeStatusResponse>> status(@PathVariable UUID bookingId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        DeliveryCodeStatusResponse response = deliveryVerificationService.getStatus(bookingId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
