package io.github.lucasfcz.olympusprotocol.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Integer setOrder; // new

    @Column(nullable = false)
    private Integer reps;

    @Column // can be null, because some exercises uses the bodyweight
    private Double weight;

    @Column
    private Integer restTime;

    public WorkoutSessionSet(Integer setOrder, Integer reps, Double weight, Integer restTime) {
        this.setOrder = setOrder;
        this.reps = reps;
        this.weight = weight;
        this.restTime = restTime;
    }

    public Double setVolume() {
        if (weight == null || reps == null) {
            return 0.0;
        }
        return weight * reps;
    }
}
