package com.samson.restbackend.repositories;

import com.samson.restbackend.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJwtId(String jwtId);

    Long deleteByExpiresAtBefore(OffsetDateTime now);

    Long deleteByUserId(UUID userId);

    @Modifying
    @Query(value = "update RefreshToken r set r.revoked = true where r.userId = :userId", nativeQuery = true)
    Long updateByUserIdSetRevokedTrue(UUID userId);

    @Modifying
    @Query(value = "update RefreshToken r set r.revoked = true where r.userId = :userId and r.revoked = false", nativeQuery = true)
    Integer revokeAllForUser(UUID userId);

}
