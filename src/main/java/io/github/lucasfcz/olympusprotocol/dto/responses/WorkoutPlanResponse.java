package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.WorkoutGoal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutPlanResponse(
        UUID id,
        String name,
        WorkoutGoal goal,
        boolean active,
        LocalDateTime createdAt,
        List<WorkoutDayResponse> days
) {}
