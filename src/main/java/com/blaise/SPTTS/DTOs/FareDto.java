package com.blaise.SPTTS.DTOs;

import java.time.LocalDate;
import java.util.UUID;

public record FareDto(
                UUID fareId,
                String routeName,
                UUID routeId,
                double amount,
                LocalDate effectiveDate) {
}