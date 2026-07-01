package com.marketplace.chat.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.entity.BookingStatus;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.chat.dto.AdminChatRoomResponse;
import com.marketplace.chat.dto.ChatMessageResponse;
import com.marketplace.chat.dto.ChatRoomFilter;
import com.marketplace.chat.dto.ChatRoomResponse;
import com.marketplace.chat.dto.MarkReadResponse;
import com.marketplace.chat.dto.SendMessageRequest;
import com.marketplace.chat.dto.UnreadMessageCountResponse;
import com.marketplace.chat.entity.ChatMessage;
import com.marketplace.chat.entity.ChatRoom;
import com.marketplace.chat.entity.ChatRoomStatus;
import com.marketplace.chat.entity.MessageType;
import com.marketplace.chat.mapper.ChatMapper;
import com.marketplace.chat.repository.ChatMessageRepository;
import com.marketplace.chat.repository.ChatRoomRepository;
import com.marketplace.chat.repository.ChatRoomSpecification;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Set<BookingStatus> BLOCKED_BOOKING_STATUSES = EnumSet.of(
            BookingStatus.CANCELLED,
            BookingStatus.PENDING_OFFER,
            BookingStatus.OFFER_SENT,
            BookingStatus.ACCEPTED);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public ChatRoomResponse createOrGetRoom(UUID bookingId, UUID userId) {
        Booking booking = getBooking(bookingId);
        assertBookingEligibleForChat(booking);
        assertBookingParticipant(booking, userId);

        ChatRoom room = chatRoomRepository.findByBooking_Id(bookingId)
                .orElseGet(() -> createRoomFromBooking(booking));

        return toRoomResponse(room, userId);
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomResponse> getMyRooms(UUID userId, ChatRoomStatus status, Pageable pageable) {
        ChatRoomFilter filter = ChatRoomFilter.builder().status(status).build();
        return chatRoomRepository.findAll(ChatRoomSpecification.forParticipant(userId, filter), pageable)
                .map(room -> toRoomResponse(room, userId));
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(UUID roomId, UserPrincipal principal) {
        ChatRoom room = getChatRoom(roomId);
        assertCanAccessRoom(room, principal);
        UUID viewerId = principal.getId();
        return toRoomResponse(room, viewerId);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID roomId, UserPrincipal principal, Pageable pageable) {
        ChatRoom room = getChatRoom(roomId);
        assertCanAccessRoom(room, principal);
        return chatMessageRepository.findByChatRoom_Id(roomId, pageable)
                .map(chatMapper::toMessageResponse);
    }

    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, UUID senderId, SendMessageRequest request) {
        validateUserMessageRequest(request);

        ChatRoom room = getChatRoom(roomId);
        assertRoomParticipant(room, senderId);

        if (room.getStatus() == ChatRoomStatus.CLOSED) {
            throw new InvalidStatusException("Chat room is closed");
        }

        User sender = getUser(senderId);
        Instant now = Instant.now();

        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoom(room)
                .sender(sender)
                .message(resolveMessageText(request))
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .sentAt(now)
                .build();
        message = chatMessageRepository.save(message);

        room.setUpdatedAt(now);
        chatRoomRepository.save(room);

        UUID receiverId = resolveReceiverId(room, senderId);
        notificationService.notifyNewChatMessage(receiverId, sender.getFullName(), room.getBooking().getId(), room.getId());

        log.info("Chat message sent: roomId={}, senderId={}, messageType={}", roomId, senderId, request.getMessageType());

        return chatMapper.toMessageResponse(message);
    }

    @Transactional
    public MarkReadResponse markRoomMessagesAsRead(UUID roomId, UUID userId) {
        ChatRoom room = getChatRoom(roomId);
        assertRoomParticipant(room, userId);

        int updated = chatMessageRepository.markUnreadAsRead(roomId, userId, Instant.now());
        log.debug("Marked {} messages as read in room {} for user {}", updated, roomId, userId);
        return MarkReadResponse.builder().updatedCount(updated).build();
    }

    @Transactional(readOnly = true)
    public UnreadMessageCountResponse getUnreadCount(UUID roomId, UUID userId) {
        ChatRoom room = getChatRoom(roomId);
        assertRoomParticipant(room, userId);
        long count = chatMessageRepository.countUnreadForUser(roomId, userId);
        return UnreadMessageCountResponse.builder().unreadCount(count).build();
    }

    @Transactional
    public ChatRoomResponse closeRoom(UUID roomId) {
        ChatRoom room = getChatRoom(roomId);
        if (room.getStatus() == ChatRoomStatus.CLOSED) {
            return toRoomResponse(room, null);
        }
        room.setStatus(ChatRoomStatus.CLOSED);
        room = chatRoomRepository.save(room);
        saveSystemMessage(room, "Chat room has been closed by an administrator.");
        auditLogService.logAdminAction(AuditAction.ADMIN_CHAT_CLOSED, "CHAT_ROOM", roomId,
                "bookingId=" + room.getBooking().getId());
        log.info("Chat room closed: roomId={}, bookingId={}", roomId, room.getBooking().getId());
        return toRoomResponse(room, null);
    }

    @Transactional(readOnly = true)
    public Page<AdminChatRoomResponse> adminSearch(ChatRoomFilter filter, Pageable pageable) {
        return chatRoomRepository.findAll(ChatRoomSpecification.withFilter(filter), pageable)
                .map(this::toAdminRoomResponse);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> adminGetMessages(UUID roomId, Pageable pageable) {
        ChatRoom room = getChatRoom(roomId);
        return chatMessageRepository.findByChatRoom_Id(room.getId(), pageable)
                .map(chatMapper::toMessageResponse);
    }

    @Transactional
    public void createSystemMessage(UUID bookingId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            throw new BadRequestException("System message text must not be blank");
        }

        Booking booking = getBooking(bookingId);
        if (BLOCKED_BOOKING_STATUSES.contains(booking.getStatus())) {
            log.debug("Skipping system message for ineligible booking status: bookingId={}, status={}",
                    bookingId, booking.getStatus());
            return;
        }

        ChatRoom room = chatRoomRepository.findByBooking_Id(bookingId)
                .orElseGet(() -> createRoomFromBooking(booking));

        if (room.getStatus() == ChatRoomStatus.CLOSED) {
            log.warn("Skipping system message for closed chat room: bookingId={}", bookingId);
            return;
        }

        saveSystemMessage(room, messageText.trim());
    }

    private ChatRoom createRoomFromBooking(Booking booking) {
        ChatRoom room = ChatRoom.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .buyer(booking.getBuyer())
                .traveller(booking.getTraveller())
                .status(ChatRoomStatus.ACTIVE)
                .build();
        return chatRoomRepository.save(room);
    }

    private void saveSystemMessage(ChatRoom room, String messageText) {
        Instant now = Instant.now();
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoom(room)
                .sender(null)
                .message(messageText)
                .messageType(MessageType.SYSTEM)
                .sentAt(now)
                .build();
        chatMessageRepository.save(message);

        room.setUpdatedAt(now);
        chatRoomRepository.save(room);
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, UUID viewerId) {
        Optional<ChatMessage> lastMessage = chatMessageRepository.findTopByChatRoom_IdOrderBySentAtDesc(room.getId());
        String lastMessageText = lastMessage.map(ChatMessage::getMessage).orElse(null);
        Instant lastMessageAt = lastMessage.map(ChatMessage::getSentAt).orElse(null);
        long unreadCount = viewerId != null
                ? chatMessageRepository.countUnreadForUser(room.getId(), viewerId)
                : 0L;
        return chatMapper.toRoomResponse(room, lastMessageText, lastMessageAt, unreadCount);
    }

    private AdminChatRoomResponse toAdminRoomResponse(ChatRoom room) {
        Optional<ChatMessage> lastMessage = chatMessageRepository.findTopByChatRoom_IdOrderBySentAtDesc(room.getId());
        return chatMapper.toAdminRoomResponse(
                room,
                lastMessage.map(ChatMessage::getMessage).orElse(null),
                lastMessage.map(ChatMessage::getSentAt).orElse(null));
    }

    private void validateUserMessageRequest(SendMessageRequest request) {
        if (request.getMessageType() == MessageType.SYSTEM) {
            throw new BadRequestException("System messages cannot be sent by users");
        }
        if (request.getMessageType() == MessageType.TEXT) {
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                throw new BadRequestException("message must not be blank for TEXT messages");
            }
        }
        if (request.getMessageType() == MessageType.IMAGE) {
            if (request.getAttachmentUrl() == null || request.getAttachmentUrl().isBlank()) {
                throw new BadRequestException("Attachment URL is required for image messages");
            }
            validateAttachmentUrl(request.getAttachmentUrl());
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                request.setMessage("Image attachment");
            }
        }
    }

    private String resolveMessageText(SendMessageRequest request) {
        return request.getMessage().trim();
    }

    private void validateAttachmentUrl(String attachmentUrl) {
        try {
            URI uri = URI.create(attachmentUrl.trim());
            uri.toURL();
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("Attachment URL must be a valid HTTP or HTTPS URL");
            }
        } catch (IllegalArgumentException | MalformedURLException ex) {
            throw new BadRequestException("Attachment URL must be a valid HTTP or HTTPS URL");
        }
    }

    private UUID resolveReceiverId(ChatRoom room, UUID senderId) {
        if (room.getBuyer().getId().equals(senderId)) {
            return room.getTraveller().getId();
        }
        if (room.getTraveller().getId().equals(senderId)) {
            return room.getBuyer().getId();
        }
        throw new OwnershipException("Only booking participants can send messages");
    }

    private void assertBookingEligibleForChat(Booking booking) {
        if (BLOCKED_BOOKING_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Chat is not available for this booking status");
        }
    }

    private void assertBookingParticipant(Booking booking, UUID userId) {
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("Only booking participants can access this chat room");
        }
        if (booking.getBuyer().getId().equals(userId) && booking.getTraveller().getId().equals(userId)) {
            throw new BadRequestException("User cannot message themselves");
        }
    }

    private void assertRoomParticipant(ChatRoom room, UUID userId) {
        if (!room.getBuyer().getId().equals(userId) && !room.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("Only booking participants can send messages");
        }
    }

    private void assertCanAccessRoom(ChatRoom room, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        UUID userId = principal.getId();
        if (!room.getBuyer().getId().equals(userId) && !room.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You are not allowed to access this chat room");
        }
    }

    private ChatRoom getChatRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
