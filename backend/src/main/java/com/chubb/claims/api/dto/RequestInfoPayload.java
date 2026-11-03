package com.chubb.claims.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestInfoPayload(
    @NotBlank(message = "Requested information details cannot be blank")
    String requestedInfo
) {}
