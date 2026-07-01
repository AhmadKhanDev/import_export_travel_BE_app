package com.marketplace.matching.controller;

import com.marketplace.common.response.ApiResponse;
import com.marketplace.common.response.PagedResponse;
import com.marketplace.matching.dto.AdminMatchResponse;
import com.marketplace.matching.dto.MatchFilter;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/matches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Matches", description = "Admin match management")
@SecurityRequirement(name = "bearerAuth")
public class AdminMatchController {

    private final MatchingService matchingService;

    @GetMapping
    @Operation(summary = "Search all matches with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<AdminMatchResponse>>> search(
            @RequestParam(required = false) UUID buyerRequestId,
            @RequestParam(required = false) UUID travellerTripId,
            @RequestParam(required = false) MatchStatus status,
            @PageableDefault(size = 20, sort = "matchScore", direction = Sort.Direction.DESC) Pageable pageable) {
        MatchFilter filter = MatchFilter.builder()
                .buyerRequestId(buyerRequestId)
                .travellerTripId(travellerTripId)
                .status(status)
                .build();
        Page<AdminMatchResponse> page = matchingService.adminSearch(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(page)));
    }
}