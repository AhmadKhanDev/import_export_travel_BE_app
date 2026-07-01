package com.marketplace.chat.dto;

import com.marketplace.chat.entity.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private String message;

    @NotNull(message = "messageType is required")
    private MessageType messageType;

    private String attachmentUrl;
}
