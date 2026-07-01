package com.marketplace.dispute.dto;

import com.marketplace.dispute.entity.DisputeReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisputeRequest {

    @NotNull(message = "bookingId is required")
    private UUID bookingId;

    @NotNull(message = "reason is required")
    private DisputeReason reason;

    @NotBlank(message = "description must not be blank")
    private String description;
}
