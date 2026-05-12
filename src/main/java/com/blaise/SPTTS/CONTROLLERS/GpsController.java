package com.blaise.SPTTS.CONTROLLERS;


import com.blaise.SPTTS.DTOs.BusLocationDto;
import com.blaise.SPTTS.SERVICES.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gps")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService service;

    @PostMapping("/update")
    public void update(@RequestBody BusLocationDto dto) {
        service.save(dto);
    }

    @GetMapping("/{busId}/latest")
    public BusLocationDto latest(@PathVariable UUID busId) {
        return service.getLatest(busId);
    }

    @GetMapping("/{busId}/history")
    public List<BusLocationDto> history(@PathVariable UUID busId) {
        return service.getHistory(busId);
    }
}