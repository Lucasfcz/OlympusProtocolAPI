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
            @RequestBody @Valid WorkoutPlanRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.create(user.getId(), request));
    }

    @Operation(summary = "List all workout plans", description = "Retrieve all workout plans of the authenticated user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout plans retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping
    public ResponseEntity<List<WorkoutPlanResponse>> findAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutPlanService.findAllByUser(user.getId()));
    }

    @Operation(summary = "Get workout plan by ID", description = "Retrieve a specific workout plan by its ID for the authenticated user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout plan retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> findById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId
    ) {
        return ResponseEntity.ok(workoutPlanService.findById(user.getId(), planId));
    }

    @Operation(summary = "Add a day to workout plan", description = "Add a new workout day to an existing workout plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workout day added successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @PostMapping("/{planId}/days")
    public ResponseEntity<WorkoutPlanResponse> addDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @RequestBody @Valid WorkoutDayRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.addDay(user.getId(), planId, request));
    }

    @Operation(summary = "Add exercise to workout day", description = "Add an exercise to a specific day within a workout plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Exercise added to day successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan, day, or exercise not found"
            )
    })
    @PostMapping("/{planId}/days/{dayId}/exercises")
    public ResponseEntity<WorkoutPlanResponse> addExercise(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid WorkoutDayExerciseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.addExerciseToDay(user.getId(), planId, dayId, request));
    }

    @Operation(summary = "Remove day from workout plan", description = "Delete a specific workout day and all its exercises from a workout plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout day removed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan or day not found"
            )
    })
    @DeleteMapping("/{planId}/days/{dayId}")
    public ResponseEntity<WorkoutPlanResponse> removeDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId
    ) {
        return ResponseEntity.ok(workoutPlanService.removeDay(user.getId(), planId, dayId));
    }

    @Operation(summary = "Remove exercise from day", description = "Delete a specific exercise from a workout day")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Exercise removed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan, day, or exercise not found"
            )
    })
    @DeleteMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutPlanResponse> removeExerciseFromDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId
    ) {
        return ResponseEntity.ok(workoutPlanService.removeExerciseFromDay(user.getId(), planId, dayId, exerciseId));
    }

    @Operation(summary = "Update workout day", description = "Update the details of a specific workout day within a workout plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout day updated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan or day not found"
            )
    })
    @PutMapping("/{planId}/days/{dayId}")
    public ResponseEntity<WorkoutPlanResponse> updateDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid UpdateWorkoutDayRequest request
    ) {
        return ResponseEntity.ok(workoutPlanService.updateDay(user.getId(), planId, dayId, request));
    }

    @Operation(summary = "Update exercise in day", description = "Update a specific exercise within a workout day")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise updated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan, day, or exercise not found"
            )
    })
    @PutMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutPlanResponse> updateExerciseInDay(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @PathVariable UUID exerciseId,
            @RequestBody @Valid UpdateWorkoutDayExerciseRequest request
    ) {
        return ResponseEntity.ok(workoutPlanService.updateExerciseInDay(user.getId(), planId, dayId, exerciseId, request));
    }

    @Operation(summary = "Deactivate workout plan", description = "Deactivate a workout plan by setting its status to inactive")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout plan deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @PatchMapping("/{planId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId
    ) {
        workoutPlanService.deactivate(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivate workout plan", description = "Reactivate a previously deactivated workout plan by setting its status to active")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout plan reactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @PatchMapping("/{planId}/reactivate")
    public ResponseEntity<Void> reactivate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId
    ) {
        workoutPlanService.reactivate(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reorder workout days", description = "Reorder the workout days within a workout plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout days reordered successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @PatchMapping("/{planId}/days/reorder")
    public ResponseEntity<WorkoutPlanResponse> reorderDays(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @RequestBody @Valid ReorderDaysRequest request
    ) {
        return ResponseEntity.ok(workoutPlanService.reorderDays(user.getId(), planId, request));
    }

    @Operation(summary = "Reorder exercises in day", description = "Reorder the exercises within a specific workout day")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercises reordered successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan or day not found"
            )
    })
    @PatchMapping("/{planId}/days/{dayId}/exercises/reorder")
    public ResponseEntity<WorkoutPlanResponse> reorderExercisesInDay(@AuthenticationPrincipal User user,
            @PathVariable UUID planId,
            @PathVariable UUID dayId,
            @RequestBody @Valid ReorderExercisesRequest request
    ) {
        return ResponseEntity.ok(workoutPlanService.reorderExercisesInDay(user.getId(), planId, dayId, request));
    }

    @Operation(summary = "Change workout plan visibility", description = "Toggle the visibility status of a workout plan (public/private)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout plan visibility changed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan not found"
            )
    })
    @PatchMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> changeVisibility(
            @AuthenticationPrincipal User user,
            @PathVariable UUID planId
    ) {
        workoutPlanService.changeVisibility(user.getId(), planId);
        return ResponseEntity.noContent().build();
    }
}