package com.samson.restbackend.controllers;

import com.samson.restbackend.api.dto.AuthDtos;
import com.samson.restbackend.models.Users;
import com.samson.restbackend.repositories.UserRepository;
import com.samson.restbackend.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthDtos.@NotNull LoginRequest requestBody) {
        Users user = userRepository.findByEmail(requestBody.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(requestBody.password(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");

        String accessToken = jwtService.generateAccessToken(user.getUser_id(), user.getUsername(), user.getUserRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getUser_id());

        return Map.of("access_token", accessToken, "refresh_token", refreshToken, "token_type", "Bearer");
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@Valid @RequestBody @NotNull Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refresh_token");

        if (refreshToken == null || refreshToken.isBlank() || jwtService.isRefreshTokenExpired(refreshToken))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token is invalid or expired");

        Jws<Claims> claimsJws = jwtService.parseAndValidate(refreshToken);
        Claims claims = claimsJws.getPayload();

        if (!"refresh".equals(claims.get("typ", String.class)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token is invalid");

        if (jwtService.isRefreshTokenExpired(claims.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token is expired or revoked");

        UUID userId = UUID.fromString(claims.getSubject());
        Users user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user"));

        // ROTATE: revoke old refresh token and create new one
        jwtService.revokeRefreshToken(claims.getId());
        String newAccessToken = jwtService.generateAccessToken(user.getUser_id(), user.getUsername(), user.getUserRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getUser_id());

        return Map.of("access_token", newAccessToken, "refresh_token", newRefreshToken, "token_type", "Bearer");
    }

}
