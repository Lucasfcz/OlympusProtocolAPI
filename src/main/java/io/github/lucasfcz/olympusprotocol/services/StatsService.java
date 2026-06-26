package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.responses.*;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSessionSetRepository workoutSessionSetRepository;

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(UUID userId) {
        var user = getUserOrThrow(userId);

        var totalSessions = workoutSessionRepository.countTotalOfSessionsFromUser(user);
        var totalSets = workoutSessionSetRepository.totalOfSetsFromUser(user);
        var totalVolume = workoutSessionRepository.totalVolumeAllTime(user);
        var totalMinutes = workoutSessionRepository.totalMinutesTrained(user);
        var mostUsed = workoutSessionSetRepository.findMostUsedExercise(user);

        return new UserStatsResponse(
                totalSessions,
                totalSets,
                totalVolume != null ? totalVolume : 0.0,
                totalMinutes != null ? totalMinutes : 0,
                mostUsed
        );
    }

    @Transactional(readOnly = true)
    public MuscleVolumeResponse getAllVolumeFromMuscle(UUID userId, MuscleGroup muscleGroup) {
        var user = getUserOrThrow(userId);
        var totalVolumeOfMuscle = workoutSessionRepository.totalVolumeOfMuscleByUser(muscleGroup, user);

        return new MuscleVolumeResponse(
                muscleGroup,
                totalVolumeOfMuscle
        );
    }
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public WeeklyVolumeResponse getWeeklyVolume(UUID userId) {
        var user = getUserOrThrow(userId);
        var today = LocalDate.now();
        var start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        var end = start.plusDays(6).with(LocalTime.MAX);

        var sessions = workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(user, start, end);

        Map<DayOfWeek, Double> volumeByDay = new EnumMap<>(DayOfWeek.class);
        Arrays.stream(DayOfWeek.values()).forEach(d -> volumeByDay.put(d, 0.0));

        sessions.forEach(session -> {
            var day = session.getStartedAt().getDayOfWeek();
            volumeByDay.merge(day, session.getTotalVolume(), Double::sum);
        });

        var volumes = Arrays.stream(DayOfWeek.values())
                .map(day -> new DailyVolumeResponse(day, volumeByDay.get(day)))
                .toList();

        return new WeeklyVolumeResponse(volumes);
    }

    @Transactional(readOnly = true)
    public FrequencyResponse getMonthlyFrequency(UUID userId) {
        var user = getUserOrThrow(userId);
        var today = LocalDate.now();
        var start = today.withDayOfMonth(1).atStartOfDay();
        var end = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        var sessions = workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(user, start, end);

        var totalSessions = sessions.size();
        var totalDaysInMonth = today.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        var weeksInPeriod = Math.max(1, totalDaysInMonth / 7.0);
        var avgSessionsPerWeek = totalSessions / weeksInPeriod;

        var sessionsPerWeek = sessions.stream()
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

    // Helpers methods
    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
    }

    private Exercise getExerciseOrThrow(UUID exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseId", exerciseId));
    }
}