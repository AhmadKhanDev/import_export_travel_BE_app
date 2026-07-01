package com.marketplace.chat.repository;

import com.marketplace.chat.dto.ChatRoomFilter;
import com.marketplace.chat.entity.ChatRoom;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ChatRoomSpecification {

    private ChatRoomSpecification() {
    }

    public static Specification<ChatRoom> withFilter(ChatRoomFilter filter) {
        return Specification.where(byBuyerId(filter.getBuyerId()))
                .and(byTravellerId(filter.getTravellerId()))
                .and(byBookingId(filter.getBookingId()))
                .and(byStatus(filter.getStatus()));
    }

    public static Specification<ChatRoom> forParticipant(UUID userId, ChatRoomFilter filter) {
        Specification<ChatRoom> participantSpec = (root, query, cb) -> cb.or(
                cb.equal(root.get("buyer").get("id"), userId),
                cb.equal(root.get("traveller").get("id"), userId)
        );
        return participantSpec.and(withFilter(filter));
    }

    private static Specification<ChatRoom> byBuyerId(UUID buyerId) {
        if (buyerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("buyer").get("id"), buyerId);
    }

    private static Specification<ChatRoom> byTravellerId(UUID travellerId) {
        if (travellerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("traveller").get("id"), travellerId);
    }

    private static Specification<ChatRoom> byBookingId(UUID bookingId) {
        if (bookingId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    private static Specification<ChatRoom> byStatus(com.marketplace.chat.entity.ChatRoomStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
