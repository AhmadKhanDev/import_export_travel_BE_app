package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AdminActionReasonRequest;
import com.marketplace.admin.dto.AdminChangeRoleRequest;
import com.marketplace.admin.dto.AdminUserDetailResponse;
import com.marketplace.admin.dto.AdminUserFilter;
import com.marketplace.admin.dto.AdminUserResponse;
import com.marketplace.admin.service.AdminUserService;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.user.entity.AccountStatus;
import com.marketplace.user.entity.Role;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List users with filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> search(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) AccountStatus accountStatus,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Boolean profileCompleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        AdminUserFilter filter = AdminUserFilter.builder()
                .role(role)
                .accountStatus(accountStatus)
                .email(email)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .profileCompleted(profileCompleted)
                .build();
        Page<AdminUserResponse> page = adminUserService.search(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get detailed user info")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getDetail(userId)));
    }

    @PostMapping("/{userId}/disable")
    @Operation(summary = "Disable a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> disable(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User disabled", adminUserService.disable(userId, request)));
    }

    @PostMapping("/{userId}/enable")
    @Operation(summary = "Enable a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> enable(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("User enabled", adminUserService.enable(userId)));
    }

    @PostMapping("/{userId}/change-role")
    @Operation(summary = "Change user role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changeRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminChangeRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role updated", adminUserService.changeRole(userId, request)));
    }
}
