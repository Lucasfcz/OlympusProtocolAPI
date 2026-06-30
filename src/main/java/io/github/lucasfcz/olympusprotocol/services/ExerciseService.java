package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.ExerciseRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.ExerciseResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.DuplicateResourceException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.ExerciseMapper;
import io.github.lucasfcz.olympusprotocol.models.Exercise;
import io.github.lucasfcz.olympusprotocol.models.enums.*;
import io.github.lucasfcz.olympusprotocol.repositories.ExerciseRepository;
import io.github.lucasfcz.olympusprotocol.specifications.ExerciseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.github.lucasfcz.olympusprotocol.cache.CachesNames.*;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional
    @CacheEvict(value = EXERCISES, allEntries = true)
    public ExerciseResponse create(ExerciseRequest request) {
        if (exerciseRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("This exercise is already registered: " + request.name());
        }

        var exercise = new Exercise(
                request.name(),
                request.description(),
                request.minExperienceLevel(),
                request.safetyRating(),
                request.efficiencyRating(),
                request.adminNotes(),
                request.gifUrl(),
                request.usesBodyWeight()
        );

        exerciseRepository.save(exercise);

        return saveChildrenAndReturn(request, exercise);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = EXERCISE, key = "#id")
    public ExerciseResponse findById(UUID id) {
        return exerciseRepository.findById(id)
                .map(exerciseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = EXERCISES, key = "{#name, #muscleGroups, #safetyRatings, #efficiencyRatings, #levels, #muscleHeads}")
    public List<ExerciseResponse> findAll(String name,
                                          List<MuscleGroup> muscleGroups,
                                          List<SafetyRating> safetyRatings,
                                          List<EfficiencyRating> efficiencyRatings,
                                          List<ExperienceLevel> levels,
                                          List<MuscleHead> muscleHeads) {
        var spec = ExerciseSpecification.filters(name, muscleGroups, safetyRatings,
                efficiencyRatings, levels, muscleHeads);

        return exerciseRepository.findAll(spec)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = EXERCISE, key = "#id"),
            @CacheEvict(value = EXERCISES, allEntries = true)
    })
    public ExerciseResponse update(UUID id, ExerciseRequest request) {
        var exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));

        if (exerciseRepository.existsByNameIgnoreCase(request.name())
                && !exercise.getName().equalsIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Some exercise already exists with name: " + request.name());
        }

        exercise.updateInfo(
                request.name(), request.description(),
                request.minExperienceLevel(), request.safetyRating(),
                request.efficiencyRating(), request.adminNotes(), request.gifUrl()
        );

        return saveChildrenAndReturn(request, exercise);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = EXERCISE, key = "#id"),
            @CacheEvict(value = EXERCISES, allEntries = true)
    })
    public void deactivate(UUID id) {
        var exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));
        exercise.deactivate();
        exerciseRepository.save(exercise);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = EXERCISE, key = "#id"),
            @CacheEvict(value = EXERCISES, allEntries = true)
    })
    public void reactivate(UUID id) {
        var exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));
        exercise.reactivate();
        exerciseRepository.save(exercise);
    }

    // Helpers Methods
    private ExerciseResponse saveChildrenAndReturn(ExerciseRequest request, Exercise exercise) {
        if (request.muscles() != null) {
            exercise.replaceMuscles(
                    request.muscles().stream()
                            .map(m -> exerciseMapper.toMuscleEntity(exercise, m))
                            .toList()
            );
        }

        if (request.tips() != null) {
            exercise.replaceTips(
                    request.tips().stream()
                            .map(t -> exerciseMapper.toTipEntity(exercise, t))
                            .toList()
            );
        }

        if (request.contraindications() != null) {
            exercise.replaceContraindications(
                    request.contraindications().stream()
                            .map(c -> exerciseMapper.toContraindicationEntity(exercise, c))
                            .toList()
            );
        }

        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }
}