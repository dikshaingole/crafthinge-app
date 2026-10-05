package com.crafthinge.food;

import com.crafthinge.common.security.SecurityDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http, SecurityDefaults defaults) throws Exception {
        defaults.apply(http).authorizeHttpRequests(a -> a
            .requestMatchers(SecurityDefaults.OPS_ENDPOINTS).permitAll()
            .requestMatchers(HttpMethod.GET, "/api/menu/**").permitAll()
            .anyRequest().authenticated());
        return http.build();
    }
}
