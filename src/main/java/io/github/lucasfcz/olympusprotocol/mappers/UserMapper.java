package io.github.lucasfcz.olympusprotocol.mappers;

import io.github.lucasfcz.olympusprotocol.dto.responses.PublicUserResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.UserResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse (User user) {
        return new UserResponse(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                user.getExperienceLevel(),
                user.getBodyWeight(),
                user.getHeight(),
                user.getCreatedAt()
        );
    }

    public PublicUserResponse toPublicResponse(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getName(),
                user.getAvatarUrl(),
                user.getExperienceLevel()
        );
    }
}
