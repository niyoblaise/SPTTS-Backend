package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.RouteDto;
import com.blaise.SPTTS.DTOs.StopDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.SERVICES.RouteService;
import com.blaise.SPTTS.repository.BusStopRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final BusStopRepository stopRepo;
    private final TransportMapper mapper;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public RouteDto create(@Valid @RequestBody RouteDto routeDto) {
        return routeService.create(routeDto);
    }

    @GetMapping("/{routeId}")
    public RouteDto findById(@PathVariable UUID routeId) {
        return routeService.get(routeId);
    }

    @PutMapping("/{routeId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public RouteDto update(@PathVariable UUID routeId, @Valid @RequestBody RouteDto routeDto) {
        return routeService.update(routeId, routeDto);
    }

    @DeleteMapping("/{routeId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID routeId) {
        routeService.delete(routeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<RouteDto> list() {
        return routeService.list();
    }

    @GetMapping("/{routeId}/stops")
    public List<StopDto> stops(@PathVariable UUID routeId) {
        return stopRepo.findByRoute_RouteIdOrderByStopOrder(routeId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
