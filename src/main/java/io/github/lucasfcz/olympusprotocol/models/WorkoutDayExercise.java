package io.github.lucasfcz.olympusprotocol.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_day_exercises")
public class WorkoutDayExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer exerciseOrder;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer reps;

    @Column(name = "rest_time", nullable = false)
    private Integer restTime;

    public WorkoutDayExercise(WorkoutDay workoutDay, Exercise exercise,
                              Integer exerciseOrder, Integer sets,
                              Integer reps, Integer restTime) {
        this.workoutDay = workoutDay;
        this.exercise = exercise;
        this.exerciseOrder = exerciseOrder;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
    }
}