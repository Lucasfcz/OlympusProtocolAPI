package io.github.lucasfcz.olympusprotocol.dto.responses;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExerciseStatsResponse(
        UUID exerciseId,
        String exerciseName,
        Integer totalSets,
        Double maxWeight,
        Integer repsIfMaxWeight,
        LocalDateTime dayOfSetWithMaxWeight,
        List<ChartPointResponse> progression
) {}
