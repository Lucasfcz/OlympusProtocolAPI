package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;
import java.util.UUID;

public record WorkoutSessionSetResponse(
        UUID id,
        UUID sessionExerciseId,
        Integer setOrder,
        Integer reps,
        Double weight,
        Integer restTime,
        Double rpe,
        List<MuscleVolumeResponse> musclesVolumes
) {}