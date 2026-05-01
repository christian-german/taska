package com.taska.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@AllArgsConstructor
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
                        .jwt(jwt -> {})
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
     * <p>Disables JWT token issuer validation.</p>
     *
     * Useful when the frontend and backend do not access the authentication provider through the same route,
     * for example, in a docker-compose stack including the backend.
     * In this case, since the issuer must be the same, the token is otherwise invalid.
     *
     * @return A {@link JwtDecoder} where issuer validation is not active.
     */
    @Bean
    @ConditionalOnProperty(prefix = "taska.jwt-issuer-validator", name = "disable", havingValue = "true")
    public JwtDecoder jwtTokenDecoder() {

        log.warn("""
           Security!
           ***********************************************
           JWT issuer verification is disabled!
           ***********************************************
           """);

        // Creates a JWT decoder from the issuer configured in the properties.
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri());

        // Only adds the timestamp validator.
        List<OAuth2TokenValidator<Jwt>> oAuth2TokenValidators = List.of(
                new JwtTimestampValidator()
        );
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(oAuth2TokenValidators));

        return jwtDecoder;
    }
}
