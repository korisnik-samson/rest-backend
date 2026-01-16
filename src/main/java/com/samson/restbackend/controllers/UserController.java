package com.samson.restbackend.controllers;

import com.samson.restbackend.api.dto.CreateUserRequest;
import com.samson.restbackend.api.dto.UserResponse;
import com.samson.restbackend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users/{userId}")
    public UserResponse get(@PathVariable UUID userId) {
        return service.get(userId);
    }

    @GetMapping("/users")
    public Iterable<UserResponse> getAll() {
        return service.getAllUsers();
    }

    @PostMapping(path = "/users/create")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest requestBody) {
        return service.create(requestBody);
    }
}