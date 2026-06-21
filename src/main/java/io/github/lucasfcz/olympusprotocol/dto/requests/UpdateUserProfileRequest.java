package io.github.lucasfcz.olympusprotocol.dto.requests;

public record UpdateUserProfileRequest(
        String name,
        String avatarUrl
) {
}
