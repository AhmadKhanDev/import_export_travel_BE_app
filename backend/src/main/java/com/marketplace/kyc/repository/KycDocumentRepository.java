package com.marketplace.kyc.repository;

import com.marketplace.kyc.entity.KycDocument;
import com.marketplace.kyc.entity.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {

    boolean existsByUser_IdAndStatus(UUID userId, KycStatus status);
}