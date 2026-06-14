package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkoutDayRequest(
        @NotBlank String name,
        @NotNull Integer dayOrder
) {}