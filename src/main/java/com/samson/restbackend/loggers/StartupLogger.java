package com.samson.restbackend.loggers;

import com.samson.restbackend.security.JWTKeyProvider;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartupLogger {
    private final JWTKeyProvider props;
    private final Environment env;

    @PostConstruct
    public void logStartupInfo() {
        log.info("Active JWT kid={}, DB user={}",
                props.getCurrentKeyId(), env.getProperty("spring.datasource.username"));
    }
}