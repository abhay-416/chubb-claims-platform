package com.chubb.claims.claim.api;

import com.chubb.claims.claim.api.dto.*;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.ClaimType;
import com.chubb.claims.claim.domain.Market;
import com.chubb.claims.claim.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims", description = "Claims First Notice of Loss (FNOL) and Lifecycle Operations")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @Operation(summary = "Submit a new claim (FNOL)")
    public ResponseEntity<ClaimResponse> submitClaim(@Valid @RequestBody CreateClaimRequest request) {
        ClaimResponse response = claimService.submitClaim(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List and filter claims")
    public ResponseEntity<List<ClaimResponse>> listClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) Market market,
            @RequestParam(required = false) ClaimType type,
            @RequestParam(required = false) Boolean unassignedOnly) {
        return ResponseEntity.ok(claimService.listClaims(status, market, type, unassignedOnly));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed claim view with audit trail")
    public ResponseEntity<ClaimDetailResponse> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimDetail(id));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign claim to officer (Transition: SUBMITTED -> ASSIGNED)")
    public ResponseEntity<ClaimResponse> assignClaim(
            @PathVariable Long id,
            @RequestBody(required = false) Long officerId) {
        return ResponseEntity.ok(claimService.assignClaim(id, officerId));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Start claim investigation (Transition: ASSIGNED -> UNDER_REVIEW)")
    public ResponseEntity<ClaimResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.startReview(id));
    }

    @PostMapping("/{id}/request-info")
    @Operation(summary = "Request additional info from claimant (Transition: UNDER_REVIEW -> ADDITIONAL_INFO_REQUIRED)")
    public ResponseEntity<ClaimResponse> requestAdditionalInfo(
            @PathVariable Long id,
            @Valid @RequestBody RequestInfoPayload payload) {
        return ResponseEntity.ok(claimService.requestAdditionalInfo(id, payload));
    }

    @PostMapping("/{id}/submit-info")
    @Operation(summary = "Claimant provides requested information (Transition: ADDITIONAL_INFO_REQUIRED -> UNDER_REVIEW)")
    public ResponseEntity<ClaimResponse> submitAdditionalInfo(
            @PathVariable Long id,
            @Valid @RequestBody ProvideInfoPayload payload) {
        return ResponseEntity.ok(claimService.submitAdditionalInfo(id, payload));
    }

    @PostMapping("/{id}/assess")
    @Operation(summary = "Assess claim: Approve or Reject (Transition: UNDER_REVIEW -> APPROVED / REJECTED)")
    public ResponseEntity<ClaimResponse> assessClaim(
            @PathVariable Long id,
            @Valid @RequestBody AssessClaimRequest request) {
        return ResponseEntity.ok(claimService.assessClaim(id, request));
    }

    @PostMapping("/{id}/settle")
    @Operation(summary = "Settle approved claim payout (Transition: APPROVED -> SETTLED)")
    public ResponseEntity<ClaimResponse> settleClaim(
            @PathVariable Long id,
            @RequestBody(required = false) String settlementNotes) {
        return ResponseEntity.ok(claimService.settleClaim(id, settlementNotes));
    }
}
