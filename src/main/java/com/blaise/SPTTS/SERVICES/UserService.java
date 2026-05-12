package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.RegisterRequest;
import com.blaise.SPTTS.DTOs.UserDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blaise.SPTTS.entity.User;

import com.blaise.SPTTS.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final TransportMapper mapper;

    public UserDto add(RegisterRequest req) {
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
        return mapper.toDto(repo.save(user));
    }

    public UserDto findByEmail(String email) {
        return repo.findByEmail(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    public java.util.List<UserDto> findAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public void delete(java.util.UUID userId) {
        repo.deleteById(userId);
    }

    public UserDto update(java.util.UUID userId, UserDto dto) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.fullName() != null) user.setFullName(dto.fullName());
        if (dto.phone() != null) user.setPhone(dto.phone());

        return mapper.toDto(repo.save(user));
    }
}