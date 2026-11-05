package com.chubb.claims.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record LiabilityExposureResponse(
    String note,
    List<MarketLiabilityDto> marketLiabilities,
    Map<String, Map<String, BigDecimal>> typeExposureByCurrency
) {}
