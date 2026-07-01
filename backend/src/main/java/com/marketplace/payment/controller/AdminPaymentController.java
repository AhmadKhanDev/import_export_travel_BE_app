package com.marketplace.payment.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.payment.dto.AdminPaymentResponse;
import com.marketplace.payment.dto.PaymentFilter;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.service.PaymentService;
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
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payments", description = "Admin payment management")
@SecurityRequirement(name = "bearerAuth")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Search all payments with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminPaymentResponse>>> search(
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(required = false) UUID travellerId,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PaymentFilter filter = PaymentFilter.builder()
                .bookingId(bookingId)
                .buyerId(buyerId)
                .travellerId(travellerId)
                .status(status)
                .build();
        Page<AdminPaymentResponse> page = paymentService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}