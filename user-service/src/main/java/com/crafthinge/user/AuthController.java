package com.crafthinge.user;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.common.web.ApiException;
import com.crafthinge.user.AuthDtos.AuthResponse;
import com.crafthinge.user.AuthDtos.LoginRequest;
import com.crafthinge.user.AuthDtos.RegisterRequest;
import com.crafthinge.user.AuthDtos.UserView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService auth;
    private final UserRepository users;

    public AuthController(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/api/users/me")
    public UserView me() {
        return users.findById(AuthUser.current().id()).map(UserView::of)
            .orElseThrow(() -> ApiException.notFound("User"));
    }
}
