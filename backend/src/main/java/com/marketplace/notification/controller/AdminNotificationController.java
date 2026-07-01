package com.marketplace.notification.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.notification.dto.AdminSendNotificationRequest;
import com.marketplace.notification.dto.NotificationFilter;
import com.marketplace.notification.dto.NotificationResponse;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationReferenceType;
import com.marketplace.notification.entity.NotificationStatus;
import com.marketplace.notification.entity.NotificationType;
import com.marketplace.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Notifications", description = "Admin notification management")
@SecurityRequirement(name = "bearerAuth")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List all notifications with filters")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> search(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationReferenceType referenceType,
            @RequestParam(required = false) UUID referenceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        NotificationFilter filter = NotificationFilter.builder()
                .userId(userId)
                .status(status)
                .channel(channel)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        Page<NotificationResponse> page = notificationService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping("/send")
    @Operation(summary = "Send a manual notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@Valid @RequestBody AdminSendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification sent", notificationService.sendManual(request)));
    }
}