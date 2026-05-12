package com.blaise.SPTTS.DTOs;



import com.blaise.SPTTS.entity.ENUMS.UserType;
import jakarta.validation.constraints.*;


public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank @Size(min = 6) String password,
        @NotNull UserType userType,
        String licenseNumber,
        String company,
        String authority
) {}