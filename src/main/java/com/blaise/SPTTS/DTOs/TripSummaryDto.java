package com.blaise.SPTTS.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripSummaryDto(
        UUID tripId,
        BusLocationDto currentLocation,
        double etaMinutes,
        LocalDateTime startTime,
        String busPlate,
        Double fare,
        String status,
        double distance) {
}