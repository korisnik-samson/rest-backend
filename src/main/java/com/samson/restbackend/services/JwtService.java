package com.samson.restbackend.services;

import com.samson.restbackend.models.RefreshToken;
import com.samson.restbackend.repositories.RefreshTokenRepository;
import com.samson.restbackend.security.JWTKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JWTKeyProvider keyProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public JwtService(JWTKeyProvider keyProvider, RefreshTokenRepository refreshTokenRepository) {
        this.keyProvider = keyProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(@NonNull UUID userId, String username, String role) {
        OffsetDateTime now = OffsetDateTime.now();

        Date iat = Date.from(now.toInstant());
        Date exp = Date.from(now.plus(keyProvider.getAccessTtl()).toInstant());

        Key key = keyProvider.getKeysByKeyID().get(keyProvider.getCurrentKeyId());

        return Jwts.builder().subject(userId.toString()).issuer(keyProvider.getIssuer())
                .issuedAt(iat).expiration(exp).claim("username", username)
                .claim("role", role).signWith(key).compact();
    }

    public String generateRefreshToken(@NonNull UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();

        Date iat = Date.from(now.toInstant());
        Date exp = Date.from(now.plus(keyProvider.getRefreshTtl()).toInstant());

        String jwtId = UUID.randomUUID().toString();
        Key key = keyProvider.getKeysByKeyID().get(keyProvider.getCurrentKeyId());

        String token = Jwts.builder().subject(userId.toString()).issuer(keyProvider.getIssuer()).id(jwtId)
                .issuedAt(iat).expiration(exp).claim("typ", "refresh").signWith(key).compact();

        refreshTokenRepository.save(
                RefreshToken.builder().userId(userId)
                        .jwtId(jwtId).expiresAt(OffsetDateTime.ofInstant(exp.toInstant(), now.getOffset()))
                        .revoked(false).build()
        );

        return token;
    }

    public Jws<Claims> validateAccessToken(@NonNull String token) {
        return Jwts.parser().verifyWith((SecretKey) keyProvider.getKeysByKeyID().get(keyProvider.getCurrentKeyId())).build().parseSignedClaims(token);
    }

    public Jws<Claims> parseAndValidate(String jwt) {
        return Jwts.parser().keyLocator(header -> {
            // String kid = header.get("kid", String.class);

            String keyId = header.get("kid").toString();
            Key key = keyProvider.getKeysByKeyID().get(keyId);

            if (key == null) throw new SecurityException("Unknown Key ID: " + keyId);

            return key;
        }).build().parseSignedClaims(jwt);
    }

    public Boolean isRefreshTokenExpired(String jwtId) {
        return refreshTokenRepository.findByJwtId(jwtId)
                .map(refreshToken -> refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(OffsetDateTime.now()))
                .orElse(true);
    }

    public void revokeRefreshToken(String jwtId) {
        refreshTokenRepository.findByJwtId(jwtId).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

}
