package io.github.lucasfcz.olympusprotocol.controllers;

import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserBodyWeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserHeightRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.UpdateUserProfileRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.PublicUserResponse;
import io.github.lucasfcz.olympusprotocol.dto.responses.UserResponse;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> findOwnProfile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userService.findOwnProfile(user.getId()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PublicUserResponse> findById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PublicUserResponse>> findUsersByName(
        @RequestParam @NotBlank String name
    ) {
        return ResponseEntity.ok(userService.findByNameIgnoringCase(name));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserProfileRequest request
            ) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PutMapping("/me/body-weight")
    public ResponseEntity<UserResponse> updateUserBodyWeight(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserBodyWeightRequest request
            ) {
        return ResponseEntity.ok(userService.updateBodyWeight(user.getId(), request));
    }

    @PutMapping("/me/height")
    public ResponseEntity<UserResponse> updateUserHeight(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserHeightRequest request
    ) {
        return ResponseEntity.ok(userService.updateHeight(user.getId(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User user
    ) {
        userService.deactivate(user.getId());

        return ResponseEntity.noContent().build();
    }
}
