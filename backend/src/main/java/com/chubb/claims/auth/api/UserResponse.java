package com.chubb.claims.auth.api;

import com.chubb.claims.auth.domain.User;
import com.chubb.claims.auth.domain.UserRole;
import com.chubb.claims.claim.domain.Market;

public record UserResponse(
    Long id,
    String name,
    String email,
    UserRole role,
    Market market
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getMarket()
        );
    }
}
