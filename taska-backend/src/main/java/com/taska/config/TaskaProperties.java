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

    private final Security security = new Security();

    @Getter
    @Setter
    public static class Security {

        /**
         * Indicates whether JWT timeout should be increased by 10 seconds.
         * Spring security by default sets the timeout to 500ms, which is too short for some OIDC servers.
         */
        private boolean increaseTimeout = false;

        /**
         * Indicates whether issuer validation should be disabled.
         * This is useful when the issuer URI is not available or cannot be verified.
         */
        private boolean disableIssuerValidation = false;
    }
}
