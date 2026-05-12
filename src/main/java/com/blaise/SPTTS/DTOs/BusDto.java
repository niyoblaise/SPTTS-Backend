package com.blaise.SPTTS.DTOs;

import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import java.util.UUID;

public record BusDto(
        UUID id,
        String plateNumber,
        String model,
        BusStatus status,
        UUID driverId,
        UUID routeId,
        String routeName) {
}