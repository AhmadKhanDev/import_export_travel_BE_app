package com.marketplace.admin.dto;

import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminKycResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private Role userRole;
    private KycStatus status;
}
