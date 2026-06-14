package io.github.lucasfcz.olympusprotocol.models;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.TipType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exercise_tips")
public class ExerciseTip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel targetLevel;   // BEGINNER, INTERMEDIATE, ADVANCED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipType tipType;               // FORM, BREATHING, COMMON_MISTAKE, PROGRESSION

    @Column(nullable = false, length = 500)
    private String content;

    public ExerciseTip(Exercise exercise, ExperienceLevel targetLevel, TipType tipType, String content) {
        this.exercise = exercise;
        this.targetLevel = targetLevel;
        this.tipType = tipType;
        this.content = content;
    }
}
