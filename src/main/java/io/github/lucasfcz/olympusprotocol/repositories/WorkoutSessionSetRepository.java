package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// Read-only repository, writes are handled by WorkoutSession cascade
public interface WorkoutSessionSetRepository extends JpaRepository<WorkoutSessionSet, UUID> {
    @Query("""
    SELECT s FROM WorkoutSessionSet s
    JOIN FETCH s.workoutSessionExercise se
    JOIN FETCH se.workoutSession ws
    WHERE ws.user = :user
    AND se.exercise = :exercise
    AND ws.finishedAt IS NOT NULL
    ORDER BY ws.startedAt ASC
""")
    List<WorkoutSessionSet> findCompletedSetsByUserAndExercise(
            @Param("user") User user,
            @Param("exercise") Exercise exercise
    );

    @Query("""
    SELECT COUNT(s)
    FROM WorkoutSessionSet s
    JOIN s.workoutSessionExercise se
    JOIN se.workoutSession ws
    WHERE ws.user = :user
    AND ws.finishedAt IS NOT NULL
""")
    long totalOfSetsFromUser(@Param("user") User user);
}
