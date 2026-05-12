package com.blaise.SPTTS.DTOs;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BusAcceptDto(
        @NotNull UUID tripId,
        @NotNull UUID operatorId
) {}