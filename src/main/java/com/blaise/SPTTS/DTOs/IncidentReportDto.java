package com.blaise.SPTTS.DTOs;

import java.time.LocalDateTime;

public record IncidentReportDto(
    String incidentId,
    String type,
    String description,
    String location,
    String status,
    String busPlateNumber,
    String operatorName,
    String passengerName,
    LocalDateTime reportedAt
) {
}
