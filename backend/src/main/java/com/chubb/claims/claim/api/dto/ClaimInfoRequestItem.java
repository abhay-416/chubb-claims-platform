package com.chubb.claims.claim.api.dto;

import com.chubb.claims.claim.domain.ClaimInfoRequest;

import java.time.LocalDateTime;

public record ClaimInfoRequestItem(
    Long id,
    String requestedInfo,
    String requestedBy,
    String response,
    LocalDateTime respondedAt,
    String status,
    LocalDateTime createdAt
) {
    public static ClaimInfoRequestItem fromEntity(ClaimInfoRequest request) {
        return new ClaimInfoRequestItem(
            request.getId(),
            request.getRequestedInfo(),
            request.getRequestedBy(),
            request.getResponse(),
            request.getRespondedAt(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
