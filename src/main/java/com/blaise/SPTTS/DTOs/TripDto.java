package com.blaise.SPTTS.DTOs;

import com.blaise.SPTTS.entity.ENUMS.TripStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripDto(
        UUID tripId,
        UUID passengerId,
        UUID busId,
        UUID routeId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        TripStatus status,
        Double fare,
        Integer rating,
        Boolean isPaid,
        String passengerName,
        String passengerEmail) {
}