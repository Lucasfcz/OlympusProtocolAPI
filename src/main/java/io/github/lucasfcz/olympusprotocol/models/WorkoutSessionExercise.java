package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.dto.responses.MuscleVolumeResponse;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_session_exercises")
public class WorkoutSessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer exerciseOrder;

    @OneToMany(mappedBy = "workoutSessionExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutSessionSet> sets = new ArrayList<>();

    public WorkoutSessionExercise(WorkoutSession workoutSession, Exercise exercise, Integer exerciseOrder) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.exerciseOrder = exerciseOrder;
    }

    public void addSet(WorkoutSessionSet set) {
        this.sets.add(set);
    }

    public void removeSet(WorkoutSessionSet set) {
        this.sets.remove(set);
    }

    public void updateSessionExercise(Exercise exercise, Integer exerciseOrder) {
        this.exercise = exercise;
        this.exerciseOrder = exerciseOrder;

    }

    public void updateOrder(Integer newOrder) {
        this.exerciseOrder = newOrder;
    }

    public Double getExerciseVolume() {
        return sets.stream()
                .mapToDouble(WorkoutSessionSet::setVolume)
                .sum();
    }

    public List<MuscleVolumeResponse> getAggregatedMuscleVolumes() {
        Map<MuscleGroup, Double> aggregatedVolumes = sets.stream()
                .flatMap(set -> set.setMuscleVolumes().stream())
                .collect(Collectors.groupingBy(
                        MuscleVolumeResponse::muscleGroup,
                        Collectors.summingDouble(MuscleVolumeResponse::totalVolume)
                ));

        return aggregatedVolumes.entrySet().stream()
                .map(entry -> new MuscleVolumeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
