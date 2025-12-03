package com.samson.restbackend.services;

import com.samson.restbackend.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthourizationService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthourizationService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public int revokeAllSessionForUser(UUID userId) {
        return refreshTokenRepository.revokeAllForUser(userId);
    }

}
