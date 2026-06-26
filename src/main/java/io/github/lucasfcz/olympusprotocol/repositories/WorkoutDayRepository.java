package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, UUID> {
    List<WorkoutDay> findByWorkoutPlanOrderByDayOrderAsc(WorkoutPlan workoutPlan);

    @Query("""
        SELECT DISTINCT wd FROM WorkoutDay wd
        LEFT JOIN FETCH wd.exercises de
        LEFT JOIN FETCH de.exercise e
        WHERE wd.id = :id
        """)
    Optional<WorkoutDay> findByIdWithExercises(@Param("id") UUID id);

    Optional<WorkoutDay> findByIdAndWorkoutPlanId(UUID dayId, UUID workoutPlanId
    );
}