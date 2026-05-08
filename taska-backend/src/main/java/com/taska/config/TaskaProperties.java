package com.taska.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "taska")
@Getter
@Setter
public class TaskaProperties {

    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        /**
         * Indicates whether JWT timeout should be increased by 10 seconds.
         * Spring security by default sets the timeout to 500ms, which is too short for some OIDC servers.
         */
        private boolean increaseTimeout = false;
    }
}
