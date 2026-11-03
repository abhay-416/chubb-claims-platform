package com.chubb.claims.api.dto;

public record AuthResponse(
    String token,
    UserResponse user
) {}
