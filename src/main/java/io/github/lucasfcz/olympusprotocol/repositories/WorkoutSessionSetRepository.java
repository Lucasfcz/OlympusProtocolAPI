package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

// REMEMBER IN FUTURE UPGRADE QUERIES TO STATS SERVICE
// Read-only repository, writes are handled by WorkoutSession cascade
public interface WorkoutSessionSetRepository extends JpaRepository<WorkoutSessionSet, UUID> {
    @Query("""
    SELECT s
    FROM WorkoutSessionSet s
    WHERE s.workoutSessionExercise.exercise = :exercise
      AND s.workoutSessionExercise.workoutSession.user = :user
      AND s.workoutSessionExercise.workoutSession.finishedAt IS NOT NULL
      ORDER BY s.workoutSessionExercise.workoutSession.startedAt ASC
""")
    List<WorkoutSessionSet> findCompletedSetsByUserAndExercise(User user, Exercise exercise);

}
