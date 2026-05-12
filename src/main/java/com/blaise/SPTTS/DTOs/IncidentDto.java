package com.blaise.SPTTS.DTOs;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record IncidentDto(@NotBlank String description,
        UUID tripId,
        UUID busId,
        String type,
        String location) {
}