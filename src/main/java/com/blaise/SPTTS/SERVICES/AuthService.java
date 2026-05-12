package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.ChangePasswordDto;
import com.blaise.SPTTS.DTOs.LoginRequest;
import com.blaise.SPTTS.DTOs.LoginResponse;
import com.blaise.SPTTS.DTOs.RegisterRequest;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.repository.UserRepository;
import com.blaise.SPTTS.util.JwtUtil;
import com.blaise.SPTTS.util.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public void register(RegisterRequest req) {
        if (repo.findByEmail(req.email()).isPresent())
            throw new IllegalArgumentException("Email already registered");
        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .phone(req.phone())
                .password(encoder.encode(req.password()))
                .userType(req.userType())
                .licenseNumber(req.licenseNumber())
                .company(req.company())
                .authority(req.authority())
                .build();
        repo.save(user);
    }

    public LoginResponse refresh(String refreshToken) {
        String raw = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtUtil.getSecret())))
                .build()
                .parseClaimsJws(raw)
                .getBody();
        String email = claims.getSubject();
        String newToken = jwtUtil.generateToken(email);
        User userEntity = repo.findByEmail(email).orElseThrow();
        return new LoginResponse(newToken, refreshToken,
                new com.blaise.SPTTS.DTOs.UserDto(
                        userEntity.getUserId(),
                        userEntity.getEmail(),
                        userEntity.getFullName(),
                        userEntity.getPhone(),
                        userEntity.getUserType(),
                        userEntity.getLicenseNumber(),
                        userEntity.getCompany(),
                        userEntity.getAuthority()));
    }

    public void changePassword(String email, ChangePasswordDto dto) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!encoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password incorrect");
        }
        user.setPassword(encoder.encode(dto.newPassword()));
        repo.save(user);
    }

    public LoginResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String token = jwtUtil.generateToken(req.email());
        String refreshToken = jwtUtil.generateToken(req.email()); // Using same logic for now if no specific refresh
                                                                  // method
        User user = repo.findByEmail(req.email()).orElseThrow();

        return new LoginResponse(token, refreshToken,
                new com.blaise.SPTTS.DTOs.UserDto(
                        user.getUserId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhone(),
                        user.getUserType(),
                        user.getLicenseNumber(),
                        user.getCompany(),
                        user.getAuthority()));
    }
}
