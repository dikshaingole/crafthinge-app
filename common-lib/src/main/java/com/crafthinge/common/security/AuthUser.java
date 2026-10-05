package com.crafthinge.common.security;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** The caller, taken from the validated JWT. */
public record AuthUser(UUID id, String email, List<String> roles) {

    public static final String CUSTOMER = "CUSTOMER";
    public static final String RESTAURANT_OWNER = "RESTAURANT_OWNER";
    public static final String DELIVERY_PARTNER = "DELIVERY_PARTNER";

    public static AuthUser current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No authenticated JWT user in context");
        }
        var jwt = jwtAuth.getToken();
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new AuthUser(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("email"),
            roles == null ? List.of() : roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
