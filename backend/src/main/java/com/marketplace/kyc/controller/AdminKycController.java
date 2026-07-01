package com.marketplace.kyc.controller;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminKycResponse;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin KYC", description = "Admin KYC review")
@SecurityRequirement(name = "bearerAuth")
public class AdminKycController {

    private final KycService kycService;

    @GetMapping("/pending")
    @Operation(summary = "List pending KYC submissions")
    public ResponseEntity<ApiResponse<PagedResponse<AdminKycResponse>>> pending(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminKycResponse> page = kycService.listPending(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping
    @Operation(summary = "List KYC documents with optional status filter")
    public ResponseEntity<ApiResponse<PagedResponse<AdminKycResponse>>> list(
            @RequestParam(required = false) KycStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AdminKycResponse> page = kycService.listAll(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{kycId}")
    @Operation(summary = "Get KYC document by ID")
    public ResponseEntity<ApiResponse<AdminKycResponse>> getById(@PathVariable UUID kycId) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getById(kycId)));
    }

    @PostMapping("/{kycId}/approve")
    @Operation(summary = "Approve KYC submission")
    public ResponseEntity<ApiResponse<AdminKycResponse>> approve(@PathVariable UUID kycId) {
        return ResponseEntity.ok(ApiResponse.success("KYC approved", kycService.approve(kycId)));
    }

    @PostMapping("/{kycId}/reject")
    @Operation(summary = "Reject KYC submission")
    public ResponseEntity<ApiResponse<AdminKycResponse>> reject(
            @PathVariable UUID kycId,
            @Valid @RequestBody AdminActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("KYC rejected",
                kycService.reject(kycId, request.getReason().trim())));
    }
}
