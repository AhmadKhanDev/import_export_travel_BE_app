package com.marketplace.offer.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.offer.dto.AdminOfferResponse;
import com.marketplace.offer.dto.OfferFilter;
import com.marketplace.offer.entity.OfferStatus;
import com.marketplace.offer.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Offers", description = "Admin offer management")
@SecurityRequirement(name = "bearerAuth")
public class AdminOfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = "Search all offers with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminOfferResponse>>> search(
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(required = false) UUID travellerId,
            @RequestParam(required = false) OfferStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OfferFilter filter = OfferFilter.builder()
                .buyerId(buyerId)
                .travellerId(travellerId)
                .status(status)
                .build();
        Page<AdminOfferResponse> page = offerService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}