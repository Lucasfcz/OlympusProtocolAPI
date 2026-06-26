package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;

public record MuscleVolumeResponse(
        MuscleGroup muscleGroup,
        Double totalVolume
) {}
