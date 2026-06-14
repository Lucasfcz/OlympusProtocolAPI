package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleHead;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRegion;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "muscles_activation")
public class ActivatedMuscles {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MuscleGroup muscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_region")
    private MuscleRegion muscleRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_head")
    private MuscleHead muscleHead;

    @Enumerated(EnumType.STRING)
    @Column(name = "muscle_role", nullable = false)
    private MuscleRole muscleRole;

    @Column(name = "activation_percent", nullable = false)
    private Integer activationPercent;

    public ActivatedMuscles(Exercise exercise, MuscleGroup muscleGroup, MuscleRegion muscleRegion, MuscleHead muscleHead, MuscleRole muscleRole, Integer activationPercent) {
        this.exercise = exercise;
        this.muscleGroup = muscleGroup;
        this.muscleRegion = muscleRegion;
        this.muscleHead = muscleHead;
        this.muscleRole = muscleRole;
        this.activationPercent = activationPercent;
    }
}