package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutDayRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.WorkoutPlanRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutPlanResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.WorkoutPlanService;
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

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.create(user.getId(), request));
    }

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
}