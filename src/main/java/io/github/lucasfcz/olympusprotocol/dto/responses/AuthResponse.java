package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.Role;

public record AuthResponse(
        String token,
        String name,
        String email,
        Role role
) {}
