package com.samson.restbackend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
@Component
@ConfigurationProperties(prefix = "jwt.rotating-keys")
@Getter @Setter
public class RotatingKeysProperties {

    private String issuer;
    private List<KeySpecification> keySpecificationList;
    private String currentKeyId;
    private String accessTtl;
    private String refreshTtl;

    @Getter @Setter
    public static class KeySpecification {
        private String keyId;
        private String secretBase64;
    }
}
