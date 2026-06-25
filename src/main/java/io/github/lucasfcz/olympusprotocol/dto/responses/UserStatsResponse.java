package io.github.lucasfcz.olympusprotocol.dto.responses;

public record UserStatsResponse(
        long totalSessions,
        long totalSets,
        Double totalVolumeAllTime,
        long totalMinutesTrained,
        String mostUsedExercise
) {}
