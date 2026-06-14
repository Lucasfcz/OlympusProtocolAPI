package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;
import java.util.UUID;

public record WorkoutDayResponse(
        UUID id,
        String name,
        Integer dayOrder,
        List<WorkoutDayExerciseResponse> exercises
) {}
