package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;

public record WeeklyVolumeResponse(
        List<DailyVolumeResponse> volumes
) {
}
