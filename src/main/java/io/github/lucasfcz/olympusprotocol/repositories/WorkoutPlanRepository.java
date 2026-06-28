package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {
        @Query("""
            SELECT DISTINCT wp FROM WorkoutPlan wp
            LEFT JOIN FETCH wp.workoutDays wd
            LEFT JOIN FETCH wd.exercises wde
            LEFT JOIN FETCH wde.exercise e
            WHERE wp.user = :user
            AND wp.active IS TRUE
        """)
        Optional<WorkoutPlan> findByUserAndActiveTrueWithDetails(@Param("user") User user);

        @Query("""
            SELECT DISTINCT wp FROM WorkoutPlan wp
            LEFT JOIN FETCH wp.workoutDays wd
            LEFT JOIN FETCH wd.exercises wde
            LEFT JOIN FETCH wde.exercise e
            WHERE wp.user = :user
        """)
        Optional<List<WorkoutPlan>> findAllByUserWithDetails(@Param("user") User user);

        @Query("""
            SELECT DISTINCT wp FROM WorkoutPlan wp
            LEFT JOIN FETCH wp.workoutDays wd
            LEFT JOIN FETCH wd.exercises wde
            LEFT JOIN FETCH wde.exercise e
            WHERE wp.id = :id
        """)
        Optional<WorkoutPlan> findByIdWithDetails(@Param("id") UUID id);

        Optional<WorkoutPlan> findByUserIdAndActiveTrue(UUID userId);
}
