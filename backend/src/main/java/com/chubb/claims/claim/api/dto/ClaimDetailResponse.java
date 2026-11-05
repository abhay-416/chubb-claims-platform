package com.chubb.claims.claim.api.dto;

import java.util.List;

public record ClaimDetailResponse(
    ClaimResponse claim,
    List<ClaimHistoryItem> history,
    List<ClaimInfoRequestItem> infoRequests
) {}
