package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseStatsResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.FrequencyResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WeeklyVolumeResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

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
}
