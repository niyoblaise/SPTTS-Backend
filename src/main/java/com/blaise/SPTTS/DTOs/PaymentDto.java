package com.blaise.SPTTS.DTOs;


import com.blaise.SPTTS.entity.ENUMS.PaymentStatus;

import java.util.UUID;

public record PaymentDto(
        UUID paymentId,
        UUID passengerId,
        UUID tripId,
        double amount,
        PaymentStatus status
) {}