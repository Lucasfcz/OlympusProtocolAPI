package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.*;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutPlanResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.WorkoutPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @Operation(summary = "Create a workout plan", description = "User create a workout plan with one list of workouts day and these have one list of workouts exercises")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful in create a new workout plan"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.create(user.getId(), request));
    }

    @Operation(summary = "List all exercises", description = "show all exercises, also can use some filters")
    @GetMapping
    public ResponseEntity<List<WorkoutPlanResponse>> findAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutPlanService.findAllByUser(user.getId()));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> findById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId) {
        return ResponseEntity.ok(workoutPlanService.findById(user.getId(), planId));
    }

    @PostMapping("/{planId}/days")
    public ResponseEntity<WorkoutPlanResponse> addDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @RequestBody @Valid WorkoutDayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.addDay(user.getId(), planId, request));
    }

    @PostMapping("/{planId}/days/{dayId}/exercises")
    public ResponseEntity<WorkoutPlanResponse> addExercise(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid WorkoutDayExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.addExerciseToDay(user.getId(), planId, dayId, request));
    }

    @DeleteMapping("/{planId}/days/{dayId}")
    public ResponseEntity<Void> removeDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId) {
        workoutPlanService.removeDay(user.getId(), planId, dayId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<Void> removeExerciseFromDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId) {
        workoutPlanService.removeExerciseFromDay(user.getId(), planId, dayId, exerciseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{planId}/days/{dayId}")
    public ResponseEntity<WorkoutPlanResponse> updateDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid UpdateWorkoutDayRequest request) {
        return ResponseEntity.ok(workoutPlanService.updateDay(user.getId(), planId, dayId, request));
    }

    @PutMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutPlanResponse> updateExerciseInDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId,
            @RequestBody @Valid UpdateWorkoutDayExerciseRequest request) {
        return ResponseEntity.ok(workoutPlanService.updateExerciseInDay(user.getId(), planId, dayId, exerciseId, request));
    }

    @PatchMapping("/{planId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId) {
        workoutPlanService.deactivate(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{planId}/reactivate")
    public ResponseEntity<Void> reactivate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId) {
        workoutPlanService.reactivate(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{planId}/days/reorder")
    public ResponseEntity<WorkoutPlanResponse> reorderDays(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @RequestBody @Valid ReorderDaysRequest request) {
        return ResponseEntity.ok(workoutPlanService.reorderDays(user.getId(), planId, request));
    }

    @PatchMapping("/{planId}/days/{dayId}/exercises/reorder")
    public ResponseEntity<WorkoutPlanResponse> reorderExercisesInDay(@AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid ReorderExercisesInDayRequest request) {
        return ResponseEntity.ok(workoutPlanService.reorderExercisesInDay(user.getId(), planId, dayId, request));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<Void> changeVisibility(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId) {
        workoutPlanService.changeVisibility(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }
}