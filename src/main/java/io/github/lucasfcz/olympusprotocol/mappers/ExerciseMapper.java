package io.github.lucasfcz.olympusprotocol.mappers;

import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseContraindicationRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseTipRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.MuscleActivationRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseContraindicationResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseTipResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.MuscleActivationResponse;
import io.github.lucasfcz.olympusprotocol.models.ActivatedMuscles;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.ExerciseContraindication;
import io.github.lucasfcz.olympusprotocol.models.ExerciseTip;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getRecommendedExperienceLevel(),
                exercise.getSafetyRating(),
                exercise.getEfficiencyRating(),
                exercise.getAdminNotes(),
                exercise.getGifUrl(),
                exercise.isActive(),
                exercise.getMuscles().stream().map(this::toMuscleResponse).toList(),
                exercise.getTips().stream().map(this::toTipResponse).toList(),
                exercise.getContraindications().stream().map(this::toContraindicationResponse).toList()
        );
    }

    public ActivatedMuscles toMuscleEntity(Exercise exercise, MuscleActivationRequest req) {
        return new ActivatedMuscles(
                exercise,
                req.muscleGroup(),
                req.muscleRegion(),
                req.muscleHead(),
                req.muscleRole(),
                req.activationPercent()
        );
    }

    public ExerciseTip toTipEntity(Exercise exercise, ExerciseTipRequest req) {
        return new ExerciseTip(exercise, req.targetLevel(), req.tipType(), req.content());
    }

    public ExerciseContraindication toContraindicationEntity(Exercise exercise, ExerciseContraindicationRequest req) {
        return new ExerciseContraindication(exercise, req.condition(), req.explanation());
    }

    private MuscleActivationResponse toMuscleResponse(ActivatedMuscles m) {
        return new MuscleActivationResponse(
                m.getId(), m.getMuscleGroup(), m.getMuscleRegion(),
                m.getMuscleHead(), m.getMuscleRole(), m.getActivationPercent()
        );
    }

    private ExerciseTipResponse toTipResponse(ExerciseTip t) {
        return new ExerciseTipResponse(t.getId(), t.getTargetLevel(), t.getTipType(), t.getContent());
    }

    private ExerciseContraindicationResponse toContraindicationResponse(ExerciseContraindication c) {
        return new ExerciseContraindicationResponse(c.getId(), c.getCondition(), c.getExplanation());
    }
}
