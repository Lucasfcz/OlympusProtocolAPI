package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, UUID> {
    List<WorkoutDay> findByWorkoutPlanOrderByDayOrderAsc(WorkoutPlan workoutPlan);
}