package com.chubb.claims.dashboard.dto;

import com.chubb.claims.claim.domain.Market;
import java.math.BigDecimal;

public record MarketLiabilityDto(
    Market market,
    String currency,
    String currencySymbol,
    BigDecimal outstandingAmount,
    long openClaimsCount
) {}
