package com.blaise.SPTTS.DTOs;

import java.util.List;
import java.util.UUID;

public record RouteDto(
        UUID routeId,
        String routeName,
        String description,
        String startStop,
        String destinationStop,
        Double endStopLatitude,
        Double endStopLongitude,
        Double price,
        List<StopDto> stops) {
}