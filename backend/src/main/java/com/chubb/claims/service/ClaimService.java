package com.chubb.claims.service;

import com.chubb.claims.api.dto.*;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.ClaimType;
import com.chubb.claims.domain.Market;

import java.util.List;

public interface ClaimService {
    ClaimResponse submitClaim(CreateClaimRequest request);
    List<ClaimResponse> listClaims(ClaimStatus status, Market market, ClaimType type, Boolean unassignedOnly);
    ClaimDetailResponse getClaimDetail(Long id);
    ClaimResponse assignClaim(Long id, Long officerId);
    ClaimResponse startReview(Long id);
    ClaimResponse requestAdditionalInfo(Long id, RequestInfoPayload payload);
    ClaimResponse submitAdditionalInfo(Long id, ProvideInfoPayload payload);
    ClaimResponse assessClaim(Long id, AssessClaimRequest request);
    ClaimResponse settleClaim(Long id, String settlementNotes);
}
