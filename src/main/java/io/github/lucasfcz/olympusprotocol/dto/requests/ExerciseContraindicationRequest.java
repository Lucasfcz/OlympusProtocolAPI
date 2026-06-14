package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record ExerciseContraindicationRequest(
        @NotBlank String condition,
        @NotBlank String explanation
) {}

