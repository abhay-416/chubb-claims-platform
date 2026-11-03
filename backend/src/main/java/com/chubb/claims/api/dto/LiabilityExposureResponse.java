package com.chubb.claims.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record LiabilityExposureResponse(
    BigDecimal totalOutstandingLiability,
    Map<String, BigDecimal> marketExposure,
    Map<String, BigDecimal> typeExposure
) {}
