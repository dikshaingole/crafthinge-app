package com.crafthinge.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        User.Role role) { }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) { }

    public record UserView(UUID id, String email, String fullName, String phone, String role) {
        static UserView of(User u) {
            return new UserView(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole().name());
        }
    }

    public record AuthResponse(String token, long expiresInSeconds, UserView user) { }
}
