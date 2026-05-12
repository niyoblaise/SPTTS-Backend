package com.blaise.SPTTS.DTOs;

import java.time.Instant;
import java.util.UUID;

public record BusLocationDto(
        UUID busId,
        Double latitude,
        Double longitude,
        Double accuracy,
        Double speed,
        Instant recordedAt
) {}