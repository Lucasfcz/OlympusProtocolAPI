package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    List<WorkoutSession> findByUser(User user);

    Optional<WorkoutSession> findByUserAndFinishedAtIsNull(User user);

    // Historic of one workout day
    List<WorkoutSession> findByUserAndWorkoutDay(User user, WorkoutDay workoutDay);

    @Query("""
        SELECT DISTINCT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets wss
        LEFT JOIN FETCH wse.exercise e
        WHERE ws.id = :id
        """)
    Optional<WorkoutSession> findByIdWithExercisesAndSets(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT ws
        FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets
        LEFT JOIN FETCH wse.exercise
        WHERE ws.user = :user
        ORDER BY ws.startedAt DESC
""")
    List<WorkoutSession> findByUserWithExercisesAndSets(@Param("user") User user);

    @Query("""
        SELECT DISTINCT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets
        WHERE ws.user = :user
        AND ws.finishedAt IS NOT NULL
        AND ws.startedAt BETWEEN :startDateTime AND :endDateTime
        """)
    List<WorkoutSession> findSessionWithExercisesAndSetsBetweenTime(
            @Param("user") User user,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT COUNT(s) FROM WorkoutSession s WHERE s.user = :user AND s.finishedAt IS NOT NULL")
    long countTotalOfSessionsFromUser(@Param("user") User user);

    @Query("""
    SELECT SUM(set.weight * set.reps)
    FROM WorkoutSessionSet set
    JOIN set.workoutSessionExercise se
    JOIN se.workoutSession s
    WHERE s.user = :user
    AND s.finishedAt IS NOT NULL
    AND set.weight IS NOT NULL
""")
    Double totalVolumeAllTime(@Param("user") User user);

    @Query("""
    SELECT SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, s.startedAt, s.finishedAt))
    FROM WorkoutSession s
    WHERE s.user = :user
    AND s.finishedAt IS NOT NULL
""")
    Long totalMinutesTrained(@Param("user") User user);
}

