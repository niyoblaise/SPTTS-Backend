package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.BusAcceptDto;
import com.blaise.SPTTS.DTOs.IncidentDto;
import com.blaise.SPTTS.DTOs.TripDto;
import com.blaise.SPTTS.SERVICES.GpsService;
import com.blaise.SPTTS.SERVICES.IncidentService;
import com.blaise.SPTTS.SERVICES.TripService;
import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/operator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUS_OPERATOR') or hasRole('SYSTEM_ADMIN')")
public class BusOperatorController {

    private final TripService tripService;
    private final GpsService gpsService;
    private final IncidentService incidentService;
    private final com.blaise.SPTTS.SERVICES.BusService busService;

    @PostMapping("/accept-request")
    public void accept(@Valid @RequestBody BusAcceptDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        tripService.accept(dto);
    }

    @PutMapping("/trips/{tripId}/arrived")
    public void arrived(@PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails principal) {
        tripService.markArrived(tripId, principal.getUsername());
    }

    @PutMapping("/trips/{tripId}/complete")
    public void complete(@PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails principal) {
        tripService.markCompleted(tripId, principal.getUsername());
    }

    @PutMapping("/buses/{busId}/status")
    public void updateStatus(@PathVariable UUID busId,
            @RequestParam BusStatus status,
            @AuthenticationPrincipal UserDetails principal) {
        gpsService.updateBusStatus(busId, status, principal.getUsername());
    }

    @GetMapping("/my-trips")
    public List<TripDto> myTrips(@AuthenticationPrincipal UserDetails principal) {
        return tripService.tripsByOperator(principal.getUsername());
    }

    @PostMapping("/incidents")
    public void reportIncident(@Valid @RequestBody IncidentDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        incidentService.submit(dto, principal.getUsername());
    }

    @GetMapping("/my-buses")
    public List<com.blaise.SPTTS.DTOs.BusDto> myBuses(@AuthenticationPrincipal UserDetails principal) {
        return busService.listByOperator(principal.getUsername());
    }
}