package com.chubb.claims.claim.api.dto;

import com.chubb.claims.claim.domain.ClaimHistory;
import com.chubb.claims.claim.domain.ClaimStatus;

import java.time.LocalDateTime;

public record ClaimHistoryItem(
    Long id,
    ClaimStatus oldStatus,
    ClaimStatus newStatus,
    String changedBy,
    String notes,
    LocalDateTime timestamp
) {
    public static ClaimHistoryItem fromEntity(ClaimHistory history) {
        return new ClaimHistoryItem(
            history.getId(),
            history.getOldStatus(),
            history.getNewStatus(),
            history.getChangedBy(),
            history.getNotes(),
            history.getTimestamp()
        );
    }
}
