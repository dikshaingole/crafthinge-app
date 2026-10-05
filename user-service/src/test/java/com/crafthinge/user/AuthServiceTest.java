package com.crafthinge.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.crafthinge.common.web.ApiException;
import com.crafthinge.user.AuthDtos.LoginRequest;
import com.crafthinge.user.AuthDtos.RegisterRequest;
import java.time.Duration;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private AuthService service;

    @BeforeEach
    void setUp() {
        var key = new SecretKeySpec("test-secret-test-secret-test-secret-123".getBytes(), "HmacSHA256");
        service = new AuthService(users, encoder, new TokenService(key, Duration.ofHours(1)));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegisterRequest("a@b.com", "Password123!", "A", null, null)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).status()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void registerDefaultsToCustomerAndReturnsToken() {
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        var res = service.register(new RegisterRequest("New@B.com", "Password123!", "New", null, null));
        assertThat(res.user().role()).isEqualTo("CUSTOMER");
        assertThat(res.user().email()).isEqualTo("new@b.com");
        assertThat(res.token()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() {
        var user = new User("a@b.com", encoder.encode("right-password"), "A", null, User.Role.CUSTOMER);
        when(users.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.login(new LoginRequest("a@b.com", "wrong-password")))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).status()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
