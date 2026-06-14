package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.UUID;

public record WorkoutDayExerciseResponse(
        UUID id,
        UUID exerciseId,
        String exerciseName,
        Integer exerciseOrder,
        Integer sets,
        Integer reps,
        Integer restTime
) {}