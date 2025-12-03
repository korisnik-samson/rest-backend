package com.samson.restbackend.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Getter
public class JWTKeyProvider {

    private final String issuer;
    private final Map<String, Key> keysByKeyID;
    private final String currentKeyId;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JWTKeyProvider(@NotNull RotatingKeysProperties rotatingKeysProperties) {
        this.issuer = rotatingKeysProperties.getIssuer();
        this.currentKeyId = rotatingKeysProperties.getCurrentKeyId();

        if (rotatingKeysProperties.getKeySpecificationList() == null || rotatingKeysProperties.getKeySpecificationList().isEmpty())
            throw new IllegalStateException("Key specification list cannot be null or empty");

        this.keysByKeyID = rotatingKeysProperties.getKeySpecificationList().stream().collect(Collectors.toMap(
                RotatingKeysProperties.KeySpecification::getKeyId,
                keySpecification -> toSigningKey(keySpecification.getSecretBase64())
        ));

        this.accessTtl = parseTtl(rotatingKeysProperties.getAccessTtl());
        this.refreshTtl = parseTtl(rotatingKeysProperties.getRefreshTtl());
    }

    private @NotNull Key toSigningKey(String secretBase64) {
        byte[] rawKeyBytes;

        try { rawKeyBytes = Decoders.BASE64URL.decode(secretBase64); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid Base64 secret: " + e.getMessage(), e); }

        if (rawKeyBytes.length < 32) rawKeyBytes = sha256(rawKeyBytes);
            // throw new IllegalArgumentException("Invalid secret length : must be 32 bytes, but found: " + rawKeyBytes.length + " bytes");

        return Keys.hmacShaKeyFor(rawKeyBytes);
    }

    private byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 MessageDigest not available", e);
        }
    }

    private Duration parseTtl(String s) {
        if (s == null) throw new IllegalArgumentException("TTL must not be null");

        s = s.trim().toLowerCase();

        if (s.isEmpty()) throw new IllegalArgumentException("TTL must not be empty");

        try {
            // If it's just digits, treat it as seconds (e.g., "900" -> 900 seconds)
            if (s.matches("\\d+")) return Duration.ofSeconds(Long.parseLong(s));

            if (s.endsWith("m")) {
                long minutes = Long.parseLong(s.substring(0, s.length() - 1));
                return Duration.ofMinutes(minutes);
            }

            if (s.endsWith("h")) {
                long hours = Long.parseLong(s.substring(0, s.length() - 1));
                return Duration.ofHours(hours);
            }

            if (s.endsWith("d")) {
                long days = Long.parseLong(s.substring(0, s.length() - 1));
                return Duration.ofDays(days);
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TTL value is not a valid number: " + s, e);
        }

        throw new IllegalArgumentException("Unsupported TTL format: " + s + ". Use formats like '900', '15m', '2h', '7d'.");
    }

    /*public String issuer() { return issuer; }
    public String currentKid() { return currentKeyId; }
    public Key currentKey() { return keysByKeyID.get(keysByKeyID); }
    public Key keyByKid(String kid) { return keysByKeyID.get(kid); }
    public Duration accessTtl() { return accessTtl; }
    public Duration refreshTtl() { return refreshTtl; }*/

}
