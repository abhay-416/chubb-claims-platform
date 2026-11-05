package com.chubb.claims.claim.service;

import com.chubb.claims.claim.api.dto.*;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.ClaimType;
import com.chubb.claims.claim.domain.Market;

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
