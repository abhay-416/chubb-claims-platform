package com.chubb.claims.claim.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AssessClaimRequest(
    @NotBlank(message = "Decision is required (APPROVE or REJECT)")
    String decision,

    BigDecimal approvedAmount,

    String notes
) {}
