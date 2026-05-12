package com.blaise.SPTTS.DTOs;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BusRequestDto(
        @NotNull UUID routeId,
        UUID destinationStopId,
        UUID busId) {
}