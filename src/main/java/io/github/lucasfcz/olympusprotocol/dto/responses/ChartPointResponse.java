package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.time.LocalDate;

public record ChartPointResponse(
        LocalDate date,
        Double value
) {}