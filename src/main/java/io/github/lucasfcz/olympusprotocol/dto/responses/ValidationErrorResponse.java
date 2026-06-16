package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.List;

public record ValidationErrorResponse(
        String message,
        List<FieldErrorDTO> errors
) {
}

