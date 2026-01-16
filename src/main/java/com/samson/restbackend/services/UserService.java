package com.samson.restbackend.services;

import com.samson.restbackend.api.dto.CreateUserRequest;
import com.samson.restbackend.api.dto.UserResponse;
import com.samson.restbackend.models.Users;
import com.samson.restbackend.repositories.UserRepository;
import com.samson.restbackend.util.UserRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        Users u = repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        return new UserResponse(u.getUser_id().toString(), u.getUsername(), u.getEmail(), u.getUserRole().name());
    }

    @Transactional(readOnly = true)
    public Iterable<UserResponse> getAllUsers() {
        return repository.findAll().stream().map(
                u -> new UserResponse(
                        u.getUser_id().toString(), u.getUsername(), u.getEmail(), u.getUserRole().name()
                )
        ).toList();
    }

    @Transactional
    public UserResponse create(@NotNull CreateUserRequest request) {
        if (repository.existsByEmail(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");

        if (repository.existsByUsername(request.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");

        if (request.password().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password cannot be blank");

        if (request.userRole() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user role cannot be null");

        Users user = Users.builder()
                .username(request.username())
                .email(request.email()).userRole(request.userRole())
                .password(encoder.encode(request.password()))
                .build();

        user = repository.save(user);

        return new UserResponse(user.getUser_id().toString(), user.getUsername(), user.getEmail(), user.getUserRole().name());
    }
}
