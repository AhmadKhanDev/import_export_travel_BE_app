package com.marketplace.dispute.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.dispute.dto.CreateDisputeRequest;
import com.marketplace.dispute.dto.DisputeResponse;
import com.marketplace.dispute.entity.DisputeStatus;
import com.marketplace.dispute.service.DisputeService;
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
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
@Tag(name = "Disputes", description = "Dispute management for buyers and travellers")
@SecurityRequirement(name = "bearerAuth")
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Create a dispute for a booking")
    public ResponseEntity<ApiResponse<DisputeResponse>> create(@Valid @RequestBody CreateDisputeRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        DisputeResponse response = disputeService.createDispute(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Dispute created", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Get disputes raised by or linked to the logged-in user")
    public ResponseEntity<ApiResponse<PagedResponse<DisputeResponse>>> getMyDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<DisputeResponse> page = disputeService.getMyDisputes(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER', 'ADMIN')")
    @Operation(summary = "Get dispute details")
    public ResponseEntity<ApiResponse<DisputeResponse>> getById(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        DisputeResponse response = disputeService.getById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
