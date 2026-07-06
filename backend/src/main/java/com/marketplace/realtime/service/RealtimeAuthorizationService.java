package com.marketplace.realtime.service;

import com.marketplace.booking.entity.Booking;
import com.marketplace.booking.repository.BookingRepository;
import com.marketplace.chat.entity.ChatRoom;
import com.marketplace.chat.repository.ChatRoomRepository;
import com.marketplace.common.exception.OwnershipException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RealtimeAuthorizationService {

    private final BookingRepository bookingRepository;
    private final ChatRoomRepository chatRoomRepository;

    public void assertTrackingAccess(UUID bookingId, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        UUID userId = principal.getId();
        if (!booking.getBuyer().getId().equals(userId) && !booking.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You do not have access to live tracking for this booking");
        }
    }

    public void assertChatRoomAccess(UUID roomId, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        UUID userId = principal.getId();
        if (!room.getBuyer().getId().equals(userId) && !room.getTraveller().getId().equals(userId)) {
            throw new OwnershipException("You are not allowed to access this chat room");
        }
    }
}
