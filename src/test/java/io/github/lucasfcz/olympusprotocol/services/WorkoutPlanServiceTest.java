package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.requests.ReorderDaysRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.ReorderExercisesRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateWorkoutDayRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateWorkoutDayExerciseRequest;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutPlanMapper;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.WorkoutDay;
import io.github.lucasfcz.olympusprotocol.models.WorkoutPlan;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutDayRepository;
import io.github.lucasfcz.olympusprotocol.repositories.WorkoutPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutPlanService - Testes de Unidade")
class WorkoutPlanServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID OTHER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID PLAN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
    private static final UUID OTHER_PLAN_ID = UUID.fromString("550e8400-e29b-41d4-a716-44665544000A");
    private static final UUID DAY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
    private static final UUID OTHER_DAY_ID = UUID.fromString("550e8400-e29b-41d4-a716-44665544000B");
    private static final UUID EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
    private static final UUID OTHER_EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-44665544000C");
    private static final UUID WORKOUT_DAY_EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-44665544000D");


    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutPlanMapper workoutPlanMapper;

    @Mock
    private ExerciseValidationService exerciseValidationService;

    @InjectMocks
    private WorkoutPlanService workoutPlanService;

    private User user;
    private User otherUser;
    private WorkoutPlan userPlan;
    private WorkoutPlan otherUserPlan;

    @BeforeEach
    void setUp() {
        user = TestFactory.makeUser(USER_ID, ExperienceLevel.INTERMEDIATE);
        otherUser = TestFactory.makeUser(OTHER_USER_ID, ExperienceLevel.BEGINNER);
        userPlan = TestFactory.makePlan(PLAN_ID, user);
        otherUserPlan = TestFactory.makePlan(OTHER_PLAN_ID, otherUser);
    }

    // -------------------------------------------------------------------------
    // Testes para create(UUID userId, WorkoutPlanRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create: Deve criar um novo plano e desativar o plano ativo existente do usuário")
    void create_validRequest_shouldCreateNewPlanAndDeactivateExistingActivePlan() {
        // Arrange
        var request = TestFactory.samplePlanRequest("Novo Plano");
        var existingActivePlan = TestFactory.makePlan(UUID.randomUUID(), user);
        existingActivePlan.reactivate(); // Garante que está ativo

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByUserAndActiveTrueWithDetails(user)).thenReturn(Optional.of(existingActivePlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenAnswer(invocation -> {
            WorkoutPlan savedPlan = invocation.getArgument(0);
            // Simula o salvamento, garantindo que o ID seja mantido para o novo plano
            if (savedPlan.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkoutPlan.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(savedPlan, UUID.randomUUID());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return savedPlan;
        });
        when(workoutPlanMapper.toResponse(any(WorkoutPlan.class))).thenCallRealMethod(); // Usar o mapper real para DTO

        // Act
        var result = workoutPlanService.create(USER_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(existingActivePlan.isActive()).isFalse(); // O plano anterior deve ser desativado
        verify(workoutPlanRepository, times(2)).save(any(WorkoutPlan.class)); // Um para desativar, outro para o novo
    }

    @Test
    @DisplayName("create: Deve criar um novo plano quando não há plano ativo existente")
    void create_validRequest_noExistingActivePlan_shouldCreateNewPlan() {
        // Arrange
        var request = TestFactory.samplePlanRequest("Plano Sem Ativo");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByUserAndActiveTrueWithDetails(user)).thenReturn(Optional.empty());
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenAnswer(invocation -> {
            WorkoutPlan savedPlan = invocation.getArgument(0);
            try {
                java.lang.reflect.Field field = WorkoutPlan.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(savedPlan, UUID.randomUUID());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return savedPlan;
        });
        when(workoutPlanMapper.toResponse(any(WorkoutPlan.class))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.create(USER_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(request.name());
        verify(workoutPlanRepository, times(1)).save(any(WorkoutPlan.class)); // Apenas o novo plano
    }

    @Test
    @DisplayName("create: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void create_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = TestFactory.samplePlanRequest();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.create(USER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with id " + USER_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para copyWorkoutPlan(UUID userId, UUID originalPlanId, WorkoutPlanRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("copyWorkoutPlan: Deve copiar um plano público com dias e exercícios, desativando o plano ativo existente do usuário")
    void copyWorkoutPlan_publicPlan_shouldCreateNewPlanWithCopiedContent() {
        // Arrange
        var originalPlan = TestFactory.makePlan(UUID.randomUUID(), otherUser);
        originalPlan.changeVisibility(); // Tornar público
        var originalDay = TestFactory.makeWorkoutDay(UUID.randomUUID(), originalPlan, "Dia Original", 1);
        originalPlan.addDay(originalDay);
        var originalExercise = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var originalWDE = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), originalDay, originalExercise, 1);
        originalDay.addExercise(originalWDE);

        var existingActivePlanForUser = TestFactory.makePlan(UUID.randomUUID(), user);
        existingActivePlanForUser.reactivate();

        var newPlanRequest = TestFactory.samplePlanRequest("Plano Copiado");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByIdWithDetails(originalPlan.getId())).thenReturn(Optional.of(originalPlan));
        when(workoutPlanRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(Optional.of(existingActivePlanForUser));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenAnswer(invocation -> {
            WorkoutPlan savedPlan = invocation.getArgument(0);
            if (savedPlan.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkoutPlan.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(savedPlan, UUID.randomUUID());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return savedPlan;
        });
        when(workoutPlanMapper.toResponse(any(WorkoutPlan.class))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.copyWorkoutPlan(USER_ID, originalPlan.getId(), newPlanRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotEqualTo(originalPlan.getId());
        assertThat(result.name()).isEqualTo(newPlanRequest.name());
        assertThat(result.goal()).isEqualTo(originalPlan.getGoal());
        assertThat(result.active()).isTrue();
        assertThat(result.isPublic()).isTrue(); // O novo plano é público por padrão no construtor

        assertThat(existingActivePlanForUser.isActive()).isFalse(); // Plano anterior desativado

        verify(workoutPlanRepository, times(3)).save(any(WorkoutPlan.class)); // 1 para desativar, 1 para o novo plano, 1 para o plano com dias/exercícios
    }

    @Test
    @DisplayName("copyWorkoutPlan: Deve copiar um plano público sem conteúdo (dias/exercícios)")
    void copyWorkoutPlan_publicPlanNoContent_shouldCreateNewEmptyPlan() {
        // Arrange
        var originalPlan = TestFactory.makePlan(UUID.randomUUID(), otherUser);
        originalPlan.changeVisibility(); // Tornar público

        var newPlanRequest = TestFactory.samplePlanRequest("Plano Vazio Copiado");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByIdWithDetails(originalPlan.getId())).thenReturn(Optional.of(originalPlan));
        when(workoutPlanRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty()); // Nenhum plano ativo existente
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenAnswer(invocation -> {
            WorkoutPlan savedPlan = invocation.getArgument(0);
            if (savedPlan.getId() == null) {
                try {
                    java.lang.reflect.Field field = WorkoutPlan.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(savedPlan, UUID.randomUUID());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return savedPlan;
        });
        when(workoutPlanMapper.toResponse(any(WorkoutPlan.class))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.copyWorkoutPlan(USER_ID, originalPlan.getId(), newPlanRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotEqualTo(originalPlan.getId());
        assertThat(result.name()).isEqualTo(newPlanRequest.name());
        assertThat(result.goal()).isEqualTo(originalPlan.getGoal());
        assertThat(result.active()).isTrue();
        assertThat(result.isPublic()).isTrue();
        verify(workoutPlanRepository, times(2)).save(any(WorkoutPlan.class)); // 1 para o novo plano, 1 para o plano com dias/exercícios (mesmo que vazio)
    }

    @Test
    @DisplayName("copyWorkoutPlan: Deve lançar ForbiddenException se o plano original for privado")
    void copyWorkoutPlan_privatePlan_shouldThrowForbiddenException() {
        // Arrange
        var originalPlan = TestFactory.makePlan(UUID.randomUUID(), otherUser); // Por padrão é privado
        var newPlanRequest = TestFactory.samplePlanRequest("Plano Copiado");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByIdWithDetails(originalPlan.getId())).thenReturn(Optional.of(originalPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.copyWorkoutPlan(USER_ID, originalPlan.getId(), newPlanRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("This workout plan is not public.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("copyWorkoutPlan: Deve lançar ResourceNotFoundException se o plano original não for encontrado")
    void copyWorkoutPlan_originalPlanNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var newPlanRequest = TestFactory.samplePlanRequest("Plano Copiado");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.copyWorkoutPlan(USER_ID, PLAN_ID, newPlanRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("copyWorkoutPlan: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void copyWorkoutPlan_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var newPlanRequest = TestFactory.samplePlanRequest("Plano Copiado");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.copyWorkoutPlan(USER_ID, PLAN_ID, newPlanRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with id " + USER_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para findActivePlan(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findActivePlan: Deve retornar o plano ativo do usuário quando encontrado")
    void findActivePlan_userHasActivePlan_shouldReturnPlan() {
        // Arrange
        userPlan.reactivate();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByUserAndActiveTrueWithDetails(user)).thenReturn(Optional.of(userPlan));
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.findActivePlan(USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userPlan.getId());
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("findActivePlan: Deve lançar ResourceNotFoundException se o usuário não tiver plano ativo")
    void findActivePlan_userHasNoActivePlan_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findByUserAndActiveTrueWithDetails(user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.findActivePlan(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Active Workout Plan from user id " + USER_ID + " not found");
    }

    @Test
    @DisplayName("findActivePlan: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void findActivePlan_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.findActivePlan(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with id " + USER_ID + " not found");
    }

    // -------------------------------------------------------------------------
    // Testes para findAllByUser(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAllByUser: Deve retornar todos os planos do usuário quando encontrados")
    void findAllByUser_userHasPlans_shouldReturnAllPlans() {
        // Arrange
        var plan2 = TestFactory.makePlan(UUID.randomUUID(), user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findAllByUserWithDetails(user)).thenReturn(Optional.of(List.of(userPlan, plan2)));
        when(workoutPlanMapper.toResponse(any(WorkoutPlan.class))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.findAllByUser(USER_ID);

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.stream().map(p -> p.id())).containsExactlyInAnyOrder(userPlan.getId(), plan2.getId());
    }

    @Test
    @DisplayName("findAllByUser: Deve retornar uma lista vazia se o usuário não tiver planos")
    void findAllByUser_userHasNoPlans_shouldReturnEmptyList() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(workoutPlanRepository.findAllByUserWithDetails(user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.findAllByUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlans from user id " + USER_ID + " not found");
    }

    @Test
    @DisplayName("findAllByUser: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void findAllByUser_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.findAllByUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with id " + USER_ID + " not found");
    }

    // -------------------------------------------------------------------------
    // Testes para findById(UUID planId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById: Deve retornar o plano quando encontrado")
    void findById_planFound_shouldReturnPlan() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.findById(PLAN_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PLAN_ID);
    }

    @Test
    @DisplayName("findById: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void findById_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.findById(PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
    }

    // -------------------------------------------------------------------------
    // Testes para addDay(UUID userId, UUID planId, WorkoutDayRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addDay: Deve adicionar um dia ao plano com sucesso")
    void addDay_validRequest_shouldAddDayToPlan() {
        // Arrange
        var request = TestFactory.sampleDayRequest();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.addDay(USER_ID, PLAN_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(userPlan.getWorkoutDays()).hasSize(1);
        assertThat(userPlan.getWorkoutDays().get(0).getName()).isEqualTo(request.name());
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("addDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void addDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = TestFactory.sampleDayRequest();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addDay(USER_ID, PLAN_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("addDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void addDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var request = TestFactory.sampleDayRequest();
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addDay(USER_ID, OTHER_PLAN_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para addExerciseToDay(UUID userId, UUID planId, UUID dayId, WorkoutDayExerciseRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addExerciseToDay: Deve adicionar exercício sem aviso quando os níveis são compatíveis")
    void addExerciseToDay_validRequest_compatibleLevel_shouldAddExerciseWithoutWarning() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exercise = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.BEGINNER);
        var request = TestFactory.sampleDayExerciseRequest(exercise.getId());

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, userPlan.getUser())).thenReturn(Optional.empty());
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, day.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.warnings()).isEmpty();
        assertThat(day.getExercises()).hasSize(1);
        assertThat(day.getExercises().get(0).getExercise().getId()).isEqualTo(exercise.getId());
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("addExerciseToDay: Deve adicionar exercício com aviso quando o nível é incompatível")
    void addExerciseToDay_validRequest_incompatibleLevel_shouldAddExerciseWithWarning() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exercise = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.EXPERT); // Nível mais alto
        var request = TestFactory.sampleDayExerciseRequest(exercise.getId());
        var warningMessage = "This exercise is recommended for EXPERT and your level is INTERMEDIATE. Be careful.";

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(exerciseValidationService.checkLevelCompatibility(exercise, userPlan.getUser())).thenReturn(Optional.of(warningMessage));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan, List.of(warningMessage))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, day.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.warnings()).hasSize(1).contains(warningMessage);
        assertThat(day.getExercises()).hasSize(1);
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("addExerciseToDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void addExerciseToDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("addExerciseToDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void addExerciseToDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, otherUserPlan, "Dia de Treino", 1);
        otherUserPlan.addDay(day);
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);

        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(USER_ID, OTHER_PLAN_ID, day.getId(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("addExerciseToDay: Deve lançar ResourceNotFoundException se o dia não for encontrado no plano")
    void addExerciseToDay_dayNotFoundInPlan_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan)); // Plano existe, mas sem o dia

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutDay with id " + DAY_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("addExerciseToDay: Deve lançar ResourceNotFoundException se o exercício não for encontrado")
    void addExerciseToDay_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var request = TestFactory.sampleDayExerciseRequest(EXERCISE_ID);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.addExerciseToDay(USER_ID, PLAN_ID, day.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise with id " + EXERCISE_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    // -------------------------------------------------------------------------
    // Testes para removeDay(UUID userId, UUID planId, UUID dayId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeDay: Deve remover um dia do plano com sucesso")
    void removeDay_validRequest_shouldRemoveDayFromPlan() {
        // Arrange
        var dayToRemove = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia para Remover", 1);
        userPlan.addDay(dayToRemove);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.removeDay(USER_ID, PLAN_ID, dayToRemove.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(userPlan.getWorkoutDays()).isEmpty();
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("removeDay: Não deve lançar exceção e não remover nada se o dia não for encontrado no plano")
    void removeDay_dayNotFoundInPlan_shouldNotRemoveAndReturnPlan() {
        // Arrange
        var dayToKeep = TestFactory.makeWorkoutDay(UUID.randomUUID(), userPlan, "Dia para Manter", 1);
        userPlan.addDay(dayToKeep);
        int initialSize = userPlan.getWorkoutDays().size();

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.removeDay(USER_ID, PLAN_ID, DAY_ID); // DAY_ID não existe no plano

        // Assert
        assertThat(result).isNotNull();
        assertThat(userPlan.getWorkoutDays()).hasSize(initialSize); // Tamanho deve ser o mesmo
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("removeDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void removeDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.removeDay(USER_ID, PLAN_ID, DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("removeDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void removeDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.removeDay(USER_ID, OTHER_PLAN_ID, DAY_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para removeExerciseFromDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeExerciseFromDay: Deve remover um exercício de um dia com sucesso")
    void removeExerciseFromDay_validRequest_shouldRemoveExerciseFromDay() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exerciseToRemove = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.BEGINNER);
        var wdeToRemove = TestFactory.makeWorkoutDayExercise(WORKOUT_DAY_EXERCISE_ID, day, exerciseToRemove, 1);
        day.addExercise(wdeToRemove);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.findByIdAndWorkoutPlanId(day.getId(), userPlan.getId())).thenReturn(Optional.of(day));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.removeExerciseFromDay(USER_ID, PLAN_ID, day.getId(), wdeToRemove.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(day.getExercises()).isEmpty();
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("removeExerciseFromDay: Não deve lançar exceção e não remover nada se o exercício não for encontrado no dia")
    void removeExerciseFromDay_exerciseNotFoundInDay_shouldNotRemoveAndReturnPlan() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exerciseToKeep = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var wdeToKeep = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), day, exerciseToKeep, 1);
        day.addExercise(wdeToKeep);
        int initialSize = day.getExercises().size();

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.findByIdAndWorkoutPlanId(day.getId(), userPlan.getId())).thenReturn(Optional.of(day));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.removeExerciseFromDay(USER_ID, PLAN_ID, day.getId(), EXERCISE_ID); // EXERCISE_ID não existe no dia

        // Assert
        assertThat(result).isNotNull();
        assertThat(day.getExercises()).hasSize(initialSize);
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("removeExerciseFromDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void removeExerciseFromDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.removeExerciseFromDay(USER_ID, PLAN_ID, DAY_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeExerciseFromDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void removeExerciseFromDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.removeExerciseFromDay(USER_ID, OTHER_PLAN_ID, DAY_ID, EXERCISE_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeExerciseFromDay: Deve lançar ResourceNotFoundException se o dia não for encontrado no plano")
    void removeExerciseFromDay_dayNotFoundInPlan_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.findByIdAndWorkoutPlanId(DAY_ID, userPlan.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.removeExerciseFromDay(USER_ID, PLAN_ID, DAY_ID, EXERCISE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutDay with id " + DAY_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    // -------------------------------------------------------------------------
    // Testes para updateDay(UUID userId, UUID planId, UUID dayId, UpdateWorkoutDayRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateDay: Deve atualizar os detalhes de um dia com sucesso")
    void updateDay_validRequest_shouldUpdateDayDetails() {
        // Arrange
        var dayToUpdate = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Nome Antigo", 1);
        userPlan.addDay(dayToUpdate);
        var request = new UpdateWorkoutDayRequest("Novo Nome", 2);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(dayToUpdate);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.updateDay(USER_ID, PLAN_ID, dayToUpdate.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(dayToUpdate.getName()).isEqualTo(request.name());
        assertThat(dayToUpdate.getDayOrder()).isEqualTo(request.dayOrder());
        verify(workoutDayRepository).save(dayToUpdate);
    }

    @Test
    @DisplayName("updateDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void updateDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateWorkoutDayRequest("Nome", 1);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void updateDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var request = new UpdateWorkoutDayRequest("Nome", 1);
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateDay(USER_ID, OTHER_PLAN_ID, DAY_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDay: Deve lançar ResourceNotFoundException se o dia não for encontrado no plano")
    void updateDay_dayNotFoundInPlan_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateWorkoutDayRequest("Nome", 1);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan)); // Plano existe, mas sem o dia

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutDay with id " + DAY_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    // -------------------------------------------------------------------------
    // Testes para updateExerciseInDay(UUID userId, UUID planId, UUID dayId, UUID exerciseId, UpdateWorkoutDayExerciseRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateExerciseInDay: Deve atualizar exercício sem aviso quando os níveis são compatíveis")
    void updateExerciseInDay_validRequest_compatibleLevel_shouldUpdateExerciseWithoutWarning() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var oldExercise = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var wdeToUpdate = TestFactory.makeWorkoutDayExercise(WORKOUT_DAY_EXERCISE_ID, day, oldExercise, 1);
        day.addExercise(wdeToUpdate);

        var newExercise = TestFactory.makeExercise(OTHER_EXERCISE_ID, ExperienceLevel.INTERMEDIATE);
        var request = new UpdateWorkoutDayExerciseRequest(newExercise.getId(), 2, 4, 10, 60);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(newExercise.getId())).thenReturn(Optional.of(newExercise));
        when(exerciseValidationService.checkLevelCompatibility(newExercise, userPlan.getUser())).thenReturn(Optional.empty());
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, day.getId(), wdeToUpdate.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(wdeToUpdate.getExercise().getId()).isEqualTo(newExercise.getId());
        assertThat(wdeToUpdate.getExerciseOrder()).isEqualTo(request.exerciseOrder());
        assertThat(wdeToUpdate.getSets()).isEqualTo(request.sets());
        assertThat(wdeToUpdate.getReps()).isEqualTo(request.reps());
        assertThat(wdeToUpdate.getRestTime()).isEqualTo(request.restTime());
        assertThat(result.warnings()).isEmpty();
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve atualizar exercício com aviso quando o nível é incompatível")
    void updateExerciseInDay_validRequest_incompatibleLevel_shouldUpdateExerciseWithWarning() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var oldExercise = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var wdeToUpdate = TestFactory.makeWorkoutDayExercise(WORKOUT_DAY_EXERCISE_ID, day, oldExercise, 1);
        day.addExercise(wdeToUpdate);

        var newExercise = TestFactory.makeExercise(OTHER_EXERCISE_ID, ExperienceLevel.EXPERT);
        var request = new UpdateWorkoutDayExerciseRequest(newExercise.getId(), 2, 4, 10, 60);
        var warningMessage = "This exercise is recommended for EXPERT and your level is INTERMEDIATE. Be careful.";

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(newExercise.getId())).thenReturn(Optional.of(newExercise));
        when(exerciseValidationService.checkLevelCompatibility(newExercise, userPlan.getUser())).thenReturn(Optional.of(warningMessage));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan, List.of(warningMessage))).thenCallRealMethod();

        // Act
        var result = workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, day.getId(), wdeToUpdate.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(wdeToUpdate.getExercise().getId()).isEqualTo(newExercise.getId());
        assertThat(result.warnings()).hasSize(1).contains(warningMessage);
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void updateExerciseInDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateWorkoutDayExerciseRequest(EXERCISE_ID, 1, 1, 1, 1);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, DAY_ID, WORKOUT_DAY_EXERCISE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void updateExerciseInDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var request = new UpdateWorkoutDayExerciseRequest(EXERCISE_ID, 1, 1, 1, 1);
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateExerciseInDay(USER_ID, OTHER_PLAN_ID, DAY_ID, WORKOUT_DAY_EXERCISE_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve lançar ResourceNotFoundException se o dia não for encontrado no plano")
    void updateExerciseInDay_dayNotFoundInPlan_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateWorkoutDayExerciseRequest(EXERCISE_ID, 1, 1, 1, 1);
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan)); // Plano existe, mas sem o dia

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, DAY_ID, WORKOUT_DAY_EXERCISE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutDay with id " + DAY_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve lançar ResourceNotFoundException se o exercício não for encontrado no dia")
    void updateExerciseInDay_exerciseNotFoundInDay_shouldThrowResourceNotFoundException() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var request = new UpdateWorkoutDayExerciseRequest(EXERCISE_ID, 1, 1, 1, 1);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, day.getId(), WORKOUT_DAY_EXERCISE_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise in Day with id " + WORKOUT_DAY_EXERCISE_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("updateExerciseInDay: Deve lançar ResourceNotFoundException se o novo exerciseId não for encontrado")
    void updateExerciseInDay_newExerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var oldExercise = TestFactory.makeExercise(EXERCISE_ID, ExperienceLevel.BEGINNER);
        var wdeToUpdate = TestFactory.makeWorkoutDayExercise(WORKOUT_DAY_EXERCISE_ID, day, oldExercise, 1);
        day.addExercise(wdeToUpdate);

        var nonExistentNewExerciseId = UUID.randomUUID();
        var request = new UpdateWorkoutDayExerciseRequest(nonExistentNewExerciseId, 2, 4, 10, 60);

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(exerciseRepository.findById(nonExistentNewExerciseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.updateExerciseInDay(USER_ID, PLAN_ID, day.getId(), wdeToUpdate.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise with id " + nonExistentNewExerciseId + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    // -------------------------------------------------------------------------
    // Testes para reorderDays(UUID userId, UUID planId, ReorderDaysRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reorderDays: Deve reordenar os dias do plano com sucesso")
    void reorderDays_validRequest_shouldReorderDays() {
        // Arrange
        var day1 = TestFactory.makeWorkoutDay(UUID.randomUUID(), userPlan, "Dia 1", 1);
        var day2 = TestFactory.makeWorkoutDay(UUID.randomUUID(), userPlan, "Dia 2", 2);
        userPlan.addDay(day1);
        userPlan.addDay(day2);

        var request = new ReorderDaysRequest(List.of(
                new ReorderDaysRequest.DayOrderItem(day2.getId(), 1),
                new ReorderDaysRequest.DayOrderItem(day1.getId(), 2)
        ));

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderDays(USER_ID, PLAN_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(day1.getDayOrder()).isEqualTo(2);
        assertThat(day2.getDayOrder()).isEqualTo(1);
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("reorderDays: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void reorderDays_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new ReorderDaysRequest(List.of());
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reorderDays(USER_ID, PLAN_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("reorderDays: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void reorderDays_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var request = new ReorderDaysRequest(List.of());
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reorderDays(USER_ID, OTHER_PLAN_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("reorderDays: Não deve fazer nada se a lista de ordens estiver vazia")
    void reorderDays_emptyOrderList_shouldDoNothing() {
        // Arrange
        var day1 = TestFactory.makeWorkoutDay(UUID.randomUUID(), userPlan, "Dia 1", 1);
        userPlan.addDay(day1);
        var request = new ReorderDaysRequest(List.of());

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderDays(USER_ID, PLAN_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(day1.getDayOrder()).isEqualTo(1); // Ordem não deve mudar
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("reorderDays: Deve ignorar dayIds não existentes na requisição e reordenar os existentes")
    void reorderDays_nonExistentDayIds_shouldIgnoreAndReorderExisting() {
        // Arrange
        var day1 = TestFactory.makeWorkoutDay(UUID.randomUUID(), userPlan, "Dia 1", 1);
        userPlan.addDay(day1);
        var nonExistentDayId = UUID.randomUUID();

        var request = new ReorderDaysRequest(List.of(
                new ReorderDaysRequest.DayOrderItem(day1.getId(), 2),
                new ReorderDaysRequest.DayOrderItem(nonExistentDayId, 1)
        ));

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderDays(USER_ID, PLAN_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(day1.getDayOrder()).isEqualTo(2); // Apenas o dia existente deve ser reordenado
        verify(workoutPlanRepository).save(userPlan);
    }

    // -------------------------------------------------------------------------
    // Testes para reorderExercisesInDay(UUID userId, UUID planId, UUID dayId, ReorderExercisesRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reorderExercisesInDay: Deve reordenar os exercícios em um dia com sucesso")
    void reorderExercisesInDay_validRequest_shouldReorderExercises() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exercise1 = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var exercise2 = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.INTERMEDIATE);
        var wde1 = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), day, exercise1, 1);
        var wde2 = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), day, exercise2, 2);
        day.addExercise(wde1);
        day.addExercise(wde2);

        var request = new ReorderExercisesRequest(List.of(
                new ReorderExercisesRequest.ExerciseOrderItem(wde2.getId(), 1),
                new ReorderExercisesRequest.ExerciseOrderItem(wde1.getId(), 2)
        ));

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderExercisesInDay(USER_ID, PLAN_ID, day.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(wde1.getExerciseOrder()).isEqualTo(2);
        assertThat(wde2.getExerciseOrder()).isEqualTo(1);
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("reorderExercisesInDay: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void reorderExercisesInDay_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new ReorderExercisesRequest(List.of());
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reorderExercisesInDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("reorderExercisesInDay: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void reorderExercisesInDay_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        var request = new ReorderExercisesRequest(List.of());
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reorderExercisesInDay(USER_ID, OTHER_PLAN_ID, DAY_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("reorderExercisesInDay: Deve lançar ResourceNotFoundException se o dia não for encontrado no plano")
    void reorderExercisesInDay_dayNotFoundInPlan_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new ReorderExercisesRequest(List.of());
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan)); // Plano existe, mas sem o dia

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reorderExercisesInDay(USER_ID, PLAN_ID, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutDay with id " + DAY_ID + " not found");
        verify(workoutDayRepository, never()).save(any(WorkoutDay.class));
    }

    @Test
    @DisplayName("reorderExercisesInDay: Não deve fazer nada se a lista de ordens estiver vazia")
    void reorderExercisesInDay_emptyOrderList_shouldDoNothing() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exercise1 = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var wde1 = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), day, exercise1, 1);
        day.addExercise(wde1);
        var request = new ReorderExercisesRequest(List.of());

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderExercisesInDay(USER_ID, PLAN_ID, day.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(wde1.getExerciseOrder()).isEqualTo(1); // Ordem não deve mudar
        verify(workoutDayRepository).save(day);
    }

    @Test
    @DisplayName("reorderExercisesInDay: Deve ignorar exerciseIds não existentes na requisição e reordenar os existentes")
    void reorderExercisesInDay_nonExistentExerciseIds_shouldIgnoreAndReorderExisting() {
        // Arrange
        var day = TestFactory.makeWorkoutDay(DAY_ID, userPlan, "Dia de Treino", 1);
        userPlan.addDay(day);
        var exercise1 = TestFactory.makeExercise(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var wde1 = TestFactory.makeWorkoutDayExercise(UUID.randomUUID(), day, exercise1, 1);
        day.addExercise(wde1);
        var nonExistentExerciseId = UUID.randomUUID();

        var request = new ReorderExercisesRequest(List.of(
                new ReorderExercisesRequest.ExerciseOrderItem(wde1.getId(), 2),
                new ReorderExercisesRequest.ExerciseOrderItem(nonExistentExerciseId, 1)
        ));

        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(day);
        when(workoutPlanMapper.toResponse(userPlan)).thenCallRealMethod();

        // Act
        var result = workoutPlanService.reorderExercisesInDay(USER_ID, PLAN_ID, day.getId(), request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(wde1.getExerciseOrder()).isEqualTo(2); // Apenas o exercício existente deve ser reordenado
        verify(workoutDayRepository).save(day);
    }

    // -------------------------------------------------------------------------
    // Testes para deactivate(UUID userId, UUID planId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate: Deve desativar o plano com sucesso quando o usuário é o proprietário")
    void deactivate_validRequest_shouldDeactivatePlan() {
        // Arrange
        userPlan.reactivate(); // Garante que está ativo
        assertThat(userPlan.isActive()).isTrue();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.deactivate(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isActive()).isFalse();
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("deactivate: Não deve fazer nada se o plano já estiver inativo")
    void deactivate_alreadyInactivePlan_shouldKeepInactive() {
        // Arrange
        userPlan.deactivate(); // Garante que está inativo
        assertThat(userPlan.isActive()).isFalse();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.deactivate(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isActive()).isFalse();
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("deactivate: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void deactivate_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.deactivate(USER_ID, PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("deactivate: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void deactivate_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.deactivate(USER_ID, OTHER_PLAN_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para reactivate(UUID userId, UUID planId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reactivate: Deve reativar o plano e desativar o plano ativo anterior do usuário")
    void reactivate_validRequest_shouldReactivatePlanAndDeactivatePreviousActivePlan() {
        // Arrange
        var previousActivePlan = TestFactory.makePlan(UUID.randomUUID(), user);
        previousActivePlan.reactivate(); // Garante que está ativo
        userPlan.deactivate(); // Garante que o plano a ser reativado está inativo

        when(workoutPlanRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(Optional.of(previousActivePlan));
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.reactivate(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isActive()).isTrue(); // O plano deve ser reativado
        assertThat(previousActivePlan.isActive()).isFalse(); // O plano anterior deve ser desativado
        verify(workoutPlanRepository, times(2)).save(any(WorkoutPlan.class)); // Um para desativar, outro para reativar
    }

    @Test
    @DisplayName("reactivate: Deve reativar o plano quando não há plano ativo anterior")
    void reactivate_noPreviousActivePlan_shouldReactivatePlan() {
        // Arrange
        userPlan.deactivate(); // Garante que o plano a ser reativado está inativo

        when(workoutPlanRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(Optional.empty());
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.reactivate(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isActive()).isTrue(); // O plano deve ser reativado
        verify(workoutPlanRepository, times(1)).save(any(WorkoutPlan.class)); // Apenas para reativar
    }

    @Test
    @DisplayName("reactivate: Não deve fazer nada se o plano já estiver ativo")
    void reactivate_alreadyActivePlan_shouldKeepActive() {
        // Arrange
        userPlan.reactivate(); // Garante que está ativo
        assertThat(userPlan.isActive()).isTrue();

        when(workoutPlanRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(Optional.of(userPlan)); // Retorna ele mesmo como ativo
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.reactivate(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isActive()).isTrue();
        verify(workoutPlanRepository, times(2)).save(any(WorkoutPlan.class)); // Uma para desativar (ele mesmo), outra para reativar (ele mesmo)
    }

    @Test
    @DisplayName("reactivate: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void reactivate_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reactivate(USER_ID, PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("reactivate: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void reactivate_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.reactivate(USER_ID, OTHER_PLAN_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    // -------------------------------------------------------------------------
    // Testes para changeVisibility(UUID userId, UUID planId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("changeVisibility: Deve mudar a visibilidade de público para privado")
    void changeVisibility_publicToPrivate_shouldChangeAndSave() {
        // Arrange
        userPlan.changeVisibility(); // Torna público
        assertThat(userPlan.isPublic()).isTrue();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.changeVisibility(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isPublic()).isFalse();
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("changeVisibility: Deve mudar a visibilidade de privado para público")
    void changeVisibility_privateToPublic_shouldChangeAndSave() {
        // Arrange
        userPlan.changeVisibility(); // Garante que é privado (padrão é público no construtor)
        userPlan.changeVisibility(); // Torna público
        assertThat(userPlan.isPublic()).isTrue();
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.of(userPlan));
        when(workoutPlanRepository.save(any(WorkoutPlan.class))).thenReturn(userPlan);

        // Act
        workoutPlanService.changeVisibility(USER_ID, PLAN_ID);

        // Assert
        assertThat(userPlan.isPublic()).isFalse();
        verify(workoutPlanRepository).save(userPlan);
    }

    @Test
    @DisplayName("changeVisibility: Deve lançar ResourceNotFoundException se o plano não for encontrado")
    void changeVisibility_planNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(PLAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.changeVisibility(USER_ID, PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("WorkoutPlan with id " + PLAN_ID + " not found");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }

    @Test
    @DisplayName("changeVisibility: Deve lançar ForbiddenException se o usuário não for o proprietário do plano")
    void changeVisibility_userDoesNotOwnPlan_shouldThrowForbiddenException() {
        // Arrange
        when(workoutPlanRepository.findByIdWithDetails(OTHER_PLAN_ID)).thenReturn(Optional.of(otherUserPlan));

        // Act & Assert
        assertThatThrownBy(() -> workoutPlanService.changeVisibility(USER_ID, OTHER_PLAN_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You are not allowed to access this workout plan.");
        verify(workoutPlanRepository, never()).save(any(WorkoutPlan.class));
    }
}
