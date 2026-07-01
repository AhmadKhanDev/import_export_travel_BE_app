package com.marketplace.notification.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.notification.dto.NotificationFilter;
import com.marketplace.notification.dto.NotificationResponse;
import com.marketplace.notification.dto.UnreadCountResponse;
import com.marketplace.notification.entity.NotificationChannel;
import com.marketplace.notification.entity.NotificationStatus;
import com.marketplace.notification.entity.NotificationType;
import com.marketplace.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    @Operation(summary = "List my notifications")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> myNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        NotificationFilter filter = NotificationFilter.builder()
                .status(status)
                .channel(channel)
                .type(type)
                .unreadOnly(unreadOnly)
                .build();
        Page<NotificationResponse> page = notificationService.getMyNotifications(SecurityUtils.getCurrentUserId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification detail")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(id, principal)));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Notification marked as read",
                notificationService.markAsRead(id, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all unread in-app notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead() {
        int count = notificationService.markAllAsRead(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read", Map.of("updatedCount", count)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(SecurityUtils.getCurrentUserId())));
    }
}