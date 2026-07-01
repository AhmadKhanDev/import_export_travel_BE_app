package com.marketplace.payment.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.payment.dto.PayRequest;
import com.marketplace.payment.dto.PaymentResponse;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment and escrow management")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingId}/pay")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Pay for a booking (simulated escrow hold)")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody(required = false) PayRequest request) {
        PaymentResponse response = paymentService.pay(
                bookingId, SecurityUtils.getCurrentUserId(), request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment successful, funds held in escrow", response));
    }

    @GetMapping("/my")
    @Operation(summary = "List payments where logged-in user is buyer or traveller")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> myPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> page = paymentService.findMyPayments(
                SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get payment by booking ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByBookingId(@PathVariable UUID bookingId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByBookingId(bookingId, principal)));
    }

    @PostMapping("/{paymentId}/release")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Release held payment to traveller (admin manual release after delivery)")
    public ResponseEntity<ApiResponse<PaymentResponse>> release(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.release(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment released", response));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a held or pending payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.refund(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded", response));
    }
}