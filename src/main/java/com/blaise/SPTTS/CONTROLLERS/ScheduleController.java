package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.TripDto;
import com.blaise.SPTTS.SERVICES.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final TripService tripService;

    @GetMapping
    public List<TripDto> getSchedules(@RequestParam(required = false) UUID routeId) {

        return tripService.getAllTrips(); 
    }
}
