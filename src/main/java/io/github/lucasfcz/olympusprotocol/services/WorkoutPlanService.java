package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.*;
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
import jakarta.transaction.Transactional;
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

    @Transactional
    public WorkoutPlanResponse create(UUID userId, WorkoutPlanRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var plan = new WorkoutPlan(user, request.name());
        plan.updateGoal(request.goal());

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public List<WorkoutPlanResponse> findAllByUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return workoutPlanRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    @Transactional
    public WorkoutPlanResponse findById(UUID userId, UUID planId) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse addDay(UUID userId, UUID planId, WorkoutDayRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = new WorkoutDay(plan, request.name(), request.dayOrder());
        plan.addDay(day);

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse addExerciseToDay(UUID userId, UUID planId, UUID dayId, WorkoutDayExerciseRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, planId)
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

    @Transactional
    public WorkoutPlanResponse removeDay(UUID userId, UUID planId, UUID dayId) {
        var plan = getOwnedPlan(planId, userId);

        plan.removeDay(dayId);
        workoutPlanRepository.save(plan);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse removeExerciseFromDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        day.removeExercise(exerciseId);
        workoutDayRepository.save(day);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse updateDay(UUID userId, UUID planId, UUID dayId, UpdateWorkoutDayRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        day.updateDay(request.name(), request.dayOrder());
        workoutDayRepository.save(day);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse updateExerciseInDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId, UpdateWorkoutDayExerciseRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        var exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.exerciseId()));

        var dayExercise = day.getExercises().stream()
                .filter(de -> de.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Exercise in Day", exerciseId));

        dayExercise.updateExercise(exercise, request.exerciseOrder(), request.sets(), request.reps(), request.restTime());
        workoutDayRepository.save(day);

        var warning = exerciseValidationService.checkLevelCompatibility(exercise, plan.getUser());

        return warning.isPresent()
                ? workoutPlanMapper.toResponse(plan, List.of(warning.get()))
                : workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public WorkoutPlanResponse reorderDays(UUID userId, UUID planId, ReorderDaysRequest request) {
        var plan = getOwnedPlan(planId, userId);

        request.orders().forEach(item ->
                plan.getWorkoutDays().stream()
                        .filter(d -> d.getId().equals(item.dayId()))
                        .findFirst()
                        .ifPresent(d -> d.updateDay(d.getName(), item.order()))
        );

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse reorderExercisesInDay(UUID userId, UUID planId, UUID dayId, ReorderExercisesRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        request.orders().forEach(item ->
                day.getExercises().stream()
                        .filter(e -> e.getId().equals(item.exerciseId()))
                        .findFirst()
                        .ifPresent(e -> e.updateOrder(item.order()))
        );

        workoutDayRepository.save(day);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    public void deactivate(UUID userId, UUID planId) {
        var plan = getOwnedPlan(planId, userId);

        plan.deactivate();
        workoutPlanRepository.save(plan);
    }

    @Transactional
    public void reactivate(UUID userId, UUID planId) {
        var plan = getOwnedPlan(planId, userId);

        plan.reactivate();
        workoutPlanRepository.save(plan);
    }

    @Transactional
    public void changeVisibility(UUID userId, UUID planId) {
        var plan = getOwnedPlan(planId, userId);

        plan.changeVisibility();

        workoutPlanRepository.save(plan);
    }

    // Helpers Methods

    private WorkoutPlan getOwnedPlan(UUID planId, UUID userId) {
        var plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));
        checkOwnership(plan, userId);

        return plan;
    }

    private void checkOwnership(WorkoutPlan plan, UUID userId) {
        if (!plan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout plan.");
        }
    }
}