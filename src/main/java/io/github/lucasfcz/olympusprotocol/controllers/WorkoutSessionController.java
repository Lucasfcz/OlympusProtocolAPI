package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.*;
import io.github.lucasfcz.olympusprotocol.dto.responses.SessionSummaryResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.WorkoutSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @Operation(summary = "Start a session of exercises from plan", description = "Starts a session using exercises from a plan")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful in start a session from plan"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout plan Not found"
            )
    })
    @PostMapping("from-plan/{workoutDayId}")
    public ResponseEntity<WorkoutSessionResponse> startFromPlan(
            @AuthenticationPrincipal User user,
            @PathVariable UUID workoutDayId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.startFromWorkoutDay(user.getId(), workoutDayId));
    }

    @Operation(summary = "Start session without a plan", description = "Starts a session without a workout plan, the user will have to select the exercises and sets")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful in start a session without a plan"
            )     ,
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping("/free")
    public ResponseEntity<WorkoutSessionResponse> startFreeSession(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.startFreeSession(user.getId()));
    }

    @Operation(summary = "See details about one past session", description = "Show details about one session: duration, volume, all exercises, all sets")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful in take past sessions details"
            )     ,
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "You are not allowed to see this session details"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<SessionSummaryResponse> getSessionSummary(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(workoutSessionService.getSessionSummary(user.getId(), sessionId));
    }

    @Operation(summary = "Find a session by ID", description = "Find a session with Session ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful in find session by id"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found by id"
            )
    })
    @GetMapping("/{sessionId}")
    public ResponseEntity<WorkoutSessionResponse> findById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(workoutSessionService.findById(user.getId(), sessionId));
    }

    @Operation(summary = "List user sessions", description = "Retrieve all workout sessions of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<WorkoutSessionResponse>> findAllByUser(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(workoutSessionService.findAllByUser(user.getId()));
    }

    @Operation(summary = "Add exercise to session", description = "Add an exercise to an existing workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercise added to session"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session or exercise not found")
    })
    @PostMapping("/{sessionId}/exercises")
    public ResponseEntity<WorkoutSessionResponse> addExercise(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @RequestBody @Valid AddExerciseToSessionRequest request
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.addExercise(user.getId(), sessionId, request));
    }

    @Operation(summary = "Remove exercise from session", description = "Remove an exercise from a workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session or exercise not found")
    })
    @DeleteMapping({"/{sessionId}/exercises/{sessionExerciseId}"})
    public ResponseEntity<WorkoutSessionResponse> removeExercise(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID sessionExerciseId
    ) {
        WorkoutSessionResponse resp = workoutSessionService.removeExercise(user.getId(), sessionId, sessionExerciseId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{sessionId}/exercises/{sessionExerciseId}")
    public ResponseEntity<WorkoutSessionResponse> addSet(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID sessionExerciseId,
            @RequestBody @Valid SetRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.addSet(user.getId(), sessionId, sessionExerciseId, request));
    }

    @DeleteMapping("/{sessionId}/exercises/{exerciseId}/sets/{setId}")
    public ResponseEntity<WorkoutSessionResponse> removeSet(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID exerciseId,
            @PathVariable UUID setId
    ) {
        return ResponseEntity.ok(workoutSessionService.removeSet(user.getId(), sessionId, exerciseId, setId));
    }

    @PutMapping("/{sessionId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutSessionResponse> updateSessionExercise(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID exerciseId,
            @RequestBody@Valid UpdateSessionExerciseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.updateSessionExercise(user.getId(), sessionId, exerciseId, request));
    }

    @PutMapping("/{sessionId}/exercises/sets/{setId}")
    public ResponseEntity<WorkoutSessionResponse> updateSet(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID setId,
            @RequestBody @Valid SetRequest request
    ) {
        return ResponseEntity.ok(workoutSessionService.updateSet(user.getId(), sessionId, setId, request));
    }

    @PatchMapping("/{sessionId}/exercises")
    public ResponseEntity<WorkoutSessionResponse> reorderExercises(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @RequestBody @Valid ReorderExercisesRequest request
            ) {
        return ResponseEntity.ok(workoutSessionService.reorderExercises(user.getId(), sessionId, request));
    }

    @PatchMapping("/{sessionId}/exercises/{exerciseId}/sets")
    public ResponseEntity<WorkoutSessionResponse> reorderSets(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @PathVariable UUID exerciseId,
            @RequestBody @Valid ReorderSetsRequest request
    ) {
        return ResponseEntity.ok(workoutSessionService.reorderSets(user.getId(), sessionId, exerciseId, request));
    }

    @Operation(summary = "Finish session and show session summary", description = "Finish a workout session providing optional notes and summary")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session finished successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PatchMapping("/{sessionId}/finish")
    public ResponseEntity<SessionSummaryResponse> finishSession(
            @AuthenticationPrincipal User user,
            @PathVariable UUID sessionId,
            @RequestBody @Valid FinishSessionRequest request
    ) {
        return ResponseEntity.ok(workoutSessionService.finishSession(user.getId(), sessionId, request));
    }
}
