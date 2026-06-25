package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;
import java.util.UUID;

public record SessionSummaryResponse(
        UUID sessionId,
        String workoutDayName,
        Long durationMinutes,
        Double totalVolume,
        List<WorkoutSessionExercisesResponse> exercises
) {}