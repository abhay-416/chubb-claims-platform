package com.chubb.claims.dashboard.service;

import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.Market;
import com.chubb.claims.claim.infrastructure.ClaimRepository;
import com.chubb.claims.dashboard.dto.DashboardMetricsResponse;
import com.chubb.claims.dashboard.dto.LiabilityExposureResponse;
import com.chubb.claims.dashboard.dto.MarketLiabilityDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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
        // Multi-currency calculation across 6 APAC markets
        Map<Market, BigDecimal> marketAmounts = new EnumMap<>(Market.class);
        Map<Market, String> marketCurrencies = new EnumMap<>(Market.class);

        // Initialize all 6 markets
        for (Market m : Market.values()) {
            marketAmounts.put(m, BigDecimal.ZERO);
            marketCurrencies.put(m, m.getDefaultCurrency());
        }

        List<Object[]> results = claimRepository.calculateLiabilityByMarketAndCurrency();
        for (Object[] row : results) {
            Market market = (Market) row[0];
            String currency = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            marketAmounts.put(market, amount);
            marketCurrencies.put(market, currency);
        }

        List<MarketLiabilityDto> marketLiabilities = new ArrayList<>();
        for (Market m : Market.values()) {
            long openCount = claimRepository.findByMarket(m).stream()
                .filter(c -> c.getStatus() != ClaimStatus.SETTLED && c.getStatus() != ClaimStatus.REJECTED)
                .count();

            marketLiabilities.add(new MarketLiabilityDto(
                m,
                marketCurrencies.get(m),
                m.getCurrencySymbol(),
                marketAmounts.get(m),
                openCount
            ));
        }

        // Type breakdown by currency
        Map<String, Map<String, BigDecimal>> typeExposure = new HashMap<>();
        List<Object[]> typeResults = claimRepository.calculateLiabilityByClaimTypeAndCurrency();
        for (Object[] row : typeResults) {
            String type = row[0].toString();
            String currency = row[1].toString();
            BigDecimal amount = (BigDecimal) row[2];

            typeExposure.computeIfAbsent(type, k -> new HashMap<>()).put(currency, amount);
        }

        String note = "Outstanding liability is calculated per statutory jurisdiction currency to prevent inaccurate FX cross-currency distortion.";

        return new LiabilityExposureResponse(note, marketLiabilities, typeExposure);
    }
}
