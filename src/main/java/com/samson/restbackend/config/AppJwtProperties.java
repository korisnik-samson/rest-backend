package com.samson.restbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class AppJwtProperties {

    @Getter @Setter
    private String issuer;

    @Getter @Setter
    private String secretBase64;

    @Setter
    private Long accessTTL;

    @Setter
    private Long refreshTTL;

    public Duration getAccessTTL() {
        return accessTTL != null ? Duration.ofMillis(accessTTL) : Duration.ofMinutes(15);
    }

    public Duration getRefreshTTL() {
        return refreshTTL != null ? Duration.ofMillis(refreshTTL) : Duration.ofDays(7);
    }
}
