package io.github.lucasfcz.olympusprotocol.dto.requests;

import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleHead;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRegion;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MuscleActivationRequest(
        @NotNull MuscleGroup muscleGroup,
        MuscleRegion muscleRegion,
        MuscleHead muscleHead,
        @NotNull MuscleRole muscleRole,
        @NotNull @Min(1) @Max(100) Integer activationPercent
) {}