package com.marketplace.dispute.mapper;

import com.marketplace.dispute.dto.AdminDisputeResponse;
import com.marketplace.dispute.dto.DisputeResponse;
import com.marketplace.dispute.entity.Dispute;
import com.marketplace.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DisputeMapper {

    public DisputeResponse toResponse(Dispute dispute) {
        User raisedBy = dispute.getRaisedByUser();
        User buyer = dispute.getBooking().getBuyer();
        User traveller = dispute.getBooking().getTraveller();
        User resolvedByAdmin = dispute.getResolvedByAdmin();

        return DisputeResponse.builder()
                .id(dispute.getId())
                .bookingId(dispute.getBooking().getId())
                .raisedByUserId(raisedBy.getId())
                .raisedByUserName(raisedBy.getFullName())
                .buyerId(buyer.getId())
                .buyerName(buyer.getFullName())
                .travellerId(traveller.getId())
                .travellerName(traveller.getFullName())
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .status(dispute.getStatus())
                .resolvedByAdminId(resolvedByAdmin != null ? resolvedByAdmin.getId() : null)
                .resolvedByAdminName(resolvedByAdmin != null ? resolvedByAdmin.getFullName() : null)
                .resolutionNote(dispute.getResolutionNote())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }

    public AdminDisputeResponse toAdminResponse(Dispute dispute) {
        User raisedBy = dispute.getRaisedByUser();
        User buyer = dispute.getBooking().getBuyer();
        User traveller = dispute.getBooking().getTraveller();
        User resolvedByAdmin = dispute.getResolvedByAdmin();

        return AdminDisputeResponse.builder()
                .id(dispute.getId())
                .bookingId(dispute.getBooking().getId())
                .raisedByUserId(raisedBy.getId())
                .raisedByUserName(raisedBy.getFullName())
                .raisedByUserEmail(raisedBy.getEmail())
                .buyerId(buyer.getId())
                .buyerName(buyer.getFullName())
                .buyerEmail(buyer.getEmail())
                .travellerId(traveller.getId())
                .travellerName(traveller.getFullName())
                .travellerEmail(traveller.getEmail())
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .status(dispute.getStatus())
                .resolvedByAdminId(resolvedByAdmin != null ? resolvedByAdmin.getId() : null)
                .resolvedByAdminName(resolvedByAdmin != null ? resolvedByAdmin.getFullName() : null)
                .resolutionNote(dispute.getResolutionNote())
                .createdAt(dispute.getCreatedAt())
                .resolvedAt(dispute.getResolvedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }
}
