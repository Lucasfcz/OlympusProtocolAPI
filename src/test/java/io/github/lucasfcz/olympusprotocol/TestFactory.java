package io.github.lucasfcz.olympusprotocol;

import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutPlanRequest;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import io.github.lucasfcz.olympusprotocol.models.enums.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class TestFactory {

    public static User makeUser(UUID id, ExperienceLevel level) {
        var user = new User("Leonardo De Caprio", "test@gmail", "testpassword", ExperienceLevel.BEGINNER, 102.3, 1.90);
        setPrivateField(user, "id", id);
        return user;
    }

    public static User makeUser(ExperienceLevel level) {
        return makeUser(UUID.randomUUID(), level);
    }

    public static Exercise makeExercise(ExperienceLevel level) {
        return new Exercise("Squat", "Strong legs exercise", level, SafetyRating.SAFE, EfficiencyRating.HIGH, "notes", "gif", false);
    }

    public static WorkoutPlan makePlan(User user) {
        return new WorkoutPlan(user, "My Plan");
    }

    public static ExerciseRequest sampleExerciseRequest() {
        return new ExerciseRequest(
                "Push Up",
                "Upper body exercise",
                ExperienceLevel.BEGINNER,
                SafetyRating.SAFE,
                EfficiencyRating.MEDIUM,
                "Keep chest close to ground",
                "gif_url",
                false,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static WorkoutPlanRequest samplePlanRequest() {
        return new WorkoutPlanRequest("My Plan", WorkoutGoal.HYPERTROPHY);
    }

    public static WorkoutDayRequest sampleDayRequest() {
        return new WorkoutDayRequest("Upper Body Day", 1);
    }

    public static WorkoutDayExerciseRequest sampleDayExerciseRequest(UUID exerciseId) {
        return new WorkoutDayExerciseRequest(exerciseId, 1, 3, 10, 60);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}

