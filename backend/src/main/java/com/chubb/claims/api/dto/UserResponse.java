package com.chubb.claims.api.dto;

import com.chubb.claims.domain.Market;
import com.chubb.claims.domain.User;
import com.chubb.claims.domain.UserRole;

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
