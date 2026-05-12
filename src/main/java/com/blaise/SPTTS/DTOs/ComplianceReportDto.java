package com.blaise.SPTTS.DTOs;


import java.time.LocalDate;

public record ComplianceReportDto(
        LocalDate reportDate,
        long speedingEvents,
        double onTimePercentage,
        long completedTrips
) {}