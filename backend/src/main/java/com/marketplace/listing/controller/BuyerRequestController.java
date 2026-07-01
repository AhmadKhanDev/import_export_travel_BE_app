package com.marketplace.listing.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.listing.dto.BuyerRequestFilter;
import com.marketplace.listing.dto.BuyerRequestResponse;
import com.marketplace.listing.dto.CreateBuyerRequestRequest;
import com.marketplace.listing.dto.UpdateBuyerRequestRequest;
import com.marketplace.listing.entity.BuyerRequestStatus;
import com.marketplace.listing.service.BuyerRequestService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buyer-requests")
@RequiredArgsConstructor
@Tag(name = "Buyer Requests", description = "Buyer item request listings")
@SecurityRequirement(name = "bearerAuth")
public class BuyerRequestController {

    private final BuyerRequestService buyerRequestService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Create a buyer request as draft")
    public ResponseEntity<ApiResponse<BuyerRequestResponse>> create(
            @Valid @RequestBody CreateBuyerRequestRequest request) {
        BuyerRequestResponse response = buyerRequestService.create(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Buyer request created", response));
    }

    @GetMapping
    @Operation(summary = "Search published buyer requests with filters")
    public ResponseEntity<ApiResponse<PagedResponse<BuyerRequestResponse>>> search(
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) BuyerRequestStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        BuyerRequestFilter filter = BuyerRequestFilter.builder()
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .itemCategory(itemCategory)
                .status(status)
                .build();
        UserPrincipal viewer = SecurityUtils.getCurrentUser();
        Page<BuyerRequestResponse> page = buyerRequestService.search(filter, viewer, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "List logged-in buyer's own requests")
    public ResponseEntity<ApiResponse<PagedResponse<BuyerRequestResponse>>> myRequests(
            @RequestParam(required = false) String sourceCountry,
            @RequestParam(required = false) String sourceCity,
            @RequestParam(required = false) String destinationCountry,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) BuyerRequestStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        BuyerRequestFilter filter = BuyerRequestFilter.builder()
                .sourceCountry(sourceCountry)
                .sourceCity(sourceCity)
                .destinationCountry(destinationCountry)
                .destinationCity(destinationCity)
                .itemCategory(itemCategory)
                .status(status)
                .build();
        Page<BuyerRequestResponse> page = buyerRequestService.findMyRequests(
                SecurityUtils.getCurrentUserId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get buyer request details")
    public ResponseEntity<ApiResponse<BuyerRequestResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(buyerRequestService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Update own buyer request")
    public ResponseEntity<ApiResponse<BuyerRequestResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBuyerRequestRequest request) {
        BuyerRequestResponse response = buyerRequestService.update(id, request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Buyer request updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Delete own draft buyer request")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        buyerRequestService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Buyer request deleted", null));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Publish own draft buyer request")
    public ResponseEntity<ApiResponse<BuyerRequestResponse>> publish(@PathVariable UUID id) {
        BuyerRequestResponse response = buyerRequestService.publish(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Buyer request published", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Cancel own buyer request")
    public ResponseEntity<ApiResponse<BuyerRequestResponse>> cancel(@PathVariable UUID id) {
        BuyerRequestResponse response = buyerRequestService.cancel(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Buyer request cancelled", response));
    }
}