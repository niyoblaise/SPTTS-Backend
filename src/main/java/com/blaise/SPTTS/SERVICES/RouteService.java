package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.RouteDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.Route;
import com.blaise.SPTTS.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository repo;
    private final com.blaise.SPTTS.repository.FareRepository fareRepo;
    private final TransportMapper mapper;

    public RouteDto create(RouteDto dto) {
        Route r = mapper.toEntity(dto);
        if (r.getPrice() == null) {
            r.setPrice(0.0);
        }
        return mapper.toDto(repo.save(r));
    }

    public RouteDto get(UUID id) {
        Route route = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        RouteDto dto = mapper.toDto(route);
        Double currentFare = fareRepo
                .findTopByRouteIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(route.getRouteId(),
                        java.time.LocalDate.now())
                .map(com.blaise.SPTTS.entity.Fare::getAmount)
                .orElse(route.getPrice() != null ? route.getPrice() : 0.0);
        return new RouteDto(
                dto.routeId(),
                dto.routeName(),
                dto.description(),
                dto.startStop(),
                dto.destinationStop(),
                dto.endStopLatitude(),
                dto.endStopLongitude(),
                currentFare,
                dto.stops());
    }

    public RouteDto update(UUID id, RouteDto dto) {
        Route route = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (dto.routeName() != null)
            route.setRouteName(dto.routeName());
        if (dto.description() != null)
            route.setDescription(dto.description());
        if (dto.startStop() != null)
            route.setStartStop(dto.startStop());
        if (dto.destinationStop() != null)
            route.setDestinationStop(dto.destinationStop());
        if (dto.endStopLatitude() != null)
            route.setEndStopLatitude(dto.endStopLatitude());
        if (dto.endStopLongitude() != null)
            route.setEndStopLongitude(dto.endStopLongitude());

        return mapper.toDto(repo.save(route));
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }

    public List<RouteDto> list() {
        return repo.findAll().stream()
                .map(route -> {
                    RouteDto dto = mapper.toDto(route);
                    Double currentFare = fareRepo
                            .findTopByRouteIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(route.getRouteId(),
                                    java.time.LocalDate.now())
                            .map(com.blaise.SPTTS.entity.Fare::getAmount)
                            .orElse(route.getPrice() != null ? route.getPrice() : 0.0);
                    return new RouteDto(
                            dto.routeId(),
                            dto.routeName(),
                            dto.description(),
                            dto.startStop(),
                            dto.destinationStop(),
                            dto.endStopLatitude(),
                            dto.endStopLongitude(),
                            currentFare,
                            dto.stops());
                })
                .toList();
    }
}