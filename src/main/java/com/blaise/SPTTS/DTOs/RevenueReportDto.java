package com.blaise.SPTTS.DTOs;

import java.time.LocalDateTime;

public record RevenueReportDto(
    String paymentId,
    String passengerName,
    String tripId,
    double amount,
    String status,
    LocalDateTime paymentDate
) {
}
