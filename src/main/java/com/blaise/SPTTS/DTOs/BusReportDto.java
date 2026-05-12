package com.blaise.SPTTS.DTOs;

public record BusReportDto(
    String busId,
    String licensePlate,
    String status,
    String driverName,
    String routeName,
    int totalTrips,
    int completedTrips,
    double totalRevenue,
    double completionRate
) {
}
