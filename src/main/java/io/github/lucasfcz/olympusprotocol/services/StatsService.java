package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.responses.*;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionSetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service                         // REMEMBER IN FUTURE UPGRADE QUERIES TO STATS SERVICE
@RequiredArgsConstructor        // HAVE TO CHANGE QUERIES TO TAKE INFORMATION DIRECT OF DB
public class StatsService {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSessionSetRepository workoutSessionSetRepository;

    @Transactional
    public ExerciseStatsResponse getExerciseStats(UUID userId, UUID exerciseId) {
        var user = getUserOrThrow(userId);
        var exercise = getExerciseOrThrow(exerciseId);

        var sets = workoutSessionSetRepository
                .findCompletedSetsByUserAndExercise(user, exercise);

        var totalSets = sets.size();

        var setsWithWeight = sets.stream()
                .filter(s -> s.getWeight() != null)
                .toList();

        if (setsWithWeight.isEmpty()) {
            return new ExerciseStatsResponse(
                    exerciseId, exercise.getName(), totalSets,
                    null, null, null, List.of()
            );
        }

        var setWithPR = setsWithWeight.stream()
                .max(Comparator.comparing(WorkoutSessionSet::getWeight))
                .orElseThrow();

        var progression = setsWithWeight.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getWorkoutSessionExercise()
                                .getWorkoutSession()
                                .getStartedAt()
                                .toLocalDate(),
                        TreeMap::new,
                        Collectors.maxBy(Comparator.comparing(WorkoutSessionSet::getWeight))
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .map(e -> new ChartPointResponse(e.getKey(), e.getValue().get().getWeight()))
                .toList();

        return new ExerciseStatsResponse(
                exerciseId,
                exercise.getName(),
                totalSets,
                setWithPR.getWeight(),
                setWithPR.getReps(),
                setWithPR.getWorkoutSessionExercise().getWorkoutSession().getStartedAt(),
                progression
        );
    }

    // This method take user daily volume, to show in weekly volume graphic all volume he got in week
    // Between Monday and Sunday
    @Transactional
    public WeeklyVolumeResponse getWeeklyVolume(UUID userId) {

        Map<DayOfWeek, Double> volumeOfDay = new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek day : DayOfWeek.values()) {
            volumeOfDay.put(day, 0.0);
        }
        var today = LocalDate.now();
        var startDateTime = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        var endDateTime = startDateTime.plusDays(6).with(LocalTime.MAX);
        var user = getUserOrThrow(userId);
        var weeklySessions = workoutSessionRepository.findByUserAndFinishedAtIsNotNullAndStartedAtBetween(user, startDateTime, endDateTime);
        for (WorkoutSession session : weeklySessions) {
            DayOfWeek day = session.getStartedAt().getDayOfWeek();
            volumeOfDay.put(
                    day,
                    volumeOfDay.get(day) + session.getTotalVolume()
            );
        }

        List<DailyVolumeResponse> volumes = Arrays.stream(DayOfWeek.values())
                .map(day -> new DailyVolumeResponse(
                        day,
                        volumeOfDay.get(day)
                ))
                .toList();

        return new WeeklyVolumeResponse(volumes);
    }

    @Transactional
    public FrequencyResponse getMonthlyFrequency(UUID userId) {
        var user = getUserOrThrow(userId);

        var today = LocalDate.now();
        var startOfMonth = today.withDayOfMonth(1);
        var endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        var monthlySessions = workoutSessionRepository
                .findByUserAndFinishedAtIsNotNullAndStartedAtBetween(
                        user,
                        startOfMonth.atStartOfDay(),
                        endOfMonth.atTime(LocalTime.MAX)
                );

        var totalSessions = monthlySessions.size();
        var totalDaysInMonth = endOfMonth.getDayOfMonth();
        var weeksInPeriod = Math.max(1, totalDaysInMonth / 7.0);
        var avgSessionsPerWeek = totalSessions / weeksInPeriod;

        // match sessions by week in month
        var sessionsPerWeek = monthlySessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartedAt().toLocalDate()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new ChartPointResponse(e.getKey(), (double) e.getValue()))
                .toList();

        return new FrequencyResponse(totalSessions, totalDaysInMonth, avgSessionsPerWeek, sessionsPerWeek);
    }

    // Helpers Methods
    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
    }

    private Exercise getExerciseOrThrow(UUID exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseId", exerciseId));
    }
}
