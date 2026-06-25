package io.github.lucasfcz.olympusprotocol.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.github.lucasfcz.olympusprotocol.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class JwtService {

    @Value("${api.security.jwt.secret}")
    private String secret;

    @Value("${api.security.jwt.expiration}")
    private long expiration;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    @Transactional
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(getAlgorithm());
    }

    @Transactional
    public String extractEmail(String token) {
        return JWT.require(getAlgorithm())
                .build()
                .verify(token)
                .getSubject();
    }

    @Transactional
    public boolean isTokenValid(String token) {
        try {
            JWT.require(getAlgorithm())
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}