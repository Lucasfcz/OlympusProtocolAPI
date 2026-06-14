package io.github.lucasfcz.olympusprotocol.dto.requests;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.TipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseTipRequest(
        @NotNull ExperienceLevel targetLevel,
        @NotNull TipType tipType,
        @NotBlank String content
) {}