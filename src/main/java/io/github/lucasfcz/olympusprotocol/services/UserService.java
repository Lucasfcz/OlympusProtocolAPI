package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserBodyWeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserHeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserProfileRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.PublicUserResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.UserResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.BusinessException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.UserMapper;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;

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
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = USER_PROFILE, key = "#userId")
    public UserResponse findOwnProfile(UUID userId) {
        var user = getUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = PUBLIC_USER_PROFILE, key = "#userId")
    public PublicUserResponse findById(UUID userId) {
        var user = getUserOrThrow(userId);

        return userMapper.toPublicResponse(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = USERS_BY_NAME, key = "#name")
    public List<PublicUserResponse> findByNameIgnoringCase(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(userMapper::toPublicResponse)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_PROFILE, key = "#userId"),
            @CacheEvict(value = PUBLIC_USER_PROFILE, key = "#userId"),
            @CacheEvict(value = USERS_BY_NAME, allEntries = true)
    })
    public UserResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        var user = getUserOrThrow(userId);

        user.updateProfile(request.name(), request.avatarUrl());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_PROFILE, key = "#userId"),
            @CacheEvict(value = PUBLIC_USER_PROFILE, key = "#userId"),
            @CacheEvict(value = USERS_BY_NAME, allEntries = true),
            @CacheEvict(value = USER_STATS, key = "#userId")
    })
    public UserResponse updateBodyWeight(UUID userId, UpdateUserBodyWeightRequest request) {
        var user = getUserOrThrow(userId);
        user.updateBodyWeight(request.bodyWeight());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_PROFILE, key = "#userId"),
            @CacheEvict(value = PUBLIC_USER_PROFILE, key = "#userId"),
            @CacheEvict(value = USERS_BY_NAME, allEntries = true)
    })
    public UserResponse updateHeight(UUID userId, UpdateUserHeightRequest request) {
        var user = getUserOrThrow(userId);
        user.updateHeight(request.height());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_PROFILE, key = "#userId"),
            @CacheEvict(value = PUBLIC_USER_PROFILE, key = "#userId"),
            @CacheEvict(value = USERS_BY_NAME, allEntries = true),
            @CacheEvict(value = USER_WORKOUT_PLANS, key = "#userId"),
            @CacheEvict(value = ACTIVE_WORKOUT_PLAN, key = "#userId"),
            @CacheEvict(value = USER_STATS, key = "#userId"),
            @CacheEvict(value = WEEKLY_VOLUME, key = "#userId"),
            @CacheEvict(value = MONTHLY_FREQUENCY, key = "#userId")
    })
    public void deactivate(UUID userId) {
        var user = getUserOrThrow(userId);
        if(user.isActive()) {
            user.deactivate();
            userRepository.save(user);
        }
    }

    // Helper Methods

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
               .orElseThrow(() -> new ResourceNotFoundException("UserId", userId));
    }
}
