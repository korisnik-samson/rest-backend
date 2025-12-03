package com.samson.restbackend.api.dto;

public record UserResponse (
        String id,
        String username,
        String email,
        String userRole
) {}
