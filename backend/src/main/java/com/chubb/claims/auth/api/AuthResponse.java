package com.chubb.claims.auth.api;

public record AuthResponse(
    String token,
    UserResponse user
) {}
