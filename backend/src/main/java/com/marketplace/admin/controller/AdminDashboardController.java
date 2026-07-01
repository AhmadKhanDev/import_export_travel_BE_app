package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AdminDashboardSummaryResponse;
import com.marketplace.admin.service.AdminDashboardService;
import com.marketplace.common.audit.dto.AuditLogFilter;
import com.marketplace.common.audit.dto.AuditLogResponse;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Platform operations dashboard")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AuditLogService auditLogService;

    @GetMapping("/summary")
    @Operation(summary = "Get platform summary counts")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getSummary()));
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent audit log activity")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> recentActivity(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLogResponse> page = auditLogService.getAuditLogs(AuditLogFilter.builder().build(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}
