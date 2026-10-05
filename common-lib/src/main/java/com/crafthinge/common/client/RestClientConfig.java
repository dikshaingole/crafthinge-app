package com.crafthinge.common.client;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service-to-service calls use Kubernetes Service DNS names
 * (http://food-service:8080), never pod IPs. The caller's JWT is forwarded so
 * the downstream service applies the same user's permissions.
 * Timeouts: spring.http.client.connect-timeout / read-timeout in application.yml.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestClientCustomizer forwardAuthorizationHeader() {
        return builder -> builder.requestInterceptor((request, body, execution) -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                && !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                String header = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (header != null) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, header);
                }
            }
            return execution.execute(request, body);
        });
    }
}
