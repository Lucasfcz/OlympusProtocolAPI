package io.github.lucasfcz.olympusprotocol.dto.requests;

import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotNull ExperienceLevel experienceLevel
) {}
