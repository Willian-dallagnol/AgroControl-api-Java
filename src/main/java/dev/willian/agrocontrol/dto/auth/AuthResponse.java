package dev.willian.agrocontrol.dto.auth;

import dev.willian.agrocontrol.dto.UserResponse;

public record AuthResponse(
        String token,
        String type,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse bearer(String token, long expiresInMs, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresInMs, user);
    }
}
