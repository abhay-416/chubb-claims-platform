package com.chubb.claims.claim.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProvideInfoPayload(
    @NotBlank(message = "Response details cannot be blank")
    String response
) {}
