package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.*;
import io.github.lucasfcz.olympusprotocol.dto.responses.WorkoutSessionResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.BusinessException;
import io.github.lucasfcz.olympusprotocol.exceptions.ForbiddenException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.WorkoutSessionMapper;
import io.github.lucasfcz.olympusprotocol.models.*;
import io.github.lucasfcz.olympusprotocol.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final UserRepository userRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseValidationService exerciseValidationService;
    private final WorkoutSessionMapper workoutSessionMapper;

    public WorkoutSessionResponse startFromPlan(UUID userId, UUID workoutDayId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        validateNoActiveSession(user);

        var workoutDay = workoutDayRepository.findById(workoutDayId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", workoutDayId));
        checkDayOwnership(workoutDay, userId);

        var session = new WorkoutSession(user, workoutDay);

        workoutDay.getExercises().forEach(de -> {
            var sessionExercise = new WorkoutSessionExercise(session, de.getExercise(), de.getExerciseOrder());
            for (int i = 1; i <= de.getSets(); i++) {
                sessionExercise.addSet(new WorkoutSessionSet(sessionExercise, i, de.getReps(), null, de.getRestTime()));
            }
            session.addExercise(sessionExercise);
        });

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse startFreeSession(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        validateNoActiveSession(user);

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(new WorkoutSession(user, null)));
    }

    public WorkoutSessionResponse findById(UUID userId, UUID sessionId) {
        return workoutSessionMapper.toResponse(getOwnedSession(sessionId, userId));
    }

    public List<WorkoutSessionResponse> findAllByUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return workoutSessionRepository.findByUser(user)
                .stream()
                .map(workoutSessionMapper::toResponse)
                .toList();
    }

    public WorkoutSessionResponse addExercise(UUID userId, UUID sessionId, AddExerciseToSessionRequest request) {
        var session = getValidSession(sessionId, userId);

        var exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.exerciseId()));

        session.addExercise(new WorkoutSessionExercise(session, exercise, request.exerciseOrder()));

        var warning = exerciseValidationService.checkLevelCompatibility(exercise, session.getUser());
        var saved = workoutSessionRepository.save(session);

        return warning.isPresent()
                ? workoutSessionMapper.toResponse(saved, List.of(warning.get()))
                : workoutSessionMapper.toResponse(saved);
    }

    public WorkoutSessionResponse removeExercise(UUID userId, UUID sessionId, UUID exerciseId) {
        var session = getValidSession(sessionId, userId);
        session.removeExercise(getExerciseInSession(session, exerciseId));

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse addSet(UUID userId, UUID sessionId, UUID exerciseId, SetRequest request) {
        var session = getValidSession(sessionId, userId);
        var sessionExercise = getExerciseInSession(session, exerciseId);

        sessionExercise.addSet(new WorkoutSessionSet(sessionExercise, request.setOrder(), request.reps(), request.weight(), request.restTime()));

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse removeSet(UUID userId, UUID sessionId, UUID exerciseId, UUID setId) {
        var session = getValidSession(sessionId, userId);
        getExerciseInSession(session, exerciseId).removeSet(getSetOrThrow(session, setId));
        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse updateSet(UUID userId, UUID sessionId, UUID setId, SetRequest request) {
        var session = getValidSession(sessionId, userId);
        getSetOrThrow(session, setId).updateSet(request.setOrder(), request.reps(), request.weight(), request.restTime());
        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse updateSessionExercise(UUID userId, UUID sessionId, UUID sessionExerciseId, UpdateSessionExerciseRequest request) {
        var session = getValidSession(sessionId, userId);

        var exercise = exerciseRepository.findById(request.newExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.newExerciseId()));

        session.getExercises().stream()
                .filter(se -> se.getId().equals(sessionExerciseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("SessionExercise", sessionExerciseId))
                .updateSessionExercise(exercise, request.exerciseOrder());

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse reorderExercises(UUID userId, UUID sessionId, ReorderExercisesRequest request) {
        var session = getValidSession(sessionId, userId);

        request.orders().forEach(item ->
                session.getExercises().stream()
                        .filter(e -> e.getId().equals(item.exerciseId()))
                        .findFirst()
                        .ifPresent(e -> e.updateOrder(item.order()))
        );

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse reorderSets(UUID userId, UUID sessionId, UUID exerciseId, ReorderSetsRequest request) {
        var session = getValidSession(sessionId, userId);

        request.orders().forEach(item ->
                getExerciseInSession(session, exerciseId).getSets().stream()
                        .filter(s -> s.getId().equals(item.setId()))
                        .findFirst()
                        .ifPresent(s -> s.updateOrder(item.order()))
        );

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    public WorkoutSessionResponse finish(UUID userId, UUID sessionId, FinishSessionRequest request) {
        var session = getOwnedSession(sessionId, userId);
        validateSessionNotFinished(session);
        session.finish(request.notes());
        return workoutSessionMapper.toResponse(workoutSessionRepository.save(session));
    }

    // Private methods for clean code and reutilization code

    private WorkoutSession getValidSession(UUID sessionId, UUID userId) {
        var session = getSessionOrThrow(sessionId);
        checkSessionOwnership(session, userId);
        validateSessionNotFinished(session);
        return session;
    }

    private WorkoutSession getOwnedSession(UUID sessionId, UUID userId) {
        var session = getSessionOrThrow(sessionId);
        checkSessionOwnership(session, userId);
        return session;
    }

    private WorkoutSession getSessionOrThrow(UUID sessionId) {
        return workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", sessionId));
    }

    private WorkoutSessionExercise getExerciseInSession(WorkoutSession session, UUID exerciseId) {
        return session.getExercises().stream()
                .filter(se -> se.getExercise().getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", exerciseId));
    }

    private WorkoutSessionSet getSetOrThrow(WorkoutSession session, UUID setId) {
        return session.getExercises().stream()
                .flatMap(ex -> ex.getSets().stream())
                .filter(s -> s.getId().equals(setId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Set", setId));
    }

    private void checkSessionOwnership(WorkoutSession session, UUID userId) {
        if (!session.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this session.");
        }
    }

    private void checkDayOwnership(WorkoutDay day, UUID userId) {
        if (!day.getWorkoutPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to access this workout day.");
        }
    }

    private void validateSessionNotFinished(WorkoutSession session) {
        if (session.isFinished()) {
            throw new BusinessException("This session is already finished.");
        }
    }

    private void validateNoActiveSession(User user) {
        if (workoutSessionRepository.findByUserAndFinishedAtIsNull(user).isPresent()) {
            throw new BusinessException("You already have an active session. Finish it before starting a new one.");
        }
    }
}