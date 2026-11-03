package com.chubb.claims.service;

import com.chubb.claims.api.dto.DashboardMetricsResponse;
import com.chubb.claims.api.dto.LiabilityExposureResponse;
import com.chubb.claims.domain.ClaimStatus;
import com.chubb.claims.domain.Market;
import com.chubb.claims.infrastructure.ClaimRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard & Liability Exposure Unit Tests")
class DashboardServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should calculate total liability exposure across open and approved claims")
    void shouldCalculateLiabilityExposure() {
        when(claimRepository.calculateTotalOutstandingLiability()).thenReturn(new BigDecimal("125000.00"));

        List<Object[]> marketRows = new ArrayList<>();
        marketRows.add(new Object[]{ Market.SINGAPORE, new BigDecimal("75000.00") });
        marketRows.add(new Object[]{ Market.HONG_KONG, new BigDecimal("50000.00") });
        when(claimRepository.calculateLiabilityByMarket()).thenReturn(marketRows);

        List<Object[]> typeRows = new ArrayList<>();
        typeRows.add(new Object[]{ "MOTOR", new BigDecimal("45000.00") });
        typeRows.add(new Object[]{ "PROPERTY", new BigDecimal("80000.00") });
        when(claimRepository.calculateLiabilityByClaimType()).thenReturn(typeRows);

        LiabilityExposureResponse response = dashboardService.getLiabilityExposure();

        assertNotNull(response);
        assertEquals(new BigDecimal("125000.00"), response.totalOutstandingLiability());
        assertEquals(new BigDecimal("75000.00"), response.marketExposure().get("SINGAPORE"));
        assertEquals(new BigDecimal("50000.00"), response.marketExposure().get("HONG_KONG"));
        assertEquals(BigDecimal.ZERO, response.marketExposure().get("AUSTRALIA")); // All 6 APAC markets accounted for
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
