package com.samson.restbackend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min=8, max=128) String password
    ) {}

    public record LoginResponse(
            String id,
            String username,
            String email,
            String role
    ) {}

}
