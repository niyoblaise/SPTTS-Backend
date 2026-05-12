package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.StopDto;
import com.blaise.SPTTS.SERVICES.BusStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
public class BusStopController {

    private final BusStopService stopService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public StopDto create(@Valid @RequestBody StopDto dto) {
        return stopService.create(dto);
    }

    @PutMapping("/{stopId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public StopDto update(@PathVariable UUID stopId, @Valid @RequestBody StopDto dto) {
        return stopService.update(stopId, dto);
    }

    @DeleteMapping("/{stopId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID stopId) {
        stopService.delete(stopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{stopId}")
    public StopDto findById(@PathVariable UUID stopId) {
        return stopService.findById(stopId);
    }

    @GetMapping
    public List<StopDto> list() {
        return stopService.list();
    }
}
