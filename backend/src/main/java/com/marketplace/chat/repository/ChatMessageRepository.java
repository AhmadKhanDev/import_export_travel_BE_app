package com.marketplace.chat.repository;

import com.marketplace.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findByChatRoom_Id(UUID chatRoomId, Pageable pageable);

    Optional<ChatMessage> findTopByChatRoom_IdOrderBySentAtDesc(UUID chatRoomId);

    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.chatRoom.id = :roomId
              AND m.readAt IS NULL
              AND (m.sender IS NULL OR m.sender.id <> :userId)
            """)
    long countUnreadForUser(@Param("roomId") UUID roomId, @Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ChatMessage m
            SET m.readAt = :readAt
            WHERE m.chatRoom.id = :roomId
              AND m.readAt IS NULL
              AND (m.sender IS NULL OR m.sender.id <> :userId)
            """)
    int markUnreadAsRead(@Param("roomId") UUID roomId, @Param("userId") UUID userId, @Param("readAt") Instant readAt);
}
