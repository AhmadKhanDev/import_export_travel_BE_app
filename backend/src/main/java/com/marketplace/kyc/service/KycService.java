package com.marketplace.kyc.service;

import com.marketplace.admin.dto.AdminKycResponse;
import com.marketplace.common.audit.AuditAction;
import com.marketplace.common.audit.service.AuditLogService;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ConflictException;
import com.marketplace.common.exception.InvalidStatusException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.kyc.dto.KycResponse;
import com.marketplace.kyc.dto.KycSubmitRequest;
import com.marketplace.kyc.entity.KycDocument;
import com.marketplace.kyc.entity.KycStatus;
import com.marketplace.kyc.mapper.KycMapper;
import com.marketplace.kyc.repository.KycDocumentRepository;
import com.marketplace.notification.service.NotificationService;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final KycMapper kycMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public KycResponse submit(UUID userId, KycSubmitRequest request) {
        KycDocument document = kycDocumentRepository.findFirstByUser_Id(userId).orElse(null);

        if (document != null) {
            if (document.getStatus() == KycStatus.PENDING_REVIEW) {
                throw new ConflictException("You already have a KYC submission pending review");
            }
            if (document.getStatus() == KycStatus.APPROVED) {
                throw new ConflictException("Your KYC is already approved");
            }
        } else {
            document = new KycDocument();
            document.setId(UUID.randomUUID());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            document.setUser(user);
        }

        document.setStatus(KycStatus.PENDING_REVIEW);
        document = kycDocumentRepository.save(document);

        auditLogService.log(AuditAction.KYC_SUBMITTED, "KYC", document.getId(), userId,
                "documentType=" + request.getDocumentType());

        UUID kycId = document.getId();
        userRepository.findByRole(Role.ADMIN)
                .forEach(admin -> notificationService.notifyKycSubmitted(admin.getId(), kycId));

        log.info("Traveller submitted KYC: kycId={}, userId={}", kycId, userId);
        return kycMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public KycResponse getMyKyc(UUID userId) {
        return kycDocumentRepository.findFirstByUser_Id(userId)
                .map(kycMapper::toResponse)
                .orElseGet(() -> KycResponse.builder()
                        .id(null)
                        .userId(userId)
                        .status(KycStatus.NOT_SUBMITTED)
                        .build());
    }

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

    @Transactional(readOnly = true)
    public Page<AdminKycResponse> listPending(Pageable pageable) {
        return kycDocumentRepository.findByStatus(KycStatus.PENDING_REVIEW, pageable)
                .map(kycMapper::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public Page<AdminKycResponse> listAll(KycStatus status, Pageable pageable) {
        if (status != null) {
            return kycDocumentRepository.findByStatus(status, pageable).map(kycMapper::toAdminResponse);
        }
        return kycDocumentRepository.findAll(pageable).map(kycMapper::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public AdminKycResponse getById(UUID kycId) {
        return kycMapper.toAdminResponse(getKyc(kycId));
    }

    @Transactional
    public AdminKycResponse approve(UUID kycId) {
        KycDocument document = getKyc(kycId);
        if (document.getStatus() == KycStatus.APPROVED) {
            return kycMapper.toAdminResponse(document);
        }
        if (document.getStatus() != KycStatus.PENDING_REVIEW) {
            throw new InvalidStatusException("Only pending KYC submissions can be approved");
        }

        document.setStatus(KycStatus.APPROVED);
        document = kycDocumentRepository.save(document);

        auditLogService.logAdminAction(AuditAction.ADMIN_KYC_APPROVED, "KYC", kycId,
                "userId=" + document.getUser().getId());
        notificationService.notifyKycApproved(document.getUser().getId(), kycId);

        log.info("Admin approved KYC: kycId={}, userId={}", kycId, document.getUser().getId());
        return kycMapper.toAdminResponse(document);
    }

    @Transactional
    public AdminKycResponse reject(UUID kycId, String reason) {
        KycDocument document = getKyc(kycId);
        if (document.getStatus() == KycStatus.REJECTED) {
            return kycMapper.toAdminResponse(document);
        }
        if (document.getStatus() != KycStatus.PENDING_REVIEW) {
            throw new InvalidStatusException("Only pending KYC submissions can be rejected");
        }

        document.setStatus(KycStatus.REJECTED);
        document = kycDocumentRepository.save(document);

        auditLogService.logAdminAction(AuditAction.ADMIN_KYC_REJECTED, "KYC", kycId,
                "userId=" + document.getUser().getId() + ",reason=" + reason);
        notificationService.notifyKycRejected(document.getUser().getId(), kycId, reason);

        log.info("Admin rejected KYC: kycId={}, userId={}", kycId, document.getUser().getId());
        return kycMapper.toAdminResponse(document);
    }

    private KycDocument getKyc(UUID kycId) {
        return kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
    }
}
