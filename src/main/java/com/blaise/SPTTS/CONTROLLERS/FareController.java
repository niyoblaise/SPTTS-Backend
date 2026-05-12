package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.FareDto;
import com.blaise.SPTTS.SERVICES.FareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/fares")
@RequiredArgsConstructor
public class FareController {

    private final FareService fareService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public FareDto create(@Valid @RequestBody FareDto fareDto) {
        return fareService.create(fareDto);
    }

    @GetMapping("/{fareId}")
    public FareDto findById(@PathVariable UUID fareId) {
        return fareService.findById(fareId);
    }

    @PutMapping("/{fareId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public FareDto update(@PathVariable UUID fareId, @Valid @RequestBody FareDto fareDto) {
        return fareService.update(fareId, fareDto);
    }

    @DeleteMapping("/{fareId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID fareId) {
        fareService.delete(fareId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<FareDto> list() {
        return fareService.list();
    }
}