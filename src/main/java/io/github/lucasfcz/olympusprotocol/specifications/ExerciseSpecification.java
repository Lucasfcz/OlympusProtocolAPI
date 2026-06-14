package io.github.lucasfcz.olympusprotocol.specifications;

import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.enums.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ExerciseSpecification {
    public static Specification<Exercise> filters(
            String name,
            List<MuscleGroup> muscleGroups,
            List<SafetyRating> safetyRatings,
            List<EfficiencyRating> efficiencyRatings,
            List<ExperienceLevel> levels,
            List<MuscleHead> muscleHeads) {

        return Specification
                .where(isActive())
                .and(hasName(name))
                .and(hasMuscleGroups(muscleGroups))
                .and(hasSafetyRatings(safetyRatings))
                .and(hasEfficiencyRatings(efficiencyRatings))
                .and(hasLevels(levels))
                .and(hasMuscleHeads(muscleHeads));
    }

    private static Specification<Exercise> isActive() {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.isTrue(root.get("active"));
        };
    }

    private static Specification<Exercise> hasName(String name) {
        return (root, query, cb) -> name == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Exercise> hasMuscleGroups(List<MuscleGroup> groups) {
        return (root, query, cb) -> (groups == null || groups.isEmpty()) ? null :
                root.join("muscles").get("muscleGroup").in(groups);
    }

    private static Specification<Exercise> hasMuscleHeads(List<MuscleHead> heads) {
        return (root, query, cb) -> (heads == null || heads.isEmpty()) ? null :
                root.join("muscles").get("muscleHead").in(heads);
    }

    private static Specification<Exercise> hasSafetyRatings(List<SafetyRating> ratings) {
        return (root, query, cb) -> (ratings == null || ratings.isEmpty()) ? null :
                root.get("safetyRating").in(ratings);
    }

    private static Specification<Exercise> hasEfficiencyRatings(List<EfficiencyRating> ratings) {
        return (root, query, cb) -> (ratings == null || ratings.isEmpty()) ? null :
                root.get("efficiencyRating").in(ratings);
    }

    private static Specification<Exercise> hasLevels(List<ExperienceLevel> levels) {
        return (root, query, cb) -> (levels == null || levels.isEmpty()) ? null :
                root.get("minExperienceLevel").in(levels);
    }
}