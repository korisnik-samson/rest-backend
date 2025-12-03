package com.samson.restbackend.controllers;

import com.samson.restbackend.services.AuthourizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthourizationController {
    private final AuthourizationService authourizationService;

    @Autowired
    public AdminAuthourizationController(AuthourizationService authourizationService) {
        this.authourizationService = authourizationService;
    }

    @PostMapping("/logout-all/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> logoutAll(@PathVariable UUID userId) {
        return Map.of("revoked_sessions", authourizationService.revokeAllSessionForUser(userId));
    }

}
