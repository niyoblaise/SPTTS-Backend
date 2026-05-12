package com.blaise.SPTTS.DTOs;

import com.blaise.SPTTS.entity.User;

public record UserReportDto(
    String userId,
    String fullName,
    String email,
    String userType,
    String company,
    String accountStatus,
    java.time.Instant createdAt,
    int totalTrips,
    int totalPayments
) {
    public static UserReportDto fromEntity(User user, int totalTrips, int totalPayments) {
        return new UserReportDto(
            user.getUserId().toString(),
            user.getFullName(),
            user.getEmail(),
            user.getUserType() != null ? user.getUserType().name() : "UNKNOWN",
            user.getCompany(),
            user.getAccountStatus() != null ? user.getAccountStatus() : "UNKNOWN",
            user.getCreatedAt(),
            totalTrips,
            totalPayments
        );
    }
}
