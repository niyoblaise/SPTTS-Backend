package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.ChangePasswordDto;
import com.blaise.SPTTS.DTOs.LoginRequest;
import com.blaise.SPTTS.DTOs.RegisterRequest;
import com.blaise.SPTTS.entity.ENUMS.UserType;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.repository.UserRepository;
import com.blaise.SPTTS.util.JwtUtil;
import com.blaise.SPTTS.util.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldSaveUser_WhenEmailNotTaken() {
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass123")).thenReturn("encoded");
        authService.register(new RegisterRequest("Alice", "a@b.com", "0788000000", "pass123", UserType.PASSENGER, null, null, null));
        verify(repo).save(any());
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() {
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () ->
            authService.register(new RegisterRequest("Alice", "a@b.com", "0788000000", "pass123", UserType.PASSENGER, null, null, null)));
        verify(repo, never()).save(any());
    }

    @Test
    void login_ShouldReturnTokenAndUser_WhenCredentialsValid() {
        LoginRequest req = new LoginRequest("a@b.com", "pass");
        User user = User.builder().userId(java.util.UUID.randomUUID()).email("a@b.com").fullName("Alice").userType(UserType.PASSENGER).build();
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("a@b.com")).thenReturn("token123");

        var res = authService.login(req);

        assertEquals("token123", res.accessToken());
        assertEquals("Alice", res.user().fullName());
        verify(authManager).authenticate(any());
    }

    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        when(repo.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () ->
            authService.login(new LoginRequest("x@y.com", "pass")));
    }

    @Test
    void changePassword_ShouldUpdate_WhenOldPasswordMatches() {
        User user = User.builder().password("oldEncoded").build();
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("oldPass", "oldEncoded")).thenReturn(true);
        when(encoder.encode("newPass")).thenReturn("newEncoded");

        authService.changePassword("a@b.com", new ChangePasswordDto("oldPass", "newPass"));

        assertEquals("newEncoded", user.getPassword());
        verify(repo).save(user);
    }

    @Test
    void changePassword_ShouldThrow_WhenOldPasswordWrong() {
        User user = User.builder().password("oldEncoded").build();
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "oldEncoded")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
            authService.changePassword("a@b.com", new ChangePasswordDto("wrong", "newPass")));
        verify(repo, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrow_WhenUserNotFound() {
        when(repo.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
            authService.changePassword("x@y.com", new ChangePasswordDto("old", "new")));
    }

    @Test
    void refresh_ShouldThrow_WhenTokenInvalid() {
        when(jwtUtil.getSecret()).thenReturn("QstzqwqHV2ZYB3EzmGZSXKqkm8d7nFfjeeGv3NUbl/sEha7sgfrg9Mu+MDBnf1HYkCZlJVZ/rL0tjgRNYrf/jw==");
        assertThrows(Exception.class, () -> authService.refresh("Bearer invalid-token"));
    }
}
