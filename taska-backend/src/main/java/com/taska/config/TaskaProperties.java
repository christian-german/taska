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

    private final JwtIssuerValidator jwtIssuerValidator = new JwtIssuerValidator();

    @Getter
    @Setter
    public static class JwtIssuerValidator {
        /**
         * Indicates whether JWT issuer validation should be disabled.
         */
        private boolean disable = false;
    }
}
