package com.marketplace.dispute.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.dispute.dto.AdminDisputeResponse;
import com.marketplace.dispute.dto.DisputeFilter;
import com.marketplace.dispute.dto.DisputeResponse;
import com.marketplace.dispute.dto.RejectDisputeRequest;
import com.marketplace.dispute.dto.ResolveDisputeRequest;
import com.marketplace.dispute.entity.DisputeReason;
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
@RequestMapping("/api/v1/admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Disputes", description = "Admin dispute review and resolution")
@SecurityRequirement(name = "bearerAuth")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    @Operation(summary = "List disputes with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminDisputeResponse>>> search(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) UUID raisedByUserId,
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) DisputeReason reason,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        DisputeFilter filter = DisputeFilter.builder()
                .status(status)
                .raisedByUserId(raisedByUserId)
                .bookingId(bookingId)
                .reason(reason)
                .build();
        Page<AdminDisputeResponse> page = disputeService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dispute details")
    public ResponseEntity<ApiResponse<AdminDisputeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.adminGetById(id)));
    }

    @PostMapping("/{id}/mark-under-review")
    @Operation(summary = "Mark dispute as under review")
    public ResponseEntity<ApiResponse<DisputeResponse>> markUnderReview(@PathVariable UUID id) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        DisputeResponse response = disputeService.markUnderReview(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Dispute marked under review", response));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve a dispute")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDisputeRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        DisputeResponse response = disputeService.resolve(id, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved", response));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a dispute")
    public ResponseEntity<ApiResponse<DisputeResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectDisputeRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        DisputeResponse response = disputeService.reject(id, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Dispute rejected", response));
    }
}
