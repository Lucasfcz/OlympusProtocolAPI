package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutSessionResponse(
        UUID id,
        UUID workoutDayId,
        String workoutDayName,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Double totalVolume,
        Long durationMinutes,
        Integer totalExercises,
        List<WorkoutSessionExercisesResponse> sessionExercises,
        List<String> warnings
) {}
