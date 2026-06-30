package io.github.lucasfcz.olympusprotocol.mappers;

import io.github.lucasfcz.olympusprotocol.dto.responses.*;

import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkoutSessionMapper {

    public WorkoutSessionResponse toResponse(WorkoutSession session){
        return toResponse(session, List.of());
    }

    public WorkoutSessionResponse toResponse(WorkoutSession session, List<String> warnings) {
        return new WorkoutSessionResponse(
                session.getId(),
                session.getWorkoutDay() != null
                        ? session.getWorkoutDay().getId()
                        : null,
                session.getWorkoutDay() != null
                        ? session.getWorkoutDay().getName()
                        : "Free Session",
                session.getNotes(),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getTotalVolume(),
                session.isFinished()
                        ? session.sessionDuration().toMinutes()
                        : null,
                session.getExercises().size(),
                session.getExercises().stream()
                        .sorted(Comparator.comparing(WorkoutSessionExercise::getExerciseOrder))
                        .map(this::toExerciseResponse)
                        .toList(),
                warnings
        );
    }

    public WorkoutSessionExercisesResponse toExerciseResponse(WorkoutSessionExercise exercise) {
        return new WorkoutSessionExercisesResponse(
                exercise.getId(),
                exercise.getExercise().getId(),
                exercise.getExercise().getName(),
                exercise.getExerciseOrder(),
                exercise.getExerciseVolume(),
                exercise.getSets().stream()
                        .sorted(Comparator.comparing(WorkoutSessionSet::getSetOrder))
                        .map(this::toSetResponse)
                        .toList(),
                exercise.getAggregatedMuscleVolumes()
        );
    }

    public WorkoutSessionSetResponse toSetResponse(WorkoutSessionSet set) {
        return new WorkoutSessionSetResponse(
        set.getId(),
        set.getWorkoutSessionExercise().getId(),
        set.getSetOrder(),
        set.getReps(),
        set.getWeight(),
        set.getRestTime(),
        set.getRpe(),
        set.setMuscleVolumes()
        );
    }
    
    private WorkoutSessionSetResponse toSummarySetResponse(WorkoutSessionSet set) {
        return new WorkoutSessionSetResponse(
                set.getId(),
                set.getWorkoutSessionExercise().getId(),
                set.getSetOrder(),
                set.getReps(),
                set.getWeight(),
                null, // restTime its null for summary
                set.getRpe(),
                set.setMuscleVolumes()
        );
    }
    
    private WorkoutSessionExercisesResponse toSummaryExerciseResponse(WorkoutSessionExercise exercise) {
        return new WorkoutSessionExercisesResponse(
                exercise.getId(),
                exercise.getExercise().getId(),
                exercise.getExercise().getName(),
                exercise.getExerciseOrder(),
                exercise.getExerciseVolume(),
                exercise.getSets().stream()
                        .sorted(Comparator.comparing(WorkoutSessionSet::getSetOrder))
                        .map(this::toSummarySetResponse)
                        .toList(),
                exercise.getAggregatedMuscleVolumes()
        );
    }

    public SessionSummaryResponse toSummary(WorkoutSession session, List<MuscleVolumeChangeResponse> muscleVolumeChanges) {
        var duration = session.getFinishedAt() != null
                ? session.sessionDuration().toMinutes()
                : null;

        var dayName = session.getWorkoutDay() != null
                ? session.getWorkoutDay().getName()
                : "Free Session";

        var exercises = session.getExercises().stream()
                .sorted(Comparator.comparing(WorkoutSessionExercise::getExerciseOrder))
                .map(this::toSummaryExerciseResponse)
                .toList();

        return new SessionSummaryResponse(
                session.getId(),
                dayName,
                duration,
                session.getTotalVolume(),
                exercises,
                session.getAggregatedMuscleVolumes(),
                muscleVolumeChanges // Adicionado o novo campo
        );
    }
}
