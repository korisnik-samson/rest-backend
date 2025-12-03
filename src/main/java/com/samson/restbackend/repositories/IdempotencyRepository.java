package com.samson.restbackend.repositories;

import com.samson.restbackend.models.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {
    Long deleteByExpiresAtBefore(java.time.OffsetDateTime cutoff);
}
