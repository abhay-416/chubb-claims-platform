package com.chubb.claims.claim.infrastructure;

import com.chubb.claims.claim.domain.ClaimInfoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimInfoRequestRepository extends JpaRepository<ClaimInfoRequest, Long> {
    List<ClaimInfoRequest> findByClaimIdOrderByCreatedAtDesc(Long claimId);
}
