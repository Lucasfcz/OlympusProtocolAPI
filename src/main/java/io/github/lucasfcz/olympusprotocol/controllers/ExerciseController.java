package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseResponse;
import io.github.lucasfcz.olympusprotocol.models.enums.*;
import io.github.lucasfcz.olympusprotocol.services.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(summary = "Create a exercise")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful in create a new exercise"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )

    })
    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@RequestBody @Valid ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(request));
    }

    @Operation(summary = "Get exercise by ID", description = "Retrieve a specific exercise by its unique identifier")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise found successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }

    @Operation(summary = "List all exercises", description = "Retrieve all exercises with optional filters by name, muscle groups, safety ratings, efficiency ratings, experience levels, and muscle heads")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercises retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<MuscleGroup> muscleGroups,
            @RequestParam(required = false) List<SafetyRating> safetyRatings,
            @RequestParam(required = false) List<EfficiencyRating> efficiencyRatings,
            @RequestParam(required = false) List<ExperienceLevel> levels,
            @RequestParam(required = false) List<MuscleHead> muscleHeads
    ) {
        return ResponseEntity.ok(exerciseService.findAll(name, muscleGroups, safetyRatings, efficiencyRatings, levels, muscleHeads)
        );
    }

    @Operation(summary = "Update an exercise", description = "Update the details of an existing exercise by its ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> update(@PathVariable UUID id,@RequestBody @Valid ExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @Operation(summary = "Deactivate an exercise", description = "Deactivate an exercise by setting its status to inactive")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Exercise deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        exerciseService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivate an exercise", description = "Reactivate a previously deactivated exercise by setting its status to active")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Exercise reactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable UUID id) {
        exerciseService.reactivate(id);
        return ResponseEntity.noContent().build();
    }
}
