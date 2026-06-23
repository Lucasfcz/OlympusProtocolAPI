package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.LoginRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.RegisterRequest;
import io.github.lucasfcz.olympusprotocol.dto.responses.AuthResponse;
import io.github.lucasfcz.olympusprotocol.exceptions.DuplicateResourceException;
import io.github.lucasfcz.olympusprotocol.exceptions.ResourceNotFoundException;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "This email is already registered: " + request.email()
            );
        }

        var user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.experienceLevel(),
                request.bodyWeight(),
                request.height()
        );

        userRepository.save(user);

        return new AuthResponse(
                jwtService.generateToken(user),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Throws an exception with the credentials are invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        return new AuthResponse(jwtService.generateToken(user), user.getName(), user.getEmail(), user.getRole());
    }
}