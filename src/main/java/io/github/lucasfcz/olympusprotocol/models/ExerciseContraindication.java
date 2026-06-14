package io.github.lucasfcz.olympusprotocol.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exercise_contraindications")
public class ExerciseContraindication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private String condition;   // injuries, uncomfortable...

    @Column(nullable = false, length = 500)
    private String explanation;

    public ExerciseContraindication(Exercise exercise, String condition, String explanation) {
        this.exercise = exercise;
        this.condition = condition;
        this.explanation = explanation;
    }
}
