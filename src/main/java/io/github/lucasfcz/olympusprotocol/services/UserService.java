package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserBodyWeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserHeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserProfileRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.PublicUserResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.UserResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.UserMapper;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse findOwnProfile(UUID userId) {
        var user = getUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public PublicUserResponse findById(UUID userId) {
        var user = getUserOrThrow(userId);

        return userMapper.toPublicResponse(user);
    }

    @Transactional
    public List<PublicUserResponse> findByNameIgnoringCase(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(userMapper::toPublicResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        var user = getUserOrThrow(userId);

        user.updateProfile(request.name(), request.avatarUrl());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateBodyWeight(UUID userId, UpdateUserBodyWeightRequest request) {
        var user = getUserOrThrow(userId);
        user.updateBodyWeight(request.bodyWeight());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateHeight(UUID userId, UpdateUserHeightRequest request) {
        var user = getUserOrThrow(userId);
        user.updateHeight(request.height());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivate(UUID userId) {
        var user = getUserOrThrow(userId);
        user.deactivate();
        userRepository.save(user);
    }

    // Helper Methods

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
               .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
    }
}
