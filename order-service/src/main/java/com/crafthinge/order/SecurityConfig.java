package com.crafthinge.order;

import com.crafthinge.common.security.SecurityDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            .anyRequest().authenticated());
        return http.build();
    }
}
