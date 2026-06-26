package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.dto.responses.MuscleVolumeResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_session_sets")
public class WorkoutSessionSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_exercise_id", nullable = false)
    private WorkoutSessionExercise workoutSessionExercise;

    // Ordering the number of series for exercise
    @Column(nullable = false)
    private Integer setOrder;

    @Column(nullable = false)
    @Min(1)
    private Integer reps;

    @Column // can be null, because some exercises uses the bodyweight
    @Min(0)
    private Double weight;

    @Column
    @Min(0)
    private Integer restTime;

    // Rating percent exertion
    @Column
    @Min(value = 0, message = "rpe cannot be less than 0")
    @Max(value = 10, message = "rpe cannot be more than 10")
    private Double rpe;

    public WorkoutSessionSet(WorkoutSessionExercise workoutSessionExercise, Integer setOrder, Integer reps, Double weight, Integer restTime, Double rpe) {
        this.workoutSessionExercise = workoutSessionExercise;
        this.setOrder = setOrder;
        this.reps = reps;
        this.weight = weight;
        this.restTime = restTime;
        this.rpe = rpe;
    }

    public void updateSet(Integer setOrder, Integer reps, Double weight, Integer restTime, Double rpe) {
        this.setOrder = setOrder;
        this.reps = reps;
        this.weight = weight;
        this.restTime = restTime;
        this.rpe = rpe;
    }

    public void updateOrder(Integer newOrder) {
        this.setOrder = newOrder;
    }

    public Double setVolume() {
        if (weight == null || reps == null) {
            return 0.0;
        }
        return weight * reps;
    }

    public List<MuscleVolumeResponse> setMuscleVolumes() {
        if (weight == null || reps == null || workoutSessionExercise == null || workoutSessionExercise.getExercise() == null) {
            return List.of(); // if something is null return an empty list
        }
        double baseVolume = setVolume();
        return workoutSessionExercise.getExercise().getMuscles().stream()
                .map(am -> new MuscleVolumeResponse(am.getMuscleGroup(), baseVolume * (am.getActivationPercent() / 100.0)))
                .toList();
    }
}
