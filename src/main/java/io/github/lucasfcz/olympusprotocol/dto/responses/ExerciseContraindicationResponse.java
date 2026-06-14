package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.UUID;

public record ExerciseContraindicationResponse(
        UUID id,
        String condition,
        String explanation
) {}
