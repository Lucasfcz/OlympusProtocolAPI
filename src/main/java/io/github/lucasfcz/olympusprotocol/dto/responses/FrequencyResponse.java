package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;

public record FrequencyResponse(
        int totalSessions,
        int totalDays,
        double avgSessionsPerWeek,  // Percent of days of workout plan
        List<ChartPointResponse> sessionsPerWeek
) {}
