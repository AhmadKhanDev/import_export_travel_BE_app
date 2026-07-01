package com.marketplace.verification.mapper;

import com.marketplace.verification.dto.DeliveryCodeStatusResponse;
import com.marketplace.verification.entity.DeliveryVerificationCode;
import org.springframework.stereotype.Component;

@Component
public class DeliveryVerificationMapper {

    public DeliveryCodeStatusResponse toStatusResponse(
            DeliveryVerificationCode code, boolean hasActiveCode) {
        return DeliveryCodeStatusResponse.builder()
                .bookingId(code.getBooking().getId())
                .hasActiveCode(hasActiveCode)
                .status(code.getStatus())
                .expiresAt(code.getExpiresAt())
                .verifiedAt(code.getVerifiedAt())
                .createdAt(code.getCreatedAt())
                .build();
    }
}
