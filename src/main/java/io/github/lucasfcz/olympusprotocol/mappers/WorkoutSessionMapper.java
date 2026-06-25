package io.github.lucasfcz.olympusprotocol.mappers;

import io.github.lucasfcz.olympusprotocol.dto.responses.SessionSummaryResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionExercisesResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionResponse;

import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionSetResponse;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
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
                        : null,
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
                        .toList()
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
        set.getRpe()
        );
    }

    public SessionSummaryResponse toSummary(WorkoutSession session) {
        var duration = session.getFinishedAt() != null
                ? ChronoUnit.MINUTES.between(session.getStartedAt(), session.getFinishedAt())
                : 0L;

        var dayName = session.getWorkoutDay() != null
                ? session.getWorkoutDay().getName()
                : "Free Session";

        var exercises = session.getExercises().stream()
                .sorted(Comparator.comparing(WorkoutSessionExercise::getExerciseOrder))
                .map(e -> new WorkoutSessionExercisesResponse(
                        e.getId(),
                        e.getExercise().getId(),
                        e.getExercise().getName(),
                        e.getExerciseOrder(),
                        e.getExerciseVolume(),
                        e.getSets().stream()
                                .sorted(Comparator.comparing(WorkoutSessionSet::getSetOrder))
                                .map(s -> new WorkoutSessionSetResponse(
                                        s.getId(),
                                        s.getWorkoutSessionExercise().getExercise().getId(),
                                        s.getSetOrder(),
                                        s.getReps(),
                                        s.getWeight(),
                                        null,
                                        s.getRpe()
                                ))
                                .toList()
                ))
                .toList();

        return new SessionSummaryResponse(
                session.getId(),
                dayName,
                duration,
                session.getTotalVolume(),
                exercises
        );
    }
}