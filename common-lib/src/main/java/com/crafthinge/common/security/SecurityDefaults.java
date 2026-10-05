package com.crafthinge.common.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

/**
 * Settings shared by every service's SecurityFilterChain:
 * stateless REST API, no CSRF (no cookies), bearer-token (JWT) authentication.
 * Each service then adds its own URL rules.
 */
@Component
public class SecurityDefaults {

    /** Probes and metrics. Not routed by the Ingress, so only reachable inside the cluster. */
    public static final String[] OPS_ENDPOINTS = {
        "/actuator/health/**", "/actuator/info", "/actuator/prometheus"
    };

    private final JwtAuthenticationConverter converter;

    public SecurityDefaults(JwtAuthenticationConverter converter) {
        this.converter = converter;
    }

    public HttpSecurity apply(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)));
    }
}
