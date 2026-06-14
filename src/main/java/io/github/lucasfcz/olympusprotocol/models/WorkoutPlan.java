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
@Table(name = "workouts")
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean active;

    public WorkoutPlan(User user, String name) {
        this.user = user;
        this.name = name;
        this.active = true;
    }

    public void addDay(WorkoutDay day) {
        workoutDays.add(day);
    }

    public void updateGoal(WorkoutGoal goal) {
        this.goal = goal;
    }

    public void deactivate() { this.active = false; }

    public void reactivate() {this.active = true; }
}
