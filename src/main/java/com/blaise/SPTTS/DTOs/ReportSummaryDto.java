package com.blaise.SPTTS.DTOs;

import java.time.LocalDateTime;
import java.util.Map;

public record ReportSummaryDto(
    LocalDateTime generatedAt,
    long totalUsers,
    long totalBuses,
    long totalRoutes,
    long totalTrips,
    long totalPayments,
    long totalIncidents,
    double totalRevenue,
    Map<String, Long> usersByType,
    Map<String, Long> tripsByStatus,
    Map<String, Long> paymentsByStatus,
    Map<String, Long> incidentsByType
) {
}
