package com.chubb.claims.infrastructure;

import com.chubb.claims.domain.Claim;
import com.chubb.claims.domain.ClaimStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DomainEvents {

    public record ClaimEvent(
        String eventType,
        Long claimId,
        String claimNumber,
        String market,
        ClaimStatus oldStatus,
        ClaimStatus newStatus,
        BigDecimal amount,
        String triggeredBy,
        LocalDateTime timestamp
    ) implements Serializable {
        public static ClaimEvent created(Claim claim, String user) {
            return new ClaimEvent("CLAIM_CREATED", claim.getId(), claim.getClaimNumber(),
                claim.getMarket().name(), null, claim.getStatus(), claim.getEstimatedAmount(), user, LocalDateTime.now());
        }

        public static ClaimEvent assigned(Claim claim, String officerName) {
            return new ClaimEvent("CLAIM_ASSIGNED", claim.getId(), claim.getClaimNumber(),
                claim.getMarket().name(), ClaimStatus.SUBMITTED, ClaimStatus.ASSIGNED, claim.getEstimatedAmount(), officerName, LocalDateTime.now());
        }

        public static ClaimEvent statusChanged(Claim claim, ClaimStatus oldStatus, String user) {
            return new ClaimEvent("CLAIM_STATUS_CHANGED", claim.getId(), claim.getClaimNumber(),
                claim.getMarket().name(), oldStatus, claim.getStatus(), claim.getEstimatedAmount(), user, LocalDateTime.now());
        }

        public static ClaimEvent settled(Claim claim, String user) {
            return new ClaimEvent("CLAIM_SETTLED", claim.getId(), claim.getClaimNumber(),
                claim.getMarket().name(), ClaimStatus.APPROVED, ClaimStatus.SETTLED, claim.getApprovedAmount(), user, LocalDateTime.now());
        }
    }
}
