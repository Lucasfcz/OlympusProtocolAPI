package io.github.lucasfcz.olympusprotocol.dto.responses;

import io.github.lucasfcz.olympusprotocol.models.enums.EfficiencyRating;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.SafetyRating;

import java.util.List;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        ExperienceLevel minExperienceLevel,
        SafetyRating safetyRating,
        EfficiencyRating efficiencyRating,
        String adminNotes,
        String gifUrl,
        boolean active,
        List<MuscleActivationResponse> muscles,
        List<ExerciseTipResponse> tips,
        List<ExerciseContraindicationResponse> contraindications
) {}