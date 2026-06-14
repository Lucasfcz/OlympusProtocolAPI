package io.github.lucasfcz.olympusprotocol.dto.requests;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record WorkoutDayExerciseRequest(
        @NotNull UUID exerciseId,
        @NotNull Integer exerciseOrder,
        @NotNull Integer sets,
        @NotNull Integer reps,
        @NotNull Integer restTime
) {}