package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.requests.AddExerciseToSessionRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.FinishSessionRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.ReorderExercisesRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.ReorderSetsRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.SetRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateSessionExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.SessionSummaryResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.BusinessException;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutSessionMapper;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDayExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSession;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionExercise;
import io.github.lucasfcz.olympusprotocol.models.WorkoutSessionSet;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutDayRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutSessionService Tests")
class WorkoutSessionServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID OTHER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID WORKOUT_DAY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    private static final UUID EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
    private static final UUID SESSION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
    private static final UUID SESSION_EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
    private static final UUID SET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");


    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseValidationService exerciseValidationService;

    @Mock
    private WorkoutSessionMapper workoutSessionMapper;

    @InjectMocks
    private WorkoutSessionService workoutSessionService;

    private User user;
    private User otherUser;
    private WorkoutPlan userPlan;
    private WorkoutDay workoutDay;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        user = TestFactory.makeUser(USER_ID, ExperienceLevel.INTERMEDIATE);
        otherUser = TestFactory.makeUser(OTHER_USER_ID, ExperienceLevel.BEGINNER);
        userPlan = TestFactory.makePlan(user);
        workoutDay = TestFactory.makeWorkoutDay(WORKOUT_DAY_ID, userPlan, "Day A", 1);
        exercise = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.BEGINNER);
    }

    // -------------------------------------------------------------------------
    // Testes para startFromWorkoutDay(UUID userId, UUID workoutDayId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("startFromWorkoutDay: Deve iniciar uma sessão a partir de um dia de treino com sucesso")
    void startFromWorkoutDay_validRequest_shouldStartSession() {
        // Arrange
        WorkoutDayExercise wde = TestFactory.makeWorkoutDayExercise(workoutDay, exercise);
        workoutDay.addExercise(wde);

        WorkoutSession session = new WorkoutSession(user, workoutDay);
        WorkoutSessionExercise sessionExercise = new WorkoutSessionExercise(session, exercise, wde.getExerciseOrder());
        sessionExercise.addSet(new WorkoutSessionSet(sessionExercise, 1, wde.getReps(), null, wde.getRestTime(), null));
        session.addExercise(sessionExercise);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.empty());
        when(workoutDayRepository.findByIdWithExercises(WORKOUT_DAY_ID)).thenReturn(Optional.of(workoutDay));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.startFromWorkoutDay(USER_ID, WORKOUT_DAY_ID);

        // Assert
        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verify(workoutDayRepository).findByIdWithExercises(WORKOUT_DAY_ID);
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutSessionMapper).toResponse(any(WorkoutSession.class));
    }

    @Test
    @DisplayName("startFromWorkoutDay: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void startFromWorkoutDay_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFromWorkoutDay(USER_ID, WORKOUT_DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for User: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository, workoutDayRepository);
    }

    @Test
    @DisplayName("startFromWorkoutDay: Deve lançar BusinessException se já houver uma sessão ativa")
    void startFromWorkoutDay_activeSessionExists_shouldThrowBusinessException() {
        // Arrange
        WorkoutSession activeSession = TestFactory.makeActiveSession(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.of(activeSession));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFromWorkoutDay(USER_ID, WORKOUT_DAY_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You already have an active session. Finish it before starting a new one.");

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verifyNoInteractions(workoutDayRepository);
    }

    @Test
    @DisplayName("startFromWorkoutDay: Deve lançar ResourceNotFoundException se o dia de treino não for encontrado")
    void startFromWorkoutDay_workoutDayNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.empty());
        when(workoutDayRepository.findByIdWithExercises(WORKOUT_DAY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFromWorkoutDay(USER_ID, WORKOUT_DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for WorkoutDay: " + WORKOUT_DAY_ID);

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verify(workoutDayRepository).findByIdWithExercises(WORKOUT_DAY_ID);
        verifyNoInteractions(workoutSessionRepository); // No save should be called
    }

    @Test
    @DisplayName("startFromWorkoutDay: Deve lançar ForbiddenException se o usuário não for o proprietário do dia de treino")
    void startFromWorkoutDay_userNotOwnerOfWorkoutDay_shouldThrowForbiddenException() {
        // Arrange
        WorkoutPlan otherUserPlan = TestFactory.makePlan(otherUser);
        WorkoutDay otherUserWorkoutDay = TestFactory.makeWorkoutDay(WORKOUT_DAY_ID, otherUserPlan, "Other User Day", 1);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.empty());
        when(workoutDayRepository.findByIdWithExercises(WORKOUT_DAY_ID)).thenReturn(Optional.of(otherUserWorkoutDay));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFromWorkoutDay(USER_ID, WORKOUT_DAY_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout day.");

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verify(workoutDayRepository).findByIdWithExercises(WORKOUT_DAY_ID);
        verifyNoInteractions(workoutSessionRepository); // No save should be called
    }

    // -------------------------------------------------------------------------
    // Testes para startFreeSession(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("startFreeSession: Deve iniciar uma sessão livre com sucesso")
    void startFreeSession_validRequest_shouldStartFreeSession() {
        // Arrange
        WorkoutSession session = new WorkoutSession(user, null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.empty());
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.startFreeSession(USER_ID);

        // Assert
        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutSessionMapper).toResponse(any(WorkoutSession.class));
    }

    @Test
    @DisplayName("startFreeSession: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void startFreeSession_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFreeSession(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for User: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository);
    }

    @Test
    @DisplayName("startFreeSession: Deve lançar BusinessException se já houver uma sessão ativa")
    void startFreeSession_activeSessionExists_shouldThrowBusinessException() {
        // Arrange
        WorkoutSession activeSession = TestFactory.makeActiveSession(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserAndFinishedAtIsNull(user)).thenReturn(Optional.of(activeSession));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.startFreeSession(USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You already have an active session. Finish it before starting a new one.");

        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserAndFinishedAtIsNull(user);
        verifyNoInteractions(workoutSessionRepository); // No save should be called
    }

    // -------------------------------------------------------------------------
    // Testes para findById(UUID userId, UUID sessionId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById: Deve retornar a sessão de treino quando encontrada e pertencente ao usuário")
    void findById_sessionFoundAndOwned_shouldReturnSession() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionMapper.toResponse(session)).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.findById(USER_ID, SESSION_ID);

        // Assert
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("findById: Deve lançar ResourceNotFoundException se a sessão não for encontrada")
    void findById_sessionNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.findById(USER_ID, SESSION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for WorkoutSession: " + SESSION_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionMapper);
    }

    @Test
    @DisplayName("findById: Deve lançar ForbiddenException se a sessão não pertencer ao usuário")
    void findById_sessionNotOwned_shouldThrowForbiddenException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(otherUser);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.findById(USER_ID, SESSION_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this session.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para findAllByUser(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAllByUser: Deve retornar todas as sessões de treino do usuário")
    void findAllByUser_userHasSessions_shouldReturnAllSessions() {
        // Arrange
        WorkoutSession session1 = TestFactory.makeActiveSession(user);
        WorkoutSession session2 = TestFactory.makeActiveSession(user);
        List<WorkoutSession> sessions = List.of(session1, session2);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserWithExercisesAndSets(user)).thenReturn(sessions);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        List<WorkoutSessionResponse> result = workoutSessionService.findAllByUser(USER_ID);

        // Assert
        assertThat(result).hasSize(2);
        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserWithExercisesAndSets(user);
        verify(workoutSessionMapper, times(2)).toResponse(any(WorkoutSession.class));
    }

    @Test
    @DisplayName("findAllByUser: Deve retornar uma lista vazia se o usuário não tiver sessões")
    void findAllByUser_userHasNoSessions_shouldReturnEmptyList() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutSessionRepository.findByUserWithExercisesAndSets(user)).thenReturn(List.of());

        // Act
        List<WorkoutSessionResponse> result = workoutSessionService.findAllByUser(USER_ID);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findById(USER_ID);
        verify(workoutSessionRepository).findByUserWithExercisesAndSets(user);
        verifyNoInteractions(workoutSessionMapper);
    }

    @Test
    @DisplayName("findAllByUser: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void findAllByUser_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.findAllByUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for User: " + USER_ID);

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para addExercise(UUID userId, UUID sessionId, AddExerciseToSessionRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addExercise: Deve adicionar um exercício à sessão com sucesso (sem aviso)")
    void addExercise_validRequest_shouldAddExerciseWithoutWarning() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, user)).thenReturn(Optional.empty());
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.addExercise(USER_ID, SESSION_ID, request);

        // Assert
        assertThat(session.getExercises()).hasSize(1);
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(exerciseValidationService).checkLevelCompatibility(exercise, user);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("addExercise: Deve adicionar um exercício à sessão com sucesso (com aviso)")
    void addExercise_validRequest_shouldAddExerciseWithWarning() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);
        String warningMessage = "Warning: Incompatible level";

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, user)).thenReturn(Optional.of(warningMessage));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class), anyList())).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.addExercise(USER_ID, SESSION_ID, request);

        // Assert
        assertThat(session.getExercises()).hasSize(1);
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(exerciseValidationService).checkLevelCompatibility(exercise, user);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session, List.of(warningMessage));
    }

    @Test
    @DisplayName("addExercise: Deve lançar ResourceNotFoundException se a sessão não for encontrada")
    void addExercise_sessionNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addExercise(USER_ID, SESSION_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for WorkoutSession: " + SESSION_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(exerciseRepository, exerciseValidationService, workoutSessionMapper);
    }

    @Test
    @DisplayName("addExercise: Deve lançar ForbiddenException se a sessão não pertencer ao usuário")
    void addExercise_sessionNotOwned_shouldThrowForbiddenException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(otherUser);
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addExercise(USER_ID, SESSION_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this session.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(exerciseRepository, exerciseValidationService, workoutSessionMapper);
    }

    @Test
    @DisplayName("addExercise: Deve lançar BusinessException se a sessão já estiver finalizada")
    void addExercise_sessionFinished_shouldThrowBusinessException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        session.finish("Notes");
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addExercise(USER_ID, SESSION_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("This session is already finished.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(exerciseRepository, exerciseValidationService, workoutSessionMapper);
    }

    @Test
    @DisplayName("addExercise: Deve lançar ResourceNotFoundException se o exercício não for encontrado")
    void addExercise_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        AddExerciseToSessionRequest request = new AddExerciseToSessionRequest(EXERCISE_ID, 1);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addExercise(USER_ID, SESSION_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for Exercise: " + EXERCISE_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(exerciseRepository).findById(EXERCISE_ID);
        verifyNoInteractions(exerciseValidationService, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para removeExercise(UUID userId, UUID sessionId, UUID exerciseId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeExercise: Deve remover um exercício da sessão com sucesso")
    void removeExercise_validRequest_shouldRemoveExercise() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        session.addExercise(sessionExercise);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.removeExercise(USER_ID, SESSION_ID, exercise.getId());

        // Assert
        assertThat(session.getExercises()).isEmpty();
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("removeExercise: Deve lançar ResourceNotFoundException se a sessão não for encontrada")
    void removeExercise_sessionNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.removeExercise(USER_ID, SESSION_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for WorkoutSession: " + SESSION_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    @Test
    @DisplayName("removeExercise: Deve lançar ForbiddenException se a sessão não pertencer ao usuário")
    void removeExercise_sessionNotOwned_shouldThrowForbiddenException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(otherUser);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.removeExercise(USER_ID, SESSION_ID, EXERCISE_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this session.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    @Test
    @DisplayName("removeExercise: Deve lançar BusinessException se a sessão já estiver finalizada")
    void removeExercise_sessionFinished_shouldThrowBusinessException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        session.finish("Notes");
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.removeExercise(USER_ID, SESSION_ID, EXERCISE_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("This session is already finished.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    @Test
    @DisplayName("removeExercise: Deve lançar ResourceNotFoundException se o exercício não for encontrado na sessão")
    void removeExercise_exerciseNotFoundInSession_shouldThrowResourceNotFoundException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.removeExercise(USER_ID, SESSION_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for Exercise: " + EXERCISE_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para addSet(UUID userId, UUID sessionId, UUID exerciseId, SetRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addSet: Deve adicionar um set a um exercício da sessão com sucesso")
    void addSet_validRequest_shouldAddSetToExercise() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        session.addExercise(sessionExercise);

        SetRequest request = new SetRequest(1, 10, 100.0, 60, 8.0);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.addSet(USER_ID, SESSION_ID, exercise.getId(), request);

        // Assert
        assertThat(sessionExercise.getSets()).hasSize(1);
        assertThat(sessionExercise.getSets().get(0).getWeight()).isEqualTo(100.0);
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("addSet: Deve calcular o peso corporal para exercícios bodyweight")
    void addSet_bodyweightExercise_shouldCalculateBodyWeight() {
        // Arrange
        Exercise bodyweightExercise = TestFactory.makeBodyweightExercise();
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, bodyweightExercise);
        session.addExercise(sessionExercise);

        SetRequest request = new SetRequest(1, 10, 10.0, 60, 8.0); // 10kg adicional

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.addSet(USER_ID, SESSION_ID, bodyweightExercise.getId(), request);

        // Assert
        assertThat(sessionExercise.getSets()).hasSize(1);
        assertThat(sessionExercise.getSets().get(0).getWeight()).isEqualTo(user.getBodyWeight() + request.weight()); // 75.0 + 10.0 = 85.0
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("addSet: Deve lançar BusinessException se o usuário não tiver peso corporal para exercício bodyweight")
    void addSet_bodyweightExerciseNoBodyWeight_shouldThrowBusinessException() {
        // Arrange
        user = TestFactory.makeUserWithoutBodyWeight(); // User without body weight
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, TestFactory.makeBodyweightExercise());
        session.addExercise(sessionExercise);

        SetRequest request = new SetRequest(1, 10, 10.0, 60, 8.0);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addSet(USER_ID, SESSION_ID, sessionExercise.getExercise().getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Please set your body weight in your profile before logging bodyweight exercises.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para removeSet(UUID userId, UUID sessionId, UUID exerciseId, UUID setId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeSet: Deve remover um set de um exercício da sessão com sucesso")
    void removeSet_validRequest_shouldRemoveSet() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        WorkoutSessionSet set = TestFactory.makeSet(sessionExercise);
        sessionExercise.addSet(set);
        session.addExercise(sessionExercise);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.removeSet(USER_ID, SESSION_ID, exercise.getId(), SET_ID);

        // Assert
        assertThat(sessionExercise.getSets()).isEmpty();
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("removeSet: Deve lançar ResourceNotFoundException se o set não for encontrado")
    void removeSet_setNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        session.addExercise(sessionExercise);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.removeSet(USER_ID, SESSION_ID, exercise.getId(), SET_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for Set: " + SET_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para updateSet(UUID userId, UUID sessionId, UUID setId, SetRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateSet: Deve atualizar um set de um exercício da sessão com sucesso")
    void updateSet_validRequest_shouldUpdateSet() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        WorkoutSessionSet set = TestFactory.makeSet(sessionExercise);
        sessionExercise.addSet(set);
        session.addExercise(sessionExercise);

        SetRequest request = new SetRequest(2, 12, 110.0, 90, 9.0);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.updateSet(USER_ID, SESSION_ID, SET_ID, request);

        // Assert
        assertThat(set.getSetOrder()).isEqualTo(request.setOrder());
        assertThat(set.getReps()).isEqualTo(request.reps());
        assertThat(set.getWeight()).isEqualTo(request.weight());
        assertThat(set.getRestTime()).isEqualTo(request.restTime());
        assertThat(set.getRpe()).isEqualTo(request.rpe());
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    // -------------------------------------------------------------------------
    // Testes para updateSessionExercise(UUID userId, UUID sessionId, UUID sessionExerciseId, UpdateSessionExerciseRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateSessionExercise: Deve atualizar um exercício da sessão com sucesso")
    void updateSessionExercise_validRequest_shouldUpdateSessionExercise() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);session.addExercise(sessionExercise);

        Exercise newExercise = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.ADVANCED);
        UpdateSessionExerciseRequest request = new UpdateSessionExerciseRequest(newExercise.getId(), 2);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(newExercise.getId())).thenReturn(Optional.of(newExercise));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.updateSessionExercise(USER_ID, SESSION_ID, SESSION_EXERCISE_ID, request);

        // Assert
        assertThat(sessionExercise.getExercise().getId()).isEqualTo(newExercise.getId());
        assertThat(sessionExercise.getExerciseOrder()).isEqualTo(request.exerciseOrder());
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(exerciseRepository).findById(newExercise.getId());
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    @Test
    @DisplayName("updateSessionExercise: Deve lançar ResourceNotFoundException se o SessionExercise não for encontrado")
    void updateSessionExercise_sessionExerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        session.addExercise(TestFactory.makeSessionExercise(session, exercise)); // Add a different one
        Exercise newExercise = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.ADVANCED);
        UpdateSessionExerciseRequest request = new UpdateSessionExerciseRequest(newExercise.getId(), 2);

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(newExercise.getId())).thenReturn(Optional.of(newExercise));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.updateSessionExercise(USER_ID, SESSION_ID, UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for SessionExercise: ");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(exerciseRepository).findById(newExercise.getId());
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para reorderExercises(UUID userId, UUID sessionId, ReorderExercisesRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reorderExercises: Deve reordenar os exercícios da sessão com sucesso")
    void reorderExercises_validRequest_shouldReorderExercises() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise exercise1 = TestFactory.makeSessionExercise(session, exercise);
        exercise1.updateOrder(1);
        WorkoutSessionExercise exercise2 = TestFactory.makeSessionExercise(session, TestFactory.makeExercise());
        exercise2.updateOrder(2);
        session.addExercise(exercise1);
        session.addExercise(exercise2);

        ReorderExercisesRequest request = new ReorderExercisesRequest(List.of(
                new ReorderExercisesRequest.ExerciseOrderItem(exercise2.getId(), 1),
                new ReorderExercisesRequest.ExerciseOrderItem(exercise1.getId(), 2)
        ));

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.reorderExercises(USER_ID, SESSION_ID, request);

        // Assert
        assertThat(exercise1.getExerciseOrder()).isEqualTo(2);
        assertThat(exercise2.getExerciseOrder()).isEqualTo(1);
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    // -------------------------------------------------------------------------
    // Testes para reorderSets(UUID userId, UUID sessionId, UUID exerciseId, ReorderSetsRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reorderSets: Deve reordenar os sets de um exercício da sessão com sucesso")
    void reorderSets_validRequest_shouldReorderSets() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        WorkoutSessionExercise sessionExercise = TestFactory.makeSessionExercise(session, exercise);
        WorkoutSessionSet set1 = TestFactory.makeSetWithWeight(sessionExercise, 1, 10, 100.0);
        WorkoutSessionSet set2 = TestFactory.makeSetWithWeight(sessionExercise, 2, 10, 100.0);
        sessionExercise.addSet(set1);
        sessionExercise.addSet(set2);
        session.addExercise(sessionExercise);

        ReorderSetsRequest request = new ReorderSetsRequest(List.of(
                new ReorderSetsRequest.SetOrderItem(set2.getId(), 1),
                new ReorderSetsRequest.SetOrderItem(set1.getId(), 2)
        ));

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toResponse(any(WorkoutSession.class))).thenReturn(mock(WorkoutSessionResponse.class));

        // Act
        workoutSessionService.reorderSets(USER_ID, SESSION_ID, exercise.getId(), request);

        // Assert
        assertThat(set1.getSetOrder()).isEqualTo(2);
        assertThat(set2.getSetOrder()).isEqualTo(1);
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toResponse(session);
    }

    // -------------------------------------------------------------------------
    // Testes para finishSession(UUID userId, UUID sessionId, FinishSessionRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("finishSession: Deve finalizar a sessão com sucesso e retornar o resumo")
    void finishSession_validRequest_shouldFinishSessionAndReturnSummary() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        FinishSessionRequest request = new FinishSessionRequest("Good workout!");

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toSummary(any(WorkoutSession.class))).thenReturn(mock(SessionSummaryResponse.class));

        // Act
        workoutSessionService.finishSession(USER_ID, SESSION_ID, request);

        // Assert
        assertThat(session.isFinished()).isTrue();
        assertThat(session.getNotes()).isEqualTo(request.notes());
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionRepository).save(session);
        verify(workoutSessionMapper).toSummary(session);
    }

    @Test
    @DisplayName("finishSession: Deve lançar BusinessException se a sessão já estiver finalizada")
    void finishSession_sessionAlreadyFinished_shouldThrowBusinessException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        session.finish("Notes");
        FinishSessionRequest request = new FinishSessionRequest("Good workout!");

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.finishSession(USER_ID, SESSION_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("This session is already finished.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionRepository, workoutSessionMapper);
    }

    // -------------------------------------------------------------------------
    // Testes para getSessionSummary(UUID userId, UUID sessionId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getSessionSummary: Deve retornar o resumo da sessão quando encontrada e pertencente ao usuário")
    void getSessionSummary_sessionFoundAndOwned_shouldReturnSummary() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(user);
        session.finish("Notes"); // Must be finished to have a summary

        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));
        when(workoutSessionMapper.toSummary(session)).thenReturn(mock(SessionSummaryResponse.class));

        // Act
        workoutSessionService.getSessionSummary(USER_ID, SESSION_ID);

        // Assert
        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verify(workoutSessionMapper).toSummary(session);
    }

    @Test
    @DisplayName("getSessionSummary: Deve lançar ResourceNotFoundException se a sessão não for encontrada")
    void getSessionSummary_sessionNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.getSessionSummary(USER_ID, SESSION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for WorkoutSession: " + SESSION_ID);

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionMapper);
    }

    @Test
    @DisplayName("getSessionSummary: Deve lançar ForbiddenException se a sessão não pertencer ao usuário")
    void getSessionSummary_sessionNotOwned_shouldThrowForbiddenException() {
        // Arrange
        WorkoutSession session = TestFactory.makeActiveSession(otherUser);
        session.finish("Notes");
        when(workoutSessionRepository.findByIdWithExercisesAndSets(SESSION_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.getSessionSummary(USER_ID, SESSION_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this session.");

        verify(workoutSessionRepository).findByIdWithExercisesAndSets(SESSION_ID);
        verifyNoInteractions(workoutSessionMapper);
    }
}
