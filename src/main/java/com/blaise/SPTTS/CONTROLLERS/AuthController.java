package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.SERVICES.AuthService;
import com.blaise.SPTTS.SERVICES.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestHeader("Authorization") String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserDetails principal) {
        return userService.findByEmail(principal.getUsername());
    }

    @PutMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        authService.changePassword(principal.getUsername(), dto);
    }

    @GetMapping("/debug-authorities")
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> debugAuthorities(
            @AuthenticationPrincipal UserDetails principal) {
        return principal.getAuthorities();
    }
}