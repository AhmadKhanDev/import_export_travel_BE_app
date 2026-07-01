package com.marketplace.kyc.service;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.kyc.repository.KycDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;

    @Transactional(readOnly = true)
    public void requireApprovedKyc(UUID userId) {
        if (!hasApprovedKyc(userId)) {
            throw new BadRequestException("Traveller must have approved KYC before this action");
        }
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedKyc(UUID userId) {
        return kycDocumentRepository.existsByUser_IdAndStatus(userId, KycStatus.APPROVED);
    }
}