package com.crafthinge.user;

import com.crafthinge.common.web.ApiException;
import com.crafthinge.user.AuthDtos.AuthResponse;
import com.crafthinge.user.AuthDtos.LoginRequest;
import com.crafthinge.user.AuthDtos.RegisterRequest;
import com.crafthinge.user.AuthDtos.UserView;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("An account with this email already exists");
        }
        // Demo shortcut: users pick their role at sign-up. A real platform would
        // onboard restaurants and drivers through a verification process.
        User.Role role = req.role() == null ? User.Role.CUSTOMER : req.role();
        User user = users.save(new User(req.email().toLowerCase(), passwordEncoder.encode(req.password()),
            req.fullName(), req.phone(), role));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email())
            .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
            // Same message for "no such user" and "wrong password": don't reveal which emails exist.
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"));
        return response(user);
    }

    private AuthResponse response(User user) {
        return new AuthResponse(tokens.issue(user), tokens.ttlSeconds(), UserView.of(user));
    }
}
