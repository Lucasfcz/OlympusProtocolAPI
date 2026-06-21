package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SetRequest(
        Integer setOrder,
        Integer reps,
        Double weight,
        Integer restTime,
        @Min(0) @Max(10) Double rpe
) {
}
