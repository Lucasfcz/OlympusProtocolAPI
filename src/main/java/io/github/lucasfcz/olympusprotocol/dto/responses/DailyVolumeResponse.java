package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.time.DayOfWeek;

public record DailyVolumeResponse(
        DayOfWeek day,
        Double volume
) {
}
