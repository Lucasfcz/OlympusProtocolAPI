package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutSessionExercisesResponse(
        UUID id,
        UUID exerciseId,
        String exerciseName,
        Integer exerciseOrder,
        Double exerciseVolume,
        List<WorkoutSessionSetResponse> sets,
        List<MuscleVolumeResponse> totalMuscleVolume
) {}
