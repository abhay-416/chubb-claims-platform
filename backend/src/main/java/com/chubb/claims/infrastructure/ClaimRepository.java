package com.chubb.claims.infrastructure;

import com.chubb.claims.domain.Claim;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByClaimantId(Long claimantId);

    List<Claim> findByAssignedOfficerId(Long officerId);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByMarket(Market market);

    List<Claim> findByAssignedOfficerIsNull();

    @Query("SELECT c FROM Claim c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:market IS NULL OR c.market = :market) AND " +
           "(:claimantId IS NULL OR c.claimant.id = :claimantId) AND " +
           "(:officerId IS NULL OR c.assignedOfficer.id = :officerId)")
    List<Claim> filterClaims(@Param("status") ClaimStatus status,
                             @Param("market") Market market,
                             @Param("claimantId") Long claimantId,
                             @Param("officerId") Long officerId);

    // Outstanding liability sum: Open estimated amounts + approved amounts
    @Query("SELECT COALESCE(SUM(" +
           "CASE WHEN c.status IN ('SUBMITTED', 'ASSIGNED', 'UNDER_REVIEW', 'ADDITIONAL_INFO_REQUIRED') THEN c.estimatedAmount " +
           "     WHEN c.status = 'APPROVED' THEN c.approvedAmount " +
           "     ELSE 0 END), 0) FROM Claim c")
    BigDecimal calculateTotalOutstandingLiability();

    @Query("SELECT c.market, COALESCE(SUM(" +
           "CASE WHEN c.status IN ('SUBMITTED', 'ASSIGNED', 'UNDER_REVIEW', 'ADDITIONAL_INFO_REQUIRED') THEN c.estimatedAmount " +
           "     WHEN c.status = 'APPROVED' THEN c.approvedAmount " +
           "     ELSE 0 END), 0) FROM Claim c GROUP BY c.market")
    List<Object[]> calculateLiabilityByMarket();

    @Query("SELECT c.claimType, COALESCE(SUM(" +
           "CASE WHEN c.status IN ('SUBMITTED', 'ASSIGNED', 'UNDER_REVIEW', 'ADDITIONAL_INFO_REQUIRED') THEN c.estimatedAmount " +
           "     WHEN c.status = 'APPROVED' THEN c.approvedAmount " +
           "     ELSE 0 END), 0) FROM Claim c GROUP BY c.claimType")
    List<Object[]> calculateLiabilityByClaimType();

    long countByStatus(ClaimStatus status);

    long countByMarket(Market market);
}
