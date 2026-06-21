package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;

import java.util.UUID;

public record PublicUserResponse(
        UUID id,
        String name,
        String avatarUrl,
        ExperienceLevel experienceLevel
) {}