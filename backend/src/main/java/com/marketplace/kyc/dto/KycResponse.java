package com.marketplace.kyc.dto;

import com.marketplace.kyc.entity.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {

    private UUID id;
    private UUID userId;
    private KycStatus status;
}
