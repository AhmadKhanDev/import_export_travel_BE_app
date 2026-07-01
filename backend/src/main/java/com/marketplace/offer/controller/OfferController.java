package com.marketplace.offer.controller;

import com.marketplace.booking.dto.BookingResponse;
import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.offer.dto.CreateOfferRequest;
import com.marketplace.offer.dto.OfferResponse;
import com.marketplace.offer.entity.OfferStatus;
import com.marketplace.offer.service.OfferService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Tag(name = "Offers", description = "Traveller offers to buyers")
@SecurityRequirement(name = "bearerAuth")
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Send an offer to a buyer (requires approved KYC)")
    public ResponseEntity<ApiResponse<OfferResponse>> create(@Valid @RequestBody CreateOfferRequest request) {
        OfferResponse response = offerService.create(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offer sent", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get offer details")
    public ResponseEntity<ApiResponse<OfferResponse>> getById(@PathVariable UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(offerService.getById(id, principal)));
    }

    @GetMapping("/my")
    @Operation(summary = "List offers where logged-in user is buyer or traveller")
    public ResponseEntity<ApiResponse<PagedResponse<OfferResponse>>> myOffers(
            @RequestParam(required = false) OfferStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OfferResponse> page = offerService.findMyOffers(SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/by-request/{buyerRequestId}")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "List offers for own buyer request")
    public ResponseEntity<ApiResponse<PagedResponse<OfferResponse>>> byBuyerRequest(
            @PathVariable UUID buyerRequestId,
            @RequestParam(required = false) OfferStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OfferResponse> page = offerService.findByBuyerRequest(
                buyerRequestId, SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/by-trip/{travellerTripId}")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "List offers for own traveller trip")
    public ResponseEntity<ApiResponse<PagedResponse<OfferResponse>>> byTravellerTrip(
            @PathVariable UUID travellerTripId,
            @RequestParam(required = false) OfferStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OfferResponse> page = offerService.findByTravellerTrip(
                travellerTripId, SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Accept offer and create booking with PAYMENT_PENDING status")
    public ResponseEntity<ApiResponse<BookingResponse>> accept(@PathVariable UUID id) {
        BookingResponse response = offerService.accept(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Offer accepted, booking created", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Reject a sent offer")
    public ResponseEntity<ApiResponse<OfferResponse>> reject(@PathVariable UUID id) {
        OfferResponse response = offerService.reject(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Offer rejected", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Cancel a sent offer")
    public ResponseEntity<ApiResponse<OfferResponse>> cancel(@PathVariable UUID id) {
        OfferResponse response = offerService.cancel(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Offer cancelled", response));
    }
}