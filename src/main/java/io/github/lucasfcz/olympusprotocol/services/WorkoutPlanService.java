package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutPlanRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutPlanResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutPlanMapper;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDayExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutDayRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanMapper workoutPlanMapper;
    private final ExerciseValidationService exerciseValidationService;

    public WorkoutPlanResponse create(UUID userId, WorkoutPlanRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var plan = new WorkoutPlan(user, request.name());
        plan.updateGoal(request.goal());

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    public List<WorkoutPlanResponse> findAllByUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return workoutPlanRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    public WorkoutPlanResponse findById(UUID userId, UUID planId) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        checkOwnership(plan, userId);

        return workoutPlanMapper.toResponse(plan);
    }

    public WorkoutPlanResponse addDay(UUID userId, UUID planId, WorkoutDayRequest request) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        checkOwnership(plan, userId);

        var day = new WorkoutDay(plan, request.name(), request.dayOrder());
        plan.addDay(day);

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    public WorkoutPlanResponse addExerciseToDay(UUID userId, UUID planId,
                                                UUID dayId,
                                                WorkoutDayExerciseRequest request) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        checkOwnership(plan, userId);

        var day = workoutDayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        var exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.exerciseId()));

        var dayExercise = new WorkoutDayExercise(
                day, exercise,
                request.exerciseOrder(),
                request.sets(),
                request.reps(),
                request.restTime()
        );

        day.addExercise(dayExercise);
        workoutDayRepository.save(day);

        var warning = exerciseValidationService.checkLevelCompatibility(exercise, plan.getUser());

        return warning.isPresent()
                ? workoutPlanMapper.toResponse(plan, List.of(warning.get()))
                : workoutPlanMapper.toResponse(plan);
    }

    public void deactivate(UUID userId, UUID planId) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        checkOwnership(plan, userId);
        plan.deactivate();
        workoutPlanRepository.save(plan);
    }

    public void reactivate(UUID userId, UUID planId) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        checkOwnership(plan, userId);
        plan.reactivate();
        workoutPlanRepository.save(plan);
    }

    private void checkOwnership(WorkoutPlan plan, UUID userId) {
        if (!plan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout plan.");
        }
    }
}