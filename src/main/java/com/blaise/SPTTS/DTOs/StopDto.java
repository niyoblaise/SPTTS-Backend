package com.blaise.SPTTS.DTOs;


import java.util.UUID;

public record StopDto(
        UUID stopId,
        UUID routeId,
        String name,
        double latitude,
        double longitude,
        int stopOrder
) {}