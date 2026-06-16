package io.github.lucasfcz.olympusprotocol.mappers;


import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutDayExerciseResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutDayResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutPlanResponse;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDayExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkoutPlanMapper {

    public WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        return toResponse(plan, List.of());
    }

    public WorkoutPlanResponse toResponse(WorkoutPlan plan, List<String> warnings) {
        return new WorkoutPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getGoal(),
                plan.isActive(),
                plan.getCreatedAt(),
                plan.getWorkoutDays().stream()
                        .sorted(Comparator.comparing(WorkoutDay::getDayOrder))
                        .map(this::toDayResponse)
                        .toList(),
                warnings
        );
    }

    public WorkoutDayResponse toDayResponse(WorkoutDay day) {
        return new WorkoutDayResponse(
                day.getId(),
                day.getName(),
                day.getDayOrder(),
                day.getExercises().stream()
                        .sorted(Comparator.comparing(WorkoutDayExercise::getExerciseOrder))
                        .map(this::toDayExerciseResponse)
                        .toList()
        );
    }

    private WorkoutDayExerciseResponse toDayExerciseResponse(WorkoutDayExercise e) {
        return new WorkoutDayExerciseResponse(
                e.getId(),
                e.getExercise().getId(),
                e.getExercise().getName(),
                e.getExerciseOrder(),
                e.getSets(),
                e.getReps(),
                e.getRestTime()
        );
    }
}
