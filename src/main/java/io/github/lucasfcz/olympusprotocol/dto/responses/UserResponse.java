package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        String name,
        String email,
        Role role,
        String avatarUrl,
        ExperienceLevel experienceLevel,
        Double bodyWeight,
        Double height,
        LocalDateTime createdAt
) {}
