package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseResponse;
import io.github.lucasfcz.olympusprotocol.models.enums.*;
import io.github.lucasfcz.olympusprotocol.services.ExerciseService;
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

    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@RequestBody @Valid ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<MuscleGroup> muscleGroups,
            @RequestParam(required = false) List<SafetyRating> safetyRatings,
            @RequestParam(required = false) List<EfficiencyRating> efficiencyRatings,
            @RequestParam(required = false) List<ExperienceLevel> levels,
            @RequestParam(required = false) List<MuscleHead> muscleHeads) {
        return ResponseEntity.ok(
                exerciseService.findAll(name, muscleGroups, safetyRatings,
                        efficiencyRatings, levels, muscleHeads)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> update(@PathVariable UUID id,@RequestBody @Valid ExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        exerciseService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable UUID id) {
        exerciseService.reactivate(id);
        return ResponseEntity.noContent().build();
    }
}
