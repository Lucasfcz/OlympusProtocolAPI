package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record AddExerciseToSessionRequest(
        UUID exerciseId,
        @Min(1)Integer exerciseOrder
) {
}
