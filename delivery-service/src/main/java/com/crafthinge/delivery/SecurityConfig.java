package com.crafthinge.delivery;

import com.crafthinge.common.security.SecurityDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http, SecurityDefaults defaults) throws Exception {
        defaults.apply(http).authorizeHttpRequests(a -> a
            .requestMatchers(SecurityDefaults.OPS_ENDPOINTS).permitAll()
            .requestMatchers("/internal/events").permitAll() // cluster-internal only (see NetworkPolicy, Phase 6)
            .requestMatchers("/api/deliveries/**").hasRole("DELIVERY_PARTNER")
            .anyRequest().authenticated());
        return http.build();
    }
}
