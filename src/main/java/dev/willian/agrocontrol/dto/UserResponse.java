package dev.willian.agrocontrol.dto;

import dev.willian.agrocontrol.domain.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
