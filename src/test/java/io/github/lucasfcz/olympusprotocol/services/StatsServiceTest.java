package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.responses.*;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.models.*;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.MuscleGroup;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService Tests")
class StatsServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private WorkoutSessionSetRepository workoutSessionSetRepository;

    @InjectMocks
    private StatsService statsService;

    private User user;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        user = TestFactory.makeUser(USER_ID, ExperienceLevel.INTERMEDIATE);
        exercise = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.BEGINNER);
    }

    // -------------------------------------------------------------------------
    // Testes para getUserStats(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getUserStats: Deve retornar as estatísticas do usuário quando encontrado")
    void getUserStats_userFound_shouldReturnUserStats() {
        // Arrange
        Long totalSessions = 10L;
        Long totalSets = 100L;
        Double totalVolume = 5000.0;
        Long totalMinutes = 600L;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.countTotalOfSessionsFromUser(user)).thenReturn(totalSessions);
        when(workoutSessionSetRepository.totalOfSetsFromUser(user)).thenReturn(totalSets);
        when(workoutSessionRepository.totalVolumeAllTime(user)).thenReturn(totalVolume);
        when(workoutSessionRepository.totalMinutesTrained(user)).thenReturn(totalMinutes);

        // Act
        UserStatsResponse result = statsService.getUserStats(USER_ID);

        // Assert
        assertThat(result.totalSessions()).isEqualTo(totalSessions);
        assertThat(result.totalSets()).isEqualTo(totalSets);
        assertThat(result.totalVolumeAllTime()).isEqualTo(totalVolume); // Corrected field access
        assertThat(result.totalMinutesTrained()).isEqualTo(totalMinutes); // Corrected field access

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).countTotalOfSessionsFromUser(user);
        verify(workoutSessionSetRepository).totalOfSetsFromUser(user);
        verify(workoutSessionRepository).totalVolumeAllTime(user);
        verify(workoutSessionRepository).totalMinutesTrained(user);
    }

    @Test
    @DisplayName("getUserStats: Deve retornar estatísticas com valores zero quando não há dados")
    void getUserStats_noData_shouldReturnZeroStats() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.countTotalOfSessionsFromUser(user)).thenReturn(0L);
        when(workoutSessionSetRepository.totalOfSetsFromUser(user)).thenReturn(0L);
        when(workoutSessionRepository.totalVolumeAllTime(user)).thenReturn(null); // Repositories can return null for sum
        when(workoutSessionRepository.totalMinutesTrained(user)).thenReturn(null);

        // Act
        UserStatsResponse result = statsService.getUserStats(USER_ID);

        // Assert
        assertThat(result.totalSessions()).isEqualTo(0L);
        assertThat(result.totalSets()).isEqualTo(0L);
        assertThat(result.totalVolumeAllTime()).isEqualTo(0.0); // Corrected field access
        assertThat(result.totalMinutesTrained()).isEqualTo(0); // Corrected field access
    }

    @Test
    @DisplayName("getUserStats: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void getUserStats_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getUserStats(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionSetRepository);
    }

    // -------------------------------------------------------------------------
    // Testes para getAllVolumeFromMuscle(UUID userId, MuscleGroup muscleGroup)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllVolumeFromMuscle: Deve retornar o volume total para um grupo muscular")
    void getAllVolumeFromMuscle_validUserAndMuscleGroup_shouldReturnVolume() {
        // Arrange
        MuscleGroup muscleGroup = MuscleGroup.CHEST;
        Double expectedVolume = 1500.0;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.totalVolumeOfMuscleByUser(muscleGroup, user)).thenReturn(expectedVolume);

        // Act
        MuscleVolumeResponse result = statsService.getAllVolumeFromMuscle(USER_ID, muscleGroup);

        // Assert
        assertThat(result.muscleGroup()).isEqualTo(muscleGroup);
        assertThat(result.totalVolume()).isEqualTo(expectedVolume);

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).totalVolumeOfMuscleByUser(muscleGroup, user);
    }

    @Test
    @DisplayName("getAllVolumeFromMuscle: Deve retornar volume zero se não houver dados para o grupo muscular")
    void getAllVolumeFromMuscle_noVolumeForMuscleGroup_shouldReturnZero() {
        // Arrange
        MuscleGroup muscleGroup = MuscleGroup.BACK;
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.totalVolumeOfMuscleByUser(muscleGroup, user)).thenReturn(0.0); // Or null, service handles null

        // Act
        MuscleVolumeResponse result = statsService.getAllVolumeFromMuscle(USER_ID, muscleGroup);

        // Assert
        assertThat(result.muscleGroup()).isEqualTo(muscleGroup);
        assertThat(result.totalVolume()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getAllVolumeFromMuscle: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void getAllVolumeFromMuscle_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        MuscleGroup muscleGroup = MuscleGroup.QUADRICEPS; // Corrected symbol
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getAllVolumeFromMuscle(USER_ID, muscleGroup))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository);
    }

    // -------------------------------------------------------------------------
    // Testes para getExerciseStats(UUID userId, UUID exerciseId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getExerciseStats: Deve retornar as estatísticas do exercício com PR e progressão")
    void getExerciseStats_validUserAndExercise_shouldReturnStatsWithPRAndProgression() {
        // Arrange
        WorkoutSession session1 = TestFactory.makeActiveSession(user, LocalDateTime.now().minusDays(5)); // Using new factory method
        WorkoutSessionExercise wsExercise1 = TestFactory.makeSessionExercise(session1, exercise);
        io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet set1 = TestFactory.makeSetWithWeight(wsExercise1, 1, 10, 90.0);
        io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet set2 = TestFactory.makeSetWithWeight(wsExercise1, 2, 8, 95.0);
        wsExercise1.addSet(set1);
        wsExercise1.addSet(set2);
        session1.addExercise(wsExercise1);

        WorkoutSession session2 = TestFactory.makeActiveSession(user, LocalDateTime.now().minusDays(2)); // Using new factory method
        WorkoutSessionExercise wsExercise2 = TestFactory.makeSessionExercise(session2, exercise);
        io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet set3 = TestFactory.makeSetWithWeight(wsExercise2, 1, 5, 100.0); // PR
        wsExercise2.addSet(set3);
        session2.addExercise(wsExercise2);

        List<io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet> sets = List.of(set1, set2, set3);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(workoutSessionSetRepository.findCompletedSetsByUserAndExercise(user, exercise)).thenReturn(sets);

        // Act
        ExerciseStatsResponse result = statsService.getExerciseStats(USER_ID, EXERCISE_ID);

        // Assert
        assertThat(result.exerciseId()).isEqualTo(EXERCISE_ID);
        assertThat(result.exerciseName()).isEqualTo(exercise.getName());
        assertThat(result.totalSets()).isEqualTo(3);
        assertThat(result.maxWeight()).isEqualTo(100.0); // Corrected field access
        assertThat(result.repsIfMaxWeight()).isEqualTo(5); // Corrected field access
        assertThat(result.dayOfSetWithMaxWeight()).isEqualTo(session2.getStartedAt()); // Corrected field access
        assertThat(result.progression()).hasSize(2); // Two distinct dates with max weight
        assertThat(result.progression()).containsExactly(
                new ChartPointResponse(session1.getStartedAt().toLocalDate(), 95.0),
                new ChartPointResponse(session2.getStartedAt().toLocalDate(), 100.0)
        );

        verify(userRepository).findById(USER_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(workoutSessionSetRepository).findCompletedSetsByUserAndExercise(user, exercise);
    }

    @Test
    @DisplayName("getExerciseStats: Deve retornar estatísticas com valores nulos para peso se não houver sets com peso")
    void getExerciseStats_noSetsWithWeight_shouldReturnNullWeightStats() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise wsExercise = TestFactory.makeSessionExercise(session, exercise);
        io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet set = TestFactory.makeSetWithoutWeight(wsExercise); // No weight
        wsExercise.addSet(set);
        session.addExercise(wsExercise);

        List<io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet> sets = List.of(set);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(workoutSessionSetRepository.findCompletedSetsByUserAndExercise(user, exercise)).thenReturn(sets);

        // Act
        ExerciseStatsResponse result = statsService.getExerciseStats(USER_ID, EXERCISE_ID);

        // Assert
        assertThat(result.exerciseId()).isEqualTo(EXERCISE_ID);
        assertThat(result.exerciseName()).isEqualTo(exercise.getName());
        assertThat(result.totalSets()).isEqualTo(1);
        assertThat(result.maxWeight()).isNull(); // Corrected field access
        assertThat(result.repsIfMaxWeight()).isNull(); // Corrected field access
        assertThat(result.dayOfSetWithMaxWeight()).isNull(); // Corrected field access
        assertThat(result.progression()).isEmpty();

        verify(userRepository).findById(USER_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(workoutSessionSetRepository).findCompletedSetsByUserAndExercise(user, exercise);
    }

    @Test
    @DisplayName("getExerciseStats: Deve retornar estatísticas com valores zero/vazios se não houver sets")
    void getExerciseStats_noSets_shouldReturnEmptyStats() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(workoutSessionSetRepository.findCompletedSetsByUserAndExercise(user, exercise)).thenReturn(List.of());

        // Act
        ExerciseStatsResponse result = statsService.getExerciseStats(USER_ID, EXERCISE_ID);

        // Assert
        assertThat(result.exerciseId()).isEqualTo(EXERCISE_ID);
        assertThat(result.exerciseName()).isEqualTo(exercise.getName());
        assertThat(result.totalSets()).isEqualTo(0);
        assertThat(result.maxWeight()).isNull(); // Corrected field access
        assertThat(result.repsIfMaxWeight()).isNull(); // Corrected field access
        assertThat(result.dayOfSetWithMaxWeight()).isNull(); // Corrected field access
        assertThat(result.progression()).isEmpty();

        verify(userRepository).findById(USER_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(workoutSessionSetRepository).findCompletedSetsByUserAndExercise(user, exercise);
    }

    @Test
    @DisplayName("getExerciseStats: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void getExerciseStats_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getExerciseStats(USER_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(exerciseRepository, workoutSessionSetRepository);
    }

    @Test
    @DisplayName("getExerciseStats: Deve lançar ResourceNotFoundException se o exercício não for encontrado")
    void getExerciseStats_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getExerciseStats(USER_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for ExerciseId: " + EXERCISE_ID);

        verify(userRepository).findById(USER_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verifyNoInteractions(workoutSessionSetRepository);
    }

    // -------------------------------------------------------------------------
    // Testes para getWeeklyVolume(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getWeeklyVolume: Deve retornar o volume semanal do usuário")
    void getWeeklyVolume_validUser_shouldReturnWeeklyVolume() {
        // Arrange
        LocalDate fixedToday = LocalDate.of(2024, 7, 17); // A Wednesday
        LocalDateTime startOfWeek = fixedToday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();

        WorkoutSession sessionMonday = TestFactory.makeActiveSession(user, startOfWeek.toLocalDate().atTime(10, 0)); // Using new factory method
        // Mock getTotalVolume() for sessionMonday
        doReturn(100.0).when(sessionMonday).getTotalVolume();

        WorkoutSession sessionWednesday = TestFactory.makeActiveSession(user, startOfWeek.plusDays(2).toLocalDate().atTime(11, 0)); // Using new factory method
        // Mock getTotalVolume() for sessionWednesday
        doReturn(150.0).when(sessionWednesday).getTotalVolume();

        List<WorkoutSession> sessions = List.of(sessionMonday, sessionWednesday);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(sessions);

        // Act
        WeeklyVolumeResponse result = statsService.getWeeklyVolume(USER_ID);

        // Assert
        assertThat(result.volumes()).hasSize(7); // Corrected field access
        assertThat(result.volumes()).contains( // Corrected field access
                new DailyVolumeResponse(DayOfWeek.MONDAY, 100.0),
                new DailyVolumeResponse(DayOfWeek.TUESDAY, 0.0),
                new DailyVolumeResponse(DayOfWeek.WEDNESDAY, 150.0),
                new DailyVolumeResponse(DayOfWeek.THURSDAY, 0.0),
                new DailyVolumeResponse(DayOfWeek.FRIDAY, 0.0),
                new DailyVolumeResponse(DayOfWeek.SATURDAY, 0.0),
                new DailyVolumeResponse(DayOfWeek.SUNDAY, 0.0)
        );

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("getWeeklyVolume: Deve retornar volume zero para todos os dias se não houver sessões na semana")
    void getWeeklyVolume_noSessionsInWeek_shouldReturnZeroVolume() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of());

        // Act
        WeeklyVolumeResponse result = statsService.getWeeklyVolume(USER_ID);

        // Assert
        assertThat(result.volumes()).hasSize(7); // Corrected field access
        assertThat(result.volumes()).allMatch(dailyVolume -> dailyVolume.volume() == 0.0); // Corrected field access

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("getWeeklyVolume: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void getWeeklyVolume_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getWeeklyVolume(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository);
    }

    // -------------------------------------------------------------------------
    // Testes para getMonthlyFrequency(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMonthlyFrequency: Deve retornar a frequência mensal do usuário")
    void getMonthlyFrequency_validUser_shouldReturnMonthlyFrequency() {
        // Arrange
        LocalDate fixedToday = LocalDate.of(2024, 7, 17); // A Wednesday in July
        LocalDateTime startOfMonth = fixedToday.withDayOfMonth(1).atStartOfDay();

        WorkoutSession session1 = TestFactory.makeActiveSession(user, startOfMonth.plusDays(0)); // Using new factory method
        WorkoutSession session2 = TestFactory.makeActiveSession(user, startOfMonth.plusDays(1)); // Using new factory method
        WorkoutSession session3 = TestFactory.makeActiveSession(user, startOfMonth.plusDays(7)); // Using new factory method

        List<WorkoutSession> sessions = List.of(session1, session2, session3);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(sessions);

        // Act
        FrequencyResponse result = statsService.getMonthlyFrequency(USER_ID);

        // Assert
        assertThat(result.totalSessions()).isEqualTo(3);
        assertThat(result.totalDays()).isEqualTo(fixedToday.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()); // Corrected field access
        assertThat(result.avgSessionsPerWeek()).isGreaterThan(0.0);
        assertThat(result.sessionsPerWeek()).hasSize(2); // Two distinct weeks with sessions

        // Week 1: July 1st (Monday) - July 7th (Sunday)
        // Sessions on July 1st and July 2nd fall into this week.
        LocalDate firstWeekMonday = LocalDate.of(2024, 7, 1); // July 1st, 2024 was a Monday

        // Week 2: July 8th (Monday) - July 14th (Sunday)
        // Session on July 8th falls into this week.
        LocalDate secondWeekMonday = LocalDate.of(2024, 7, 8); // July 8th, 2024 was a Monday

        assertThat(result.sessionsPerWeek()).containsExactlyInAnyOrder(
                new ChartPointResponse(firstWeekMonday, 2.0),
                new ChartPointResponse(secondWeekMonday, 1.0)
        );

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("getMonthlyFrequency: Deve retornar frequência zero/vazia se não houver sessões no mês")
    void getMonthlyFrequency_noSessionsInMonth_shouldReturnEmptyFrequency() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of());

        // Act
        FrequencyResponse result = statsService.getMonthlyFrequency(USER_ID);

        // Assert
        assertThat(result.totalSessions()).isEqualTo(0);
        assertThat(result.totalDays()).isEqualTo(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()); // Corrected field access
        assertThat(result.avgSessionsPerWeek()).isEqualTo(0.0);
        assertThat(result.sessionsPerWeek()).isEmpty();

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findSessionWithExercisesAndSetsBetweenTime(
                eq(user), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("getMonthlyFrequency: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void getMonthlyFrequency_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> statsService.getMonthlyFrequency(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository);
    }
}