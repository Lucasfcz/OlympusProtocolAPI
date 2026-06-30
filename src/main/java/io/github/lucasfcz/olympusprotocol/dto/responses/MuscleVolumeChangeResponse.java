package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;

public record MuscleVolumeChangeResponse(
        MuscleGroup muscleGroup,
        Double currentVolume,
        Double previousVolume,
        Double percentageChange
) {}
