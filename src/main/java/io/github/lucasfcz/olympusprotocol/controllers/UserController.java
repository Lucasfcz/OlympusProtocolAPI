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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get own profile", description = "Retrieve profile information of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> findOwnProfile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userService.findOwnProfile(user.getId()));
    }

    @Operation(summary = "Get public user", description = "Retrieve public profile information for the given user ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public user retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<PublicUserResponse> findById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @Operation(summary = "Search users by name", description = "Search users by name (case-insensitive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<List<PublicUserResponse>> findUsersByName(
        @RequestParam @NotBlank String name
    ) {
        return ResponseEntity.ok(userService.findByNameIgnoringCase(name));
    }

    @Operation(summary = "Update profile", description = "Update authenticated user's profile information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserProfileRequest request
            ) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @Operation(summary = "Update body weight", description = "Update authenticated user's body weight")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Body weight updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me/body-weight")
    public ResponseEntity<UserResponse> updateUserBodyWeight(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserBodyWeightRequest request
            ) {
        return ResponseEntity.ok(userService.updateBodyWeight(user.getId(), request));
    }

    @Operation(summary = "Update height", description = "Update authenticated user's height")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Height updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me/height")
    public ResponseEntity<UserResponse> updateUserHeight(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserHeightRequest request
    ) {
        return ResponseEntity.ok(userService.updateHeight(user.getId(), request));
    }

    @Operation(summary = "Deactivate account", description = "Deactivate the authenticated user's account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User user
    ) {
        userService.deactivate(user.getId());

        return ResponseEntity.noContent().build();
    }
}
