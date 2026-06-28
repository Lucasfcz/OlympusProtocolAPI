package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.models.enums.WorkoutGoal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column
    private WorkoutGoal goal; // HYPERTROPHY, FAT_LOSS, STRENGTH, ENDURANCE

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutDay> workoutDays = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WorkoutPlan(User user, String name) {
        this.user = user;
        this.name = name;
        this.isPublic = true;
        this.active = true;
    }

    public void addDay(WorkoutDay day) {
        workoutDays.add(day);
    }

    public void removeDay(UUID dayId) {
        workoutDays.removeIf(d -> d.getId().equals(dayId));
    }

    public void updateGoal(WorkoutGoal goal) {
        this.goal = goal;
    }

    public void changeVisibility() {
        this.isPublic = !isPublic;
    }

    public void deactivate() { this.active = false; }

    public void reactivate() {this.active = true; }
}
