package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    List<WorkoutSession> findByUser(User user);

    Optional<WorkoutSession> findByUserAndFinishedAtIsNull(User user);

    // Historic of one workout day
    List<WorkoutSession> findByUserAndWorkoutDay(User user, WorkoutDay workoutDay);

    // ✅ FETCH JOIN para evitar N+1 em exercises e sets
    @Query("""
        SELECT DISTINCT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets wss
        LEFT JOIN FETCH wse.exercise e
        WHERE ws.id = :id
        """)
    Optional<WorkoutSession> findByIdWithDetails(@Param("id") UUID id);

    // ✅ FETCH JOIN para listagem
    @Query("""
        SELECT DISTINCT ws
        FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets
        LEFT JOIN FETCH wse.exercise
        WHERE ws.user = :user
        ORDER BY ws.startedAt DESC
""")
    List<WorkoutSession> findByUserWithDetails(@Param("user") User user);

    // ✅ FETCH JOIN para stats
    @Query("""
        SELECT DISTINCT ws FROM WorkoutSession ws
        LEFT JOIN FETCH ws.exercises wse
        LEFT JOIN FETCH wse.sets
        WHERE ws.user = :user
        AND ws.finishedAt IS NOT NULL
        AND ws.startedAt BETWEEN :startDateTime AND :endDateTime
        """)
    List<WorkoutSession> findByUserAndFinishedAtIsNotNullAndStartedAtBetween(
            @Param("user") User user,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
