package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleHead;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRegion;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRole;

import java.util.UUID;

public record MuscleActivationResponse(
        UUID id,
        MuscleGroup muscleGroup,
        MuscleRegion muscleRegion,
        MuscleHead muscleHead,
        MuscleRole muscleRole,
        Integer activationPercent
) {}
