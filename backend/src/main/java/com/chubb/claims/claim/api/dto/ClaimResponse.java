package com.chubb.claims.claim.api.dto;

import com.chubb.claims.claim.domain.Claim;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.ClaimType;
import com.chubb.claims.claim.domain.Market;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClaimResponse(
    Long id,
    String claimNumber,
    String policyNumber,
    ClaimType claimType,
    Market market,
    String currency,
    ClaimStatus status,
    LocalDate incidentDate,
    String incidentDescription,
    BigDecimal estimatedAmount,
    BigDecimal approvedAmount,
    Long claimantId,
    String claimantName,
    Long assignedOfficerId,
    String assignedOfficerName,
    Long version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ClaimResponse fromEntity(Claim claim) {
        return new ClaimResponse(
            claim.getId(),
            claim.getClaimNumber(),
            claim.getPolicyNumber(),
            claim.getClaimType(),
            claim.getMarket(),
            claim.getCurrency(),
            claim.getStatus(),
            claim.getIncidentDate(),
            claim.getIncidentDescription(),
            claim.getEstimatedAmount(),
            claim.getApprovedAmount(),
            claim.getClaimant() != null ? claim.getClaimant().getId() : null,
            claim.getClaimant() != null ? claim.getClaimant().getName() : null,
            claim.getAssignedOfficer() != null ? claim.getAssignedOfficer().getId() : null,
            claim.getAssignedOfficer() != null ? claim.getAssignedOfficer().getName() : null,
            claim.getVersion(),
            claim.getCreatedAt(),
            claim.getUpdatedAt()
        );
    }
}
