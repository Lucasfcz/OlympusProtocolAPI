package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.DuplicateResourceException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.ExerciseMapper;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseService Tests")
class ExerciseServiceTest {

    private static final UUID EXERCISE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseService exerciseService;

    private ExerciseRequest exerciseRequest;

    @BeforeEach
    void setUp() {
        exerciseRequest = TestFactory.sampleExerciseRequest();
    }

    @Test
    @DisplayName("create should save exercise successfully and return response")
    void create_validRequest_shouldSaveAndReturnResponse() {
        // Arrange
        when(exerciseRepository.existsByNameIgnoreCase(exerciseRequest.name())).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(i -> i.getArgument(0));
        var mockResponse = new ExerciseResponse(
            EXERCISE_ID, exerciseRequest.name(), exerciseRequest.description(),
            exerciseRequest.minExperienceLevel(), exerciseRequest.safetyRating(),
            exerciseRequest.efficiencyRating(), exerciseRequest.adminNotes(),
            exerciseRequest.gifUrl(), false, true, List.of(), List.of(), List.of()
        );
        when(exerciseMapper.toResponse(any(Exercise.class))).thenReturn(mockResponse);

        // Act
        var result = exerciseService.create(exerciseRequest);

        // Assert
        assertNotNull(result);
        assertEquals(exerciseRequest.name(), result.name());
        verify(exerciseRepository, times(2)).save(any(Exercise.class));
        verify(exerciseMapper).toResponse(any());
    }

    @Test
    @DisplayName("create should throw DuplicateResourceException when exercise name already exists")
    void create_duplicateName_shouldThrowDuplicateResourceException() {
        // Arrange
        when(exerciseRepository.existsByNameIgnoreCase(exerciseRequest.name())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> exerciseService.create(exerciseRequest));
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById should return exercise when found")
    void findById_exerciseExists_shouldReturnResponse() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var mockResponse = new ExerciseResponse(
            EXERCISE_ID, exercise.getName(), exercise.getDescription(),
            exercise.getRecommendedExperienceLevel(), exercise.getSafetyRating(),
            exercise.getEfficiencyRating(), exercise.getAdminNotes(), exercise.getGifUrl(), false
            true, List.of(), List.of(), List.of()
        );
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(mockResponse);

        // Act
        var result = exerciseService.findById(EXERCISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(exercise.getName(), result.name());
        verify(exerciseRepository).findById(EXERCISE_ID);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when exercise not found")
    void findById_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> exerciseService.findById(EXERCISE_ID));
        verify(exerciseRepository).findById(EXERCISE_ID);
    }

    @Test
    @DisplayName("findAll should return mapped list of exercises")
    void findAll_withFilters_shouldReturnMappedList() {
        // Arrange
        var exercise = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var mockResponse = new ExerciseResponse(
            EXERCISE_ID, exercise.getName(), exercise.getDescription(),
            exercise.getRecommendedExperienceLevel(), exercise.getSafetyRating(),
            exercise.getEfficiencyRating(), exercise.getAdminNotes(), exercise.getGifUrl(),
            true, List.of(), List.of(), List.of()
        );
        when(exerciseRepository.findAll(any(Specification.class))).thenReturn(List.of(exercise));
        when(exerciseMapper.toResponse(any())).thenReturn(mockResponse);

        // Act
        var result = exerciseService.findAll("test", List.of(), List.of(), List.of(), List.of(), List.of());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exerciseRepository).findAll(any(Specification.class));
        verify(exerciseMapper).toResponse(any());
    }

    @Test
    @DisplayName("update should successfully update exercise when found and name is unique")
    void update_validRequest_shouldUpdateAndReturnResponse() {
        // Arrange
        var existing = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        var mockResponse = new ExerciseResponse(
            EXERCISE_ID, exerciseRequest.name(), exerciseRequest.description(),
            exerciseRequest.minExperienceLevel(), exerciseRequest.safetyRating(),
            exerciseRequest.efficiencyRating(), exerciseRequest.adminNotes(),
            exerciseRequest.gifUrl(), true, List.of(), List.of(), List.of()
        );
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(existing));
        when(exerciseRepository.existsByNameIgnoreCase(exerciseRequest.name())).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(i -> i.getArgument(0));
        when(exerciseMapper.toResponse(any())).thenReturn(mockResponse);

        // Act
        var result = exerciseService.update(EXERCISE_ID, exerciseRequest);

        // Assert
        assertNotNull(result);
        assertEquals(exerciseRequest.name(), result.name());
        verify(exerciseRepository).findById(EXERCISE_ID);
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    @DisplayName("update should throw DuplicateResourceException when name exists for different exercise")
    void update_duplicateNameDifferentExercise_shouldThrowDuplicateResourceException() {
        // Arrange
        var existing = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        existing.updateInfo("Different Name", "desc", ExperienceLevel.BEGINNER, existing.getSafetyRating(), existing.getEfficiencyRating(), "", "");
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(existing));
        when(exerciseRepository.existsByNameIgnoreCase(exerciseRequest.name())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> exerciseService.update(EXERCISE_ID, exerciseRequest));
    }

    @Test
    @DisplayName("update should throw ResourceNotFoundException when exercise not found")
    void update_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> exerciseService.update(EXERCISE_ID, exerciseRequest));
    }

    @Test
    @DisplayName("deactivate should set exercise to inactive")
    void deactivate_exerciseExists_shouldDeactivateAndSave() {
        // Arrange
        var existing = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        assertTrue(existing.isActive());
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(existing));

        // Act
        exerciseService.deactivate(EXERCISE_ID);

        // Assert
        assertFalse(existing.isActive());
        verify(exerciseRepository).save(existing);
    }

    @Test
    @DisplayName("deactivate should throw ResourceNotFoundException when exercise not found")
    void deactivate_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> exerciseService.deactivate(EXERCISE_ID));
    }

    @Test
    @DisplayName("reactivate should set exercise to active")
    void reactivate_inactiveExercise_shouldReactivateAndSave() {
        // Arrange
        var existing = TestFactory.makeExercise(ExperienceLevel.BEGINNER);
        existing.deactivate();
        assertFalse(existing.isActive());
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(existing));

        // Act
        exerciseService.reactivate(EXERCISE_ID);

        // Assert
        assertTrue(existing.isActive());
        verify(exerciseRepository).save(existing);
    }

    @Test
    @DisplayName("reactivate should throw ResourceNotFoundException when exercise not found")
    void reactivate_exerciseNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> exerciseService.reactivate(EXERCISE_ID));
    }
}

