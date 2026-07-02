package com.marketplace.kyc.mapper;

import com.marketplace.admin.dto.AdminKycResponse;
import com.marketplace.kyc.dto.KycResponse;
import com.marketplace.kyc.entity.KycDocument;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class KycMapper {

    public AdminKycResponse toAdminResponse(KycDocument document) {
        User user = document.getUser();
        return AdminKycResponse.builder()
                .id(document.getId())
                .userId(user.getId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .userRole(user.getRole())
                .status(document.getStatus())
                .build();
    }

    public KycResponse toResponse(KycDocument document) {
        return KycResponse.builder()
                .id(document.getId())
                .userId(document.getUser().getId())
                .status(document.getStatus())
                .build();
    }
}
