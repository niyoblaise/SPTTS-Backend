package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.BusDto;
import com.blaise.SPTTS.entity.Bus;
import com.blaise.SPTTS.entity.Route;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import com.blaise.SPTTS.repository.BusRepository;
import com.blaise.SPTTS.repository.RouteRepository;
import com.blaise.SPTTS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepo;
    private final RouteRepository routeRepo;
    private final UserRepository userRepo;

    public BusDto create(BusDto dto) {
        Route route = null;
        if (dto.routeId() != null) {
            route = routeRepo.findById(dto.routeId())
                    .orElseThrow(() -> new RuntimeException("Route not found"));
        }

        User operator = null;
        if (dto.driverId() != null) {
            operator = userRepo.findById(dto.driverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
        }

        Bus bus = Bus.builder()
                .licensePlate(dto.plateNumber())
                .capacity(30) // Default or add to DTO
                .status(BusStatus.ACTIVE)
                .route(route)
                .operator(operator)
                .build();

        Bus saved = busRepo.save(bus);
        return mapToDto(saved);
    }

    public BusDto update(UUID busId, BusDto dto) {
        Bus bus = busRepo.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        if (dto.plateNumber() != null)
            bus.setLicensePlate(dto.plateNumber());
        if (dto.status() != null)
            bus.setStatus(dto.status());

        if (dto.routeId() != null) {
            Route route = routeRepo.findById(dto.routeId())
                    .orElseThrow(() -> new RuntimeException("Route not found"));
            bus.setRoute(route);
        }

        return mapToDto(busRepo.save(bus));
    }

    public void delete(UUID busId) {
        busRepo.deleteById(busId);
    }

    public List<BusDto> list() {
        return busRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<BusDto> listByRoute(UUID routeId) {
        return busRepo.findByRoute_RouteId(routeId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<BusDto> listByOperator(String email) {
        return busRepo.findByOperator_Email(email).stream()
                .map(this::mapToDto)
                .toList();
    }

    private BusDto mapToDto(Bus bus) {
        return new BusDto(
                bus.getBusId(),
                bus.getLicensePlate(),
                "Unknown Model",
                bus.getStatus(),
                bus.getOperator() != null ? bus.getOperator().getUserId() : null,
                bus.getRoute() != null ? bus.getRoute().getRouteId() : null,
                bus.getRoute() != null ? bus.getRoute().getRouteName() : "Unassigned");
    }
}
