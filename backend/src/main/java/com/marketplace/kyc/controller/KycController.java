package com.marketplace.kyc.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.kyc.dto.KycResponse;
import com.marketplace.kyc.dto.KycSubmitRequest;
import com.marketplace.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Traveller KYC submission and status")
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Submit KYC documents for review")
    public ResponseEntity<ApiResponse<KycResponse>> submit(
            @Valid @RequestBody KycSubmitRequest request) {
        KycResponse response = kycService.submit(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("KYC submitted for review", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Get logged-in traveller's KYC status")
    public ResponseEntity<ApiResponse<KycResponse>> myKyc() {
        return ResponseEntity.ok(ApiResponse.success(kycService.getMyKyc(SecurityUtils.getCurrentUserId())));
    }
}
