package io.github.lucasfcz.olympusprotocol.dto.requests;

import io.github.lucasfcz.olympusprotocol.models.enums.WorkoutGoal;
import jakarta.validation.constraints.NotBlank;

public record WorkoutPlanRequest(
        @NotBlank String name,
        WorkoutGoal goal
) {}
