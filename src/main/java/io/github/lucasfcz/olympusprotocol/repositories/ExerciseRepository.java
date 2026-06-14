package io.github.lucasfcz.olympusprotocol.repositories;

import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

// ActivatedMusclesRepository não precisa — gerenciado via cascade do Exercise
// ExerciseTipRepository não precisa — mesmo motivo
// WorkoutDayExerciseRepository não precisa — gerenciado via cascade do WorkoutDay
// WorkoutSessionExerciseRepository não precisa — gerenciado via cascade do WorkoutSession
// WorkoutSessionSetRepository não precisa — gerenciado via cascade do WorkoutSessionExercise

public interface ExerciseRepository extends JpaRepository<Exercise, UUID>, JpaSpecificationExecutor<Exercise> {

    boolean existsByNameIgnoreCase(String name);
}