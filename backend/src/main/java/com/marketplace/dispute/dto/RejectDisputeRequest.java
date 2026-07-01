package com.marketplace.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectDisputeRequest {

    @NotBlank(message = "resolutionNote must not be blank")
    private String resolutionNote;
}
