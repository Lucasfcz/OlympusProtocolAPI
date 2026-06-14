package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
    List<WorkoutSession> findByUser(User user);

    Optional<WorkoutSession> findByUserAndFinishedAtIsNull(User user);

    // historic of one workout day
    List<WorkoutSession> findByUserAndWorkoutDay(User user, WorkoutDay workoutDay);
}
