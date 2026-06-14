package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.TipType;

import java.util.UUID;

public record ExerciseTipResponse(
        UUID id,
        ExperienceLevel targetLevel,
        TipType tipType,
        String content
) {}