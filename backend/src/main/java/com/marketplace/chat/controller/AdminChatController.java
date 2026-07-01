package com.marketplace.chat.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.chat.dto.AdminChatRoomResponse;
import com.marketplace.chat.dto.ChatMessageResponse;
import com.marketplace.chat.dto.ChatRoomFilter;
import com.marketplace.chat.entity.ChatRoomStatus;
import com.marketplace.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/chat/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Chat", description = "Admin chat review for support and disputes")
@SecurityRequirement(name = "bearerAuth")
public class AdminChatController {

    private final ChatService chatService;

    @GetMapping
    @Operation(summary = "List chat rooms with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminChatRoomResponse>>> search(
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(required = false) UUID travellerId,
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) ChatRoomStatus status,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ChatRoomFilter filter = ChatRoomFilter.builder()
                .buyerId(buyerId)
                .travellerId(travellerId)
                .bookingId(bookingId)
                .status(status)
                .build();
        Page<AdminChatRoomResponse> page = chatService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "View chat messages for support or dispute review")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessageResponse>>> getMessages(
            @PathVariable UUID roomId,
            @PageableDefault(size = 50, sort = "sentAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ChatMessageResponse> page = chatService.adminGetMessages(roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}
