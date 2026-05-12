package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.StopDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.BusStop;
import com.blaise.SPTTS.entity.Route;
import com.blaise.SPTTS.exception.ResourceNotFoundException;
import com.blaise.SPTTS.repository.BusStopRepository;
import com.blaise.SPTTS.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusStopService {

    private final BusStopRepository stopRepo;
    private final RouteRepository routeRepo;
    private final TransportMapper mapper;

    public StopDto create(StopDto dto) {
        Route route = routeRepo.findById(dto.routeId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        BusStop stop = mapper.toEntity(dto);
        stop.setRoute(route);

        return mapper.toDto(stopRepo.save(stop));
    }

    public StopDto update(UUID stopId, StopDto dto) {
        BusStop stop = stopRepo.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found"));

        stop.setName(dto.name());
        stop.setLatitude(dto.latitude());
        stop.setLongitude(dto.longitude());
        stop.setStopOrder(dto.stopOrder());

        if (dto.routeId() != null && !dto.routeId().equals(stop.getRoute().getRouteId())) {
            Route route = routeRepo.findById(dto.routeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
            stop.setRoute(route);
        }

        return mapper.toDto(stopRepo.save(stop));
    }

    public void delete(UUID stopId) {
        stopRepo.deleteById(stopId);
    }

    public StopDto findById(UUID stopId) {
        return stopRepo.findById(stopId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found"));
    }

    public List<StopDto> list() {
        return stopRepo.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }
}
