package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.TestFactory;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserBodyWeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserHeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserProfileRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.PublicUserResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.UserResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.mappers.UserMapper;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.Role;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID OTHER_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestFactory.makeUser(USER_ID, ExperienceLevel.INTERMEDIATE);
    }

    // -------------------------------------------------------------------------
    // Testes para findOwnProfile(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findOwnProfile: Deve retornar o perfil do usuário quando encontrado")
    void findOwnProfile_userFound_shouldReturnUserProfile() {
        // Arrange
        var expectedResponse = new UserResponse(
                "Test User " + USER_ID, "test" + USER_ID + "@email.com", Role.USER,
                null, ExperienceLevel.INTERMEDIATE, 75.0, 1.80, any(LocalDateTime.class)
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // Act
        var result = userService.findOwnProfile(USER_ID);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("findOwnProfile: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void findOwnProfile_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findOwnProfile(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userMapper, never()).toResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para findById(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById: Deve retornar o perfil público do usuário quando encontrado")
    void findById_userFound_shouldReturnPublicUserProfile() {
        // Arrange
        var expectedResponse = new PublicUserResponse(
                USER_ID, "Test User " + USER_ID, null, ExperienceLevel.INTERMEDIATE
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toPublicResponse(user)).thenReturn(expectedResponse);

        // Act
        var result = userService.findById(USER_ID);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toPublicResponse(user);
    }

    @Test
    @DisplayName("findById: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void findById_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userMapper, never()).toPublicResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para findByNameIgnoringCase(String name)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByNameIgnoringCase: Deve retornar uma lista de perfis públicos quando usuários são encontrados")
    void findByNameIgnoringCase_usersFound_shouldReturnListOfPublicUserProfiles() {
        // Arrange
        var user1 = TestFactory.makeUser(UUID.randomUUID(), ExperienceLevel.BEGINNER);
        var user2 = TestFactory.makeUser(UUID.randomUUID(), ExperienceLevel.ADVANCED);
        var users = List.of(user1, user2);

        var publicUserResponse1 = new PublicUserResponse(user1.getId(), user1.getName(), null, user1.getExperienceLevel());
        var publicUserResponse2 = new PublicUserResponse(user2.getId(), user2.getName(), null, user2.getExperienceLevel());
        var expectedResponses = List.of(publicUserResponse1, publicUserResponse2);

        when(userRepository.findByNameContainingIgnoreCase("Test User")).thenReturn(users);
        when(userMapper.toPublicResponse(user1)).thenReturn(publicUserResponse1);
        when(userMapper.toPublicResponse(user2)).thenReturn(publicUserResponse2);

        // Act
        var result = userService.findByNameIgnoringCase("Test User");

        // Assert
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedResponses);
        verify(userRepository).findByNameContainingIgnoreCase("Test User");
        verify(userMapper, times(2)).toPublicResponse(any(User.class));
    }

    @Test
    @DisplayName("findByNameIgnoringCase: Deve retornar uma lista vazia quando nenhum usuário é encontrado")
    void findByNameIgnoringCase_noUsersFound_shouldReturnEmptyList() {
        // Arrange
        when(userRepository.findByNameContainingIgnoreCase("NonExistentUser")).thenReturn(List.of());

        // Act
        var result = userService.findByNameIgnoringCase("NonExistentUser");

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByNameContainingIgnoreCase("NonExistentUser");
        verify(userMapper, never()).toPublicResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para updateProfile(UUID userId, UpdateUserProfileRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateProfile: Deve atualizar o perfil do usuário com sucesso")
    void updateProfile_validRequest_shouldUpdateUserProfile() {
        // Arrange
        var request = new UpdateUserProfileRequest("New Name", "new_avatar.jpg");
        var updatedUser = new User(
                request.name(), user.getEmail(), user.getPassword(),
                user.getExperienceLevel(), user.getBodyWeight(), user.getHeight()
        );
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(updatedUser, USER_ID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set user ID for testing", e);
        }
        updatedUser.updateProfile(request.name(), request.avatarUrl());

        var expectedResponse = new UserResponse(
                request.name(), user.getEmail(), Role.USER,
                request.avatarUrl(), user.getExperienceLevel(), user.getBodyWeight(), user.getHeight(), any(LocalDateTime.class)
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        var result = userService.updateProfile(USER_ID, request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(user.getName()).isEqualTo(request.name());
        assertThat(user.getAvatarUrl()).isEqualTo(request.avatarUrl());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(updatedUser);
    }

    @Test
    @DisplayName("updateProfile: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void updateProfile_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateUserProfileRequest("New Name", "new_avatar.jpg");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(USER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para updateBodyWeight(UUID userId, UpdateUserBodyWeightRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateBodyWeight: Deve atualizar o peso corporal do usuário com sucesso")
    void updateBodyWeight_validRequest_shouldUpdateUserBodyWeight() {
        // Arrange
        var newBodyWeight = 80.0;
        var request = new UpdateUserBodyWeightRequest(newBodyWeight);
        var updatedUser = new User(
                user.getName(), user.getEmail(), user.getPassword(),
                user.getExperienceLevel(), newBodyWeight, user.getHeight()
        );
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(updatedUser, USER_ID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set user ID for testing", e);
        }
        updatedUser.updateBodyWeight(newBodyWeight);

        var expectedResponse = new UserResponse(
                user.getName(), user.getEmail(), Role.USER,
                null, user.getExperienceLevel(), newBodyWeight, user.getHeight(), any(LocalDateTime.class)
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        var result = userService.updateBodyWeight(USER_ID, request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(user.getBodyWeight()).isEqualTo(newBodyWeight);
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(updatedUser);
    }

    @Test
    @DisplayName("updateBodyWeight: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void updateBodyWeight_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateUserBodyWeightRequest(80.0);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateBodyWeight(USER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para updateHeight(UUID userId, UpdateUserHeightRequest request)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateHeight: Deve atualizar a altura do usuário com sucesso")
    void updateHeight_validRequest_shouldUpdateUserHeight() {
        // Arrange
        var newHeight = 1.85;
        var request = new UpdateUserHeightRequest(newHeight);
        var updatedUser = new User(
                user.getName(), user.getEmail(), user.getPassword(),
                user.getExperienceLevel(), user.getBodyWeight(), newHeight
        );
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(updatedUser, USER_ID);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set user ID for testing", e);
        }
        updatedUser.updateHeight(newHeight);

        var expectedResponse = new UserResponse(
                user.getName(), user.getEmail(), Role.USER,
                null, user.getExperienceLevel(), user.getBodyWeight(), newHeight, any(LocalDateTime.class)
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Act
        var result = userService.updateHeight(USER_ID, request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(user.getHeight()).isEqualTo(newHeight);
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(updatedUser);
    }

    @Test
    @DisplayName("updateHeight: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void updateHeight_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        var request = new UpdateUserHeightRequest(1.85);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateHeight(USER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Testes para deactivate(UUID userId)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deactivate: Deve desativar o usuário com sucesso")
    void deactivate_validUser_shouldDeactivateUser() {
        // Arrange
        user.reactive(); // Ensure user is active
        assertThat(user.isActive()).isTrue();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.deactivate(USER_ID);

        // Assert
        assertThat(user.isActive()).isFalse();
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deactivate: Não deve fazer nada se o usuário já estiver inativo")
    void deactivate_alreadyInactiveUser_shouldKeepInactive() {
        // Arrange
        user.deactivate(); // Ensure user is inactive
        assertThat(user.isActive()).isFalse();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.deactivate(USER_ID);

        // Assert
        assertThat(user.isActive()).isFalse();
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(user); // Still calls save, as the method doesn't check state before calling deactivate() on entity
    }

    @Test
    @DisplayName("deactivate: Deve lançar ResourceNotFoundException se o usuário não for encontrado")
    void deactivate_userNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deactivate(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Resource not found for UserId: " + USER_ID);
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }
}
