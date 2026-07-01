package com.marketplace.chat.repository;

import com.marketplace.chat.entity.ChatRoom;
import com.marketplace.chat.entity.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID>, JpaSpecificationExecutor<ChatRoom> {

    Optional<ChatRoom> findByBooking_Id(UUID bookingId);

    boolean existsByBooking_Id(UUID bookingId);

    long countByStatus(ChatRoomStatus status);
}
