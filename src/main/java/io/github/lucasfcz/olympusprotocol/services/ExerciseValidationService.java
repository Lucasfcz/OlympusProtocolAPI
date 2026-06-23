package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExerciseValidationService {

    public Optional<String> checkLevelCompatibility(Exercise exercise, User user) {
        if (exercise.getRecommendedExperienceLevel().ordinal() > user.getExperienceLevel().ordinal()) {
            return Optional.of("This exercise is recommended for "
                    + exercise.getRecommendedExperienceLevel()
                    + " and your level is " + user.getExperienceLevel()
                    + ". Be careful.");
        }
        return Optional.empty();
    }
}
