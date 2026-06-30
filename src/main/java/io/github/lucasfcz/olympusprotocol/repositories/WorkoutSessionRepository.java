package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
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
        LEFT JOIN FETCH e.muscles am
        LEFT JOIN FETCH ws.user u
        WHERE ws.id = :sessionId
        """)
    Optional<WorkoutSession> findByIdWithExercisesAndSets(@Param("sessionId") UUID sessionId);

    @Query("""
        SELECT DISTINCT ws
        FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets wss
        LEFT JOIN FETCH wse.exercise e
        LEFT JOIN FETCH e.muscles am
        LEFT JOIN FETCH ws.user u
        WHERE ws.user = :user
        ORDER BY ws.startedAt DESC
""")
    List<WorkoutSession> findByUserWithExercisesAndSets(@Param("user") User user);

    @Query("""
        SELECT DISTINCT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets wss
        LEFT JOIN FETCH wse.exercise e
        LEFT JOIN FETCH e.muscles am
        LEFT JOIN FETCH ws.user u
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
    SELECT SUM(ss.weight * ss.reps)
    FROM WorkoutSessionSet ss
    JOIN ss.workoutSessionExercise se
    JOIN se.workoutSession s
    WHERE s.user = :user
    AND s.finishedAt IS NOT NULL
    AND ss.weight IS NOT NULL
""")
    Double totalVolumeAllTime(@Param("user") User user);

    @Query("""
    SELECT SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, s.startedAt, s.finishedAt))
    FROM WorkoutSession s
    WHERE s.user = :user
    AND s.finishedAt IS NOT NULL
""")
    Long totalMinutesTrained(@Param("user") User user);

    @Query("""
        SELECT SUM((s.weight * s.reps) * m.activationPercent / 100)
        FROM WorkoutSessionExercise ws
        JOIN ws.sets s
        JOIN ws.exercise.muscles m
        WHERE m.muscleGroup = :muscleGroup
        AND ws.workoutSession.user= :user
        AND s.weight IS NOT NULL
        AND s.reps IS NOT NULL
        AND m.activationPercent IS NOT NULL
""")
    Double totalVolumeOfMuscleByUser(@Param("muscleGroup") MuscleGroup muscleGroup, @Param("user") User user);


    @Query("""
        SELECT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.workoutDay wd
        WHERE ws.user = :user
        AND ws.workoutDay IS NOT NULL
        AND ws.finishedAt IS NOT NULL
        ORDER BY ws.startedAt DESC
    """)
    List<WorkoutSession> findLastTwoSessionsWithWorkoutPlan(@Param("user") User user);

    @Query("""
        SELECT SUM((wss.weight * wss.reps) * am.activationPercent / 100.0)
        FROM WorkoutSessionSet wss
        JOIN wss.workoutSessionExercise wse
        JOIN wse.exercise e
        JOIN e.muscles am
        WHERE wse.workoutSession.id = :sessionId
        AND am.muscleGroup = :muscleGroup
        AND wss.weight IS NOT NULL
        AND wss.reps IS NOT NULL
        AND am.activationPercent IS NOT NULL
    """)
    Optional<Double> calculateMuscleVolumeForSession(@Param("sessionId") UUID sessionId, @Param("muscleGroup") MuscleGroup muscleGroup);
}
