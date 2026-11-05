package com.chubb.claims.claim.api.dto;

import com.chubb.claims.claim.domain.ClaimType;
import com.chubb.claims.claim.domain.Market;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
    @NotBlank(message = "Policy number is required")
    String policyNumber,

    @NotNull(message = "Claim type is required (MOTOR or PROPERTY)")
    ClaimType claimType,

    @NotNull(message = "Market is required (e.g. SINGAPORE, HONG_KONG, etc.)")
    Market market,

    String currency, // Optional: defaults to market's native currency (e.g. SGD, HKD, AUD)

    @NotNull(message = "Incident date is required")
    LocalDate incidentDate,

    @NotBlank(message = "Incident description is required")
    String incidentDescription,

    @NotNull(message = "Estimated amount is required")
    @DecimalMin(value = "0.01", message = "Estimated amount must be greater than zero")
    BigDecimal estimatedAmount
) {}
