package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseStatsResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.FrequencyResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WeeklyVolumeResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.StatsService;
import jakarta.validation.Valid;
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

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<ExerciseStatsResponse> exerciseStatsOfUser(
            @AuthenticationPrincipal User user,
            @PathVariable UUID exerciseId
    ) {
        return ResponseEntity.ok(statsService.getExerciseStats(user.getId(), exerciseId));
    }

    @GetMapping("/volume/weekly")
    public ResponseEntity<WeeklyVolumeResponse> weeklyVolume(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getWeeklyVolume(user.getId()));
    }

    @GetMapping("/frequency/monthly")
    public ResponseEntity<FrequencyResponse> getUserFrequency(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(statsService.getMonthlyFrequency(user.getId()));
    }
}
