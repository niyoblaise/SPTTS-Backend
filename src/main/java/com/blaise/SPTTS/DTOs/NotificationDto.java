package com.blaise.SPTTS.DTOs;



import com.blaise.SPTTS.entity.ENUMS.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID notificationId,
        String passengerId,
        String operatorId,
        NotificationType type,
        String message,
        boolean read,
        Instant timestamp
) {}