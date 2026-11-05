package com.chubb.claims.dashboard.service;

import com.chubb.claims.claim.domain.Claim;
import com.chubb.claims.claim.domain.ClaimStatus;
import com.chubb.claims.claim.domain.Market;
import com.chubb.claims.claim.infrastructure.ClaimRepository;
import com.chubb.claims.dashboard.dto.DashboardMetricsResponse;
import com.chubb.claims.dashboard.dto.LiabilityExposureResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard & Statutory Currency Liability Exposure Unit Tests")
class DashboardServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should calculate statutory currency liability exposure across all 6 APAC markets")
    void shouldCalculateMultiCurrencyLiabilityExposure() {
        List<Object[]> marketRows = new ArrayList<>();
        marketRows.add(new Object[]{ Market.SINGAPORE, "SGD", new BigDecimal("75000.00") });
        marketRows.add(new Object[]{ Market.HONG_KONG, "HKD", new BigDecimal("500000.00") });
        marketRows.add(new Object[]{ Market.AUSTRALIA, "AUD", new BigDecimal("95000.00") });
        when(claimRepository.calculateLiabilityByMarketAndCurrency()).thenReturn(marketRows);

        List<Object[]> typeRows = new ArrayList<>();
        typeRows.add(new Object[]{ "MOTOR", "SGD", new BigDecimal("45000.00") });
        typeRows.add(new Object[]{ "PROPERTY", "HKD", new BigDecimal("500000.00") });
        when(claimRepository.calculateLiabilityByClaimTypeAndCurrency()).thenReturn(typeRows);

        when(claimRepository.findByMarket(org.mockito.ArgumentMatchers.any(Market.class))).thenReturn(Collections.emptyList());

        LiabilityExposureResponse response = dashboardService.getLiabilityExposure();

        assertNotNull(response);
        assertEquals(6, response.marketLiabilities().size());

        var sgLiability = response.marketLiabilities().stream()
            .filter(m -> m.market() == Market.SINGAPORE).findFirst().orElseThrow();
        assertEquals("SGD", sgLiability.currency());
        assertEquals("S$", sgLiability.currencySymbol());
        assertEquals(new BigDecimal("75000.00"), sgLiability.outstandingAmount());

        var hkLiability = response.marketLiabilities().stream()
            .filter(m -> m.market() == Market.HONG_KONG).findFirst().orElseThrow();
        assertEquals("HKD", hkLiability.currency());
        assertEquals(new BigDecimal("500000.00"), hkLiability.outstandingAmount());
    }

    @Test
    @DisplayName("Should aggregate workload metrics correctly")
    void shouldAggregateWorkloadMetrics() {
        when(claimRepository.count()).thenReturn(100L);
        when(claimRepository.countByStatus(ClaimStatus.SETTLED)).thenReturn(40L);
        when(claimRepository.countByStatus(ClaimStatus.REJECTED)).thenReturn(10L);

        DashboardMetricsResponse response = dashboardService.getWorkloadMetrics();

        assertEquals(100L, response.totalClaims());
        assertEquals(40L, response.settledClaims());
        assertEquals(10L, response.rejectedClaims());
        assertEquals(50L, response.openClaims());
    }
}
