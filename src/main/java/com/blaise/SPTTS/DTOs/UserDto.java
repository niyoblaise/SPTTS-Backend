package com.blaise.SPTTS.DTOs;


import com.blaise.SPTTS.entity.ENUMS.UserType;

import java.util.UUID;

public record UserDto(
        UUID userId,
        String email,
        String fullName,
        String phone,
        UserType userType,
        String licenseNumber,
        String company,
        String authority
) {}