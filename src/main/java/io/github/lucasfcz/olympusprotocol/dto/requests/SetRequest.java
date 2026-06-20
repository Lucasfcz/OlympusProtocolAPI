package io.github.lucasfcz.olympusprotocol.dto.requests;

public record SetRequest(
        Integer reps,
        Double weight,
        Integer restTime,
        Integer setOrder
) {
}
