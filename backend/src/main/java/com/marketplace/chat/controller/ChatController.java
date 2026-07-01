package com.marketplace.chat.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.chat.dto.ChatMessageResponse;
import com.marketplace.chat.dto.ChatRoomResponse;
import com.marketplace.chat.dto.MarkReadResponse;
import com.marketplace.chat.dto.SendMessageRequest;
import com.marketplace.chat.dto.UnreadMessageCountResponse;
import com.marketplace.chat.entity.ChatRoomStatus;
import com.marketplace.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Booking-based chat between buyer and traveller")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Create or get chat room for a booking")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(@PathVariable UUID bookingId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        ChatRoomResponse response = chatService.createOrGetRoom(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Chat room ready", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Get chat rooms for the logged-in user")
    public ResponseEntity<ApiResponse<PagedResponse<ChatRoomResponse>>> getMyRooms(
            @RequestParam(required = false) ChatRoomStatus status,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Page<ChatRoomResponse> page = chatService.getMyRooms(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get chat room details")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoom(@PathVariable UUID roomId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        ChatRoomResponse response = chatService.getRoom(roomId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "Get paginated messages for a chat room")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> getMessages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "sentAt"));
        Page<ChatMessageResponse> messages = chatService.getMessages(roomId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(messages)));
    }

    @PostMapping("/{roomId}/messages")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Send a message to a chat room")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable UUID roomId,
            @Valid @RequestBody SendMessageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        ChatMessageResponse response = chatService.sendMessage(roomId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @PostMapping("/{roomId}/read")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Mark unread messages as read")
    public ResponseEntity<ApiResponse<MarkReadResponse>> markAsRead(@PathVariable UUID roomId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        MarkReadResponse response = chatService.markRoomMessagesAsRead(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", response));
    }

    @GetMapping("/{roomId}/unread-count")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER')")
    @Operation(summary = "Get unread message count for the logged-in user")
    public ResponseEntity<ApiResponse<UnreadMessageCountResponse>> getUnreadCount(@PathVariable UUID roomId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UnreadMessageCountResponse response = chatService.getUnreadCount(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close a chat room (admin only)")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> closeRoom(@PathVariable UUID roomId) {
        ChatRoomResponse response = chatService.closeRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Chat room closed", response));
    }
}
