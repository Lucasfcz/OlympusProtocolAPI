package io.github.lucasfcz.olympusprotocol.models;


import io.github.lucasfcz.olympusprotocol.models.enums.*;
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
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "exercise",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ActivatedMuscles> muscles = new ArrayList<>();

    // recommendations about experience required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel minExperienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SafetyRating safetyRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EfficiencyRating efficiencyRating;

    // ✅ tips for each level(BEGINNER/INTERMEDIATE/ADVANCED)
    @OneToMany(mappedBy = "exercise",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ExerciseTip> tips = new ArrayList<>();

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseContraindication> contraindications = new ArrayList<>();

    @Column(length = 1000)
    private String adminNotes;

    @Column
    private String gifUrl;

    @CreationTimestamp
    @Column
    private LocalDateTime createAt;

    @Column(nullable = false)
    private boolean active;

    public Exercise(String name, String description, ExperienceLevel minExperienceLevel, SafetyRating safetyRating,
                    EfficiencyRating efficiencyRating, String adminNotes, String gifUrl) {
        this.name = name;
        this.description = description;
        this.minExperienceLevel = minExperienceLevel;
        this.safetyRating = safetyRating;
        this.efficiencyRating = efficiencyRating;
        this.adminNotes = adminNotes;
        this.gifUrl = gifUrl;
        this.active = true;
    }

    // clean and create another list
    public void replaceMuscles(List<ActivatedMuscles> newMuscles) {
        this.muscles.clear();
        this.muscles.addAll(newMuscles);
    }

    public void updateInfo(String name, String description, ExperienceLevel minExperienceLevel, SafetyRating safetyRating,
                           EfficiencyRating efficiencyRating, String adminNotes, String gifUrl) {
        this.name = name;
        this.description = description;
        this.minExperienceLevel = minExperienceLevel;
        this.safetyRating = safetyRating;
        this.efficiencyRating = efficiencyRating;
        this.adminNotes = adminNotes;
        this.gifUrl = gifUrl;
    }

    public void replaceTips(List<ExerciseTip> newTips) {
        this.tips.clear();
        this.tips.addAll(newTips);
    }

    public void replaceContraindications(List<ExerciseContraindication> newItems) {
        this.contraindications.clear();
        this.contraindications.addAll(newItems);
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate() {
        this.active = true;
    }
}