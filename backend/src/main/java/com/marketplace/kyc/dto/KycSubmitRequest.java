package com.marketplace.kyc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycSubmitRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    private String documentUrl;
}
