package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.cache.CachesNames;
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

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static io.github.lucasfcz.olympusprotocol.cache.CachesNames.*;

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
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId")
    })
    public WorkoutPlanResponse create(UUID userId, WorkoutPlanRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var activePlan = workoutPlanRepository.findByUserAndActiveTrueWithDetails(user);
        activePlan.ifPresent(WorkoutPlan::deactivate);

        var plan = new WorkoutPlan(user, request.name());
        plan.updateGoal(request.goal());

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId")
    })
    public WorkoutPlanResponse copyWorkoutPlan(UUID userId, UUID originalPlanId, WorkoutPlanRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        var originalPlan = workoutPlanRepository.findByIdWithDetails(originalPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", originalPlanId));

        if (!originalPlan.isPublic()) {
            throw new ForbiddenException("This workout plan is not public.");
        }
        var activePlan = workoutPlanRepository.findByUserIdAndActiveTrue(userId);
        activePlan.ifPresent(WorkoutPlan::deactivate);

        var newPlan = new WorkoutPlan(user, request.name());
        newPlan.updateGoal(originalPlan.getGoal());
        workoutPlanRepository.save(newPlan);

        originalPlan.getWorkoutDays().stream()
                .sorted(Comparator.comparing(WorkoutDay::getDayOrder))
                .forEach(originalDay -> {
                    var newDay = new WorkoutDay(newPlan, originalDay.getName(), originalDay.getDayOrder());
                    newPlan.addDay(newDay);

                    originalDay.getExercises().stream()
                            .sorted(Comparator.comparing(WorkoutDayExercise::getExerciseOrder))
                            .forEach(originalExercise -> {
                                var newExercise = new WorkoutDayExercise(
                                        newDay,
                                        originalExercise.getExercise(),
                                        originalExercise.getExerciseOrder(),
                                        originalExercise.getSets(),
                                        originalExercise.getReps(),
                                        originalExercise.getRestTime()
                                );
                                newDay.addExercise(newExercise);
                            });
                });

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(newPlan));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ACTIVE_WORKOUT_PLAN, key = "#userId")
    public WorkoutPlanResponse findActivePlan(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var plan = workoutPlanRepository.findByUserAndActiveTrueWithDetails(user)
                .orElseThrow(() -> new ResourceNotFoundException("Active Workout Plan from user id", userId));

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = USER_WORKOUT_PLANS, key = "#userId")
    public List<WorkoutPlanResponse> findAllByUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return workoutPlanRepository.findAllByUserWithDetails(user)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlans from user id", userId))
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = WORKOUT_PLAN, key = "#planId")
    public WorkoutPlanResponse findById(UUID planId) {
        var plan = workoutPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse addDay(UUID userId, UUID planId, WorkoutDayRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = new WorkoutDay(plan, request.name(), request.dayOrder());

        plan.addDay(day);

        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse addExerciseToDay(UUID userId, UUID planId, UUID dayId, WorkoutDayExerciseRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = getDayById(plan.getId(), dayId);;

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
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse removeDay(UUID userId, UUID planId, UUID dayId) {
        var plan = getOwnedPlan(planId, userId);

        plan.removeDay(dayId);
        workoutPlanRepository.save(plan);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse removeExerciseFromDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId) {
        var plan = getOwnedPlan(planId, userId);

        var day = workoutDayRepository.findByIdAndWorkoutPlanId(dayId, plan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));

        day.removeExercise(exerciseId);
        workoutDayRepository.save(day);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse updateDay(UUID userId, UUID planId, UUID dayId, UpdateWorkoutDayRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = getDayById(plan.getId(), dayId);

        day.updateDay(request.name(), request.dayOrder());
        workoutDayRepository.save(day);

        return workoutPlanMapper.toResponse(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse updateExerciseInDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId, UpdateWorkoutDayExerciseRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = getDayById(plan.getId(), dayId);

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
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
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
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public WorkoutPlanResponse reorderExercisesInDay(UUID userId, UUID planId, UUID dayId, ReorderExercisesRequest request) {
        var plan = getOwnedPlan(planId, userId);

        var day = getDayById(plan.getId(), dayId);

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
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public void deactivate(UUID userId, UUID planId) {
        var plan = getOwnedPlan(planId, userId);

        plan.deactivate();
        workoutPlanRepository.save(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public void reactivate(UUID userId, UUID planId) {
        var hasActivePlan = workoutPlanRepository.findByUserIdAndActiveTrue(userId);
        hasActivePlan.ifPresent(WorkoutPlan::deactivate);

        var plan = getOwnedPlan(planId, userId);

        plan.reactivate();
        workoutPlanRepository.save(plan);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = WORKOUT_PLAN, key = "#planId")
    })
    public void changeVisibility(UUID userId, UUID planId) {
        var plan = getOwnedPlan(planId, userId);

        plan.changeVisibility();

        workoutPlanRepository.save(plan);
    }

    // Helpers Methods
    private WorkoutPlan getOwnedPlan(UUID planId, UUID userId) {
        var plan = workoutPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));
        checkOwnership(plan, userId);

        return plan;
    }

    private WorkoutDay getDayById(UUID planId, UUID dayId) {
        var plan = workoutPlanRepository.findByIdWithDetails(planId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan", planId));

        return plan.getWorkoutDays().stream()
                .filter(wd -> wd.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", dayId));
    }

    private void checkOwnership(WorkoutPlan plan, UUID userId) {
        if (!plan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout plan.");
        }
    }
}