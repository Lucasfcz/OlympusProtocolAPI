package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.dto.responses.MuscleVolumeResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.BusinessException;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_sessions")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id")
    private WorkoutDay workoutDay;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutSessionExercise> exercises = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;  // null while a session is happening

    @Column(length = 500)
    private String notes;

    public WorkoutSession(User user, WorkoutDay workoutDay) {
        this.user = user;
        this.workoutDay = workoutDay;
    }

    public void finish(String notes) {
        if(isFinished()) {
            throw new BusinessException("This session is already finished");
        }
        this.finishedAt = LocalDateTime.now();
        this.notes = notes;
    }

    public boolean isFinished() {
        return finishedAt != null;
    }

    public void addExercise(WorkoutSessionExercise exercise) {
        this.exercises.add(exercise);
    }

    public void removeExercise(WorkoutSessionExercise exercise) {
        this.exercises.remove(exercise);
    }

    public Double getTotalVolume() {
        return exercises.stream()
                .mapToDouble(WorkoutSessionExercise::getExerciseVolume)
                .sum();
    }

    public Duration sessionDuration() {
        if (finishedAt == null) {
            return null;
        }
        return Duration.between(startedAt, finishedAt);
    }

    public List<MuscleVolumeResponse> getAggregatedMuscleVolumes() {
        Map<MuscleGroup, Double> aggregatedVolumes = exercises.stream()
                .flatMap(exercise -> exercise.getAggregatedMuscleVolumes().stream())
                .collect(Collectors.groupingBy(
                        MuscleVolumeResponse::muscleGroup,
                        Collectors.summingDouble(MuscleVolumeResponse::totalVolume)
                ));

        return aggregatedVolumes.entrySet().stream()
                .map(entry -> new MuscleVolumeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
