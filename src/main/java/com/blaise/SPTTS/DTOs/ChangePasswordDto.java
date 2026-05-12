package com.blaise.SPTTS.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(@NotBlank String oldPassword,
                                @NotBlank @Size(min = 6) String newPassword) {}