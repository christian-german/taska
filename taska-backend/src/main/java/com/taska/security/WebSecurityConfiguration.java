package com.taska.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfiguration {

    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(_ -> {
                        })
                );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*");
            }
        };
    }

    /**
     * Configures and returns a {@link JwtDecoder} bean for decoding JWT tokens.
     * The decoder is initialized using the issuer URI specified in the application
     * properties and optionally customizes the JWT validation logic based on configuration.
     * Additionally, HTTP connection and read timeout settings are applied.
     *
     * @return a fully configured {@link JwtDecoder} instance for validating and decoding JWT tokens
     */
    @Bean
    public JwtDecoder jwtTokenDecoder() {

        if (oAuth2ResourceServerProperties.getJwt().getIssuerUri() == null) {
            throw new IllegalStateException("spring.security.oauth2.resourceserver.jwt.issuer-uri must be set");
        }

        NimbusJwtDecoder nimbusJwtDecoder;

        if (taskaProperties.getSecurity().isIncreaseTimeout()) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(5));
            factory.setReadTimeout(Duration.ofSeconds(10));

            RestTemplate restTemplate = new RestTemplate(factory);

            nimbusJwtDecoder = NimbusJwtDecoder
                    .withIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri())
                    .restOperations(restTemplate)
                    .build();
        } else {
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri())
                    .build();
        }

        // Customize JWT validation logic if required
        if (taskaProperties.getSecurity().isDisableIssuerValidation()) {
            nimbusJwtDecoder.setJwtValidator(JwtValidators.createDefault());
        }

        return nimbusJwtDecoder;
    }
}
