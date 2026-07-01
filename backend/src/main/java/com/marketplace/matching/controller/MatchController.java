package com.marketplace.matching.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.common.security.SecurityUtils;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.matching.dto.MatchResponse;
import com.marketplace.matching.entity.MatchStatus;
import com.marketplace.matching.service.MatchingService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Match buyer requests with traveller trips")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchingService matchingService;

    @PostMapping("/generate/by-request/{buyerRequestId}")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Generate matches for a published buyer request")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> generateByBuyerRequest(
            @PathVariable UUID buyerRequestId) {
        List<MatchResponse> matches = matchingService.generateByBuyerRequest(
                buyerRequestId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Matches generated", matches));
    }

    @PostMapping("/generate/by-trip/{travellerTripId}")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "Generate matches for a published traveller trip (requires approved KYC)")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> generateByTravellerTrip(
            @PathVariable UUID travellerTripId) {
        List<MatchResponse> matches = matchingService.generateByTravellerTrip(
                travellerTripId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Matches generated", matches));
    }

    @GetMapping("/by-request/{buyerRequestId}")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "List matches for own buyer request")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> byBuyerRequest(
            @PathVariable UUID buyerRequestId,
            @RequestParam(required = false) MatchStatus status,
            @PageableDefault(size = 20, sort = "matchScore", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MatchResponse> page = matchingService.findByBuyerRequest(
                buyerRequestId, SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/by-trip/{travellerTripId}")
    @PreAuthorize("hasRole('TRAVELLER')")
    @Operation(summary = "List matches for own traveller trip")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> byTravellerTrip(
            @PathVariable UUID travellerTripId,
            @RequestParam(required = false) MatchStatus status,
            @PageableDefault(size = 20, sort = "matchScore", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MatchResponse> page = matchingService.findByTravellerTrip(
                travellerTripId, SecurityUtils.getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "Get match details (marks SUGGESTED as VIEWED for buyer/traveller)")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(@PathVariable UUID matchId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        MatchResponse response = matchingService.getById(matchId, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{matchId}/reject")
    @PreAuthorize("hasAnyRole('BUYER', 'TRAVELLER', 'ADMIN')")
    @Operation(summary = "Reject a match")
    public ResponseEntity<ApiResponse<MatchResponse>> reject(@PathVariable UUID matchId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        MatchResponse response = matchingService.reject(matchId, principal);
        return ResponseEntity.ok(ApiResponse.success("Match rejected", response));
    }
}