package io.github.lucasfcz.olympusprotocol.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_days")
public class WorkoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer dayOrder;

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutDayExercise> exercises = new ArrayList<>();

    public WorkoutDay(WorkoutPlan workoutPlan, String name, Integer dayOrder) {
        this.workoutPlan = workoutPlan;
        this.name = name;
        this.dayOrder = dayOrder;
    }

    public void addExercise(WorkoutDayExercise exercise) {
        exercises.add(exercise);
    }

    public void removeExercise(UUID exerciseId) {
        exercises.removeIf(e -> e.getId().equals(exerciseId));
    }
}
