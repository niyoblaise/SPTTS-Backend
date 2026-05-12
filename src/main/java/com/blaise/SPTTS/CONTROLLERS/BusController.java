package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.BusDto;
import com.blaise.SPTTS.SERVICES.BusService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BUS_OPERATOR')")
    public BusDto create(@Valid @RequestBody BusDto dto) {
        return busService.create(dto);
    }

    @PutMapping("/{busId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BUS_OPERATOR')")
    public BusDto update(@PathVariable UUID busId, @Valid @RequestBody BusDto dto) {
        return busService.update(busId, dto);
    }

    @DeleteMapping("/{busId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID busId) {
        busService.delete(busId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<BusDto> list() {
        return busService.list();
    }

    @GetMapping("/route/{routeId}")
    public List<BusDto> listByRoute(@PathVariable UUID routeId) {
        return busService.listByRoute(routeId);
    }
}
