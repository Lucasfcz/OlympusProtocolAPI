package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.responses.*;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "User stats", description = "Get statistics for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/me") // Changed endpoint to /user/me
    public ResponseEntity<UserStatsResponse> userStats(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(statsService.getUserStats(user.getId()));
    }


    @Operation(summary = "Exercise stats", description = "Get statistics for a specific exercise for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<ExerciseStatsResponse> exerciseStatsOfUser(
            @AuthenticationPrincipal User user,
            @PathVariable UUID exerciseId
    ) {
        return ResponseEntity.ok(statsService.getExerciseStats(user.getId(), exerciseId));
    }

    @Operation(summary = "All volume accumulated by muscle", description = "Get statistics for a specific muscle for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All muscle volume retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/volume/muscle/{muscleGroup}")
    public ResponseEntity<MuscleVolumeResponse> getAllVolumeFromMuscleByUser(
            @AuthenticationPrincipal User user,
            @PathVariable @RequestParam @Valid MuscleGroup muscleGroup
    )  {
        return ResponseEntity.ok(statsService.getAllVolumeFromMuscle(user.getId(), muscleGroup));
    }

    @Operation(summary = "Weekly volume", description = "Get the authenticated user's weekly training volume")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weekly volume retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/volume/weekly")
    public ResponseEntity<WeeklyVolumeResponse> weeklyVolume(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getWeeklyVolume(user.getId()));
    }

    @Operation(summary = "Monthly frequency", description = "Get the authenticated user's monthly training frequency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly frequency retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/frequency/monthly")
    public ResponseEntity<FrequencyResponse> getUserFrequency(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(statsService.getMonthlyFrequency(user.getId()));
    }

    @Operation(summary = "Muscle volume change by last session", description = "Get the percentage change in muscle volume between the last two completed sessions with a workout plan for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muscle volume changes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Not enough completed sessions with a workout plan to compare volume changes.")
    })
    @GetMapping("/volume/change/last-session")
    public ResponseEntity<List<MuscleVolumeChangeResponse>> getMuscleVolumeChangeByLastSession(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(statsService.getMuscleVolumeChangeByLastSession(user.getId()));
    }
}
