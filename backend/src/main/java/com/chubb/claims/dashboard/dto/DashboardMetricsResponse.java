package com.chubb.claims.dashboard.dto;

import java.util.Map;

public record DashboardMetricsResponse(
    long totalClaims,
    long openClaims,
    long settledClaims,
    long rejectedClaims,
    Map<String, Long> statusBreakdown,
    Map<String, Long> marketBreakdown
) {}
