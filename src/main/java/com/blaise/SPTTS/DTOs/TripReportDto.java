package com.blaise.SPTTS.DTOs;

import java.time.LocalDateTime;

public record TripReportDto(
    String tripId,
    String passengerName,
    String passengerEmail,
    String busPlateNumber,
    String routeName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String status,
    double fare,
    boolean isPaid,
    Integer rating
) {
}
