package io.github.lucasfcz.olympusprotocol.dto.requests;

import io.github.lucasfcz.olympusprotocol.models.enums.EfficiencyRating;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.SafetyRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExerciseRequest(
        @NotBlank String name,
        String description,
        @NotNull ExperienceLevel minExperienceLevel,
        @NotNull SafetyRating safetyRating,
        @NotNull EfficiencyRating efficiencyRating,
        String adminNotes,
        String gifUrl,
        @NotEmpty List<MuscleActivationRequest> muscles,
        List<ExerciseTipRequest> tips,
        List<ExerciseContraindicationRequest> contraindications
) {}