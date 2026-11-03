package com.chubb.claims.service;

import com.chubb.claims.api.dto.DashboardMetricsResponse;
import com.chubb.claims.api.dto.LiabilityExposureResponse;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.Market;
import com.chubb.claims.infrastructure.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ClaimRepository claimRepository;

    public DashboardService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    public DashboardMetricsResponse getWorkloadMetrics() {
        long totalClaims = claimRepository.count();
        long settledClaims = claimRepository.countByStatus(ClaimStatus.SETTLED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long openClaims = totalClaims - settledClaims - rejectedClaims;

        Map<String, Long> statusBreakdown = new HashMap<>();
        for (ClaimStatus status : ClaimStatus.values()) {
            statusBreakdown.put(status.name(), claimRepository.countByStatus(status));
        }

        Map<String, Long> marketBreakdown = new HashMap<>();
        for (Market market : Market.values()) {
            marketBreakdown.put(market.name(), claimRepository.countByMarket(market));
        }

        return new DashboardMetricsResponse(
            totalClaims,
            openClaims,
            settledClaims,
            rejectedClaims,
            statusBreakdown,
            marketBreakdown
        );
    }

    public LiabilityExposureResponse getLiabilityExposure() {
        BigDecimal totalLiability = claimRepository.calculateTotalOutstandingLiability();
        if (totalLiability == null) {
            totalLiability = BigDecimal.ZERO;
        }

        Map<String, BigDecimal> marketExposure = new HashMap<>();
        List<Object[]> marketResults = claimRepository.calculateLiabilityByMarket();
        for (Object[] row : marketResults) {
            Market market = (Market) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            marketExposure.put(market.name(), amount);
        }
        // Ensure all 6 markets are present even if 0
        for (Market m : Market.values()) {
            marketExposure.putIfAbsent(m.name(), BigDecimal.ZERO);
        }

        Map<String, BigDecimal> typeExposure = new HashMap<>();
        List<Object[]> typeResults = claimRepository.calculateLiabilityByClaimType();
        for (Object[] row : typeResults) {
            Object type = row[0];
            BigDecimal amount = (BigDecimal) row[1];
            typeExposure.put(type.toString(), amount);
        }

        return new LiabilityExposureResponse(totalLiability, marketExposure, typeExposure);
    }
}
