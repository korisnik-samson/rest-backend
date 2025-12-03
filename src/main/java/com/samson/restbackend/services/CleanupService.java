package com.samson.restbackend.services;

import com.samson.restbackend.repositories.IdempotencyRepository;
import com.samson.restbackend.repositories.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    private final IdempotencyRepository idempotencyRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // Run an hourly cron job (cron: sec min hour day moth day-of-week)

    public void purgeExpired() {
        var now = OffsetDateTime.now();

        long a = idempotencyRepository.deleteByExpiresAtBefore(now);
        long b = refreshTokenRepository.deleteByExpiresAtBefore(now);

        if (a > 0 || b > 0) logger.info("Purged {} expired idempotency keys and {} expired refresh tokens", a, b);
    }

}
