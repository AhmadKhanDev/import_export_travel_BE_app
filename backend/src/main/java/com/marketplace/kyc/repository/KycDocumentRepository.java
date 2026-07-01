package com.marketplace.kyc.repository;

import com.marketplace.kyc.entity.KycDocument;
import com.marketplace.kyc.entity.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID>, JpaSpecificationExecutor<KycDocument> {

    boolean existsByUser_IdAndStatus(UUID userId, KycStatus status);

    long countByStatus(KycStatus status);

    List<KycDocument> findByUser_Id(UUID userId);

    Optional<KycDocument> findFirstByUser_Id(UUID userId);

    Page<KycDocument> findByStatus(KycStatus status, Pageable pageable);
}
