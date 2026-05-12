package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.SERVICES.FareService;
import com.blaise.SPTTS.SERVICES.NotificationService;
import com.blaise.SPTTS.SERVICES.PaymentService;
import com.blaise.SPTTS.SERVICES.TripService;
import com.blaise.SPTTS.repository.BusRepository;
import com.blaise.SPTTS.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passenger")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PASSENGER') or hasRole('SYSTEM_ADMIN') or hasRole('BUS_OPERATOR')")
public class PassengerController {

    private final TripService tripService;
    private final FareService fareService;
    private final NotificationService notifService;
    private final PaymentService paymentService;
    private final BusRepository busRepo;
    private final UserRepository userRepo;
    private final TransportMapper mapper;
    private final com.blaise.SPTTS.SERVICES.IncidentService incidentService;

    @PostMapping("/request-bus")
    public TripDto requestBus(@Valid @RequestBody BusRequestDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        return tripService.createTrip(principal.getUsername(), dto);
    }

    @GetMapping("/routes/{routeId}/fare")
    public FareDto getFare(@PathVariable UUID routeId) {
        return fareService.currentFare(routeId);
    }

    @GetMapping("/trips/{tripId}/track")
    public TripSummaryDto track(@PathVariable UUID tripId) {
        return tripService.track(tripId);
    }

    @GetMapping("/notifications/unread")
    public List<NotificationDto> unread(@AuthenticationPrincipal UserDetails principal) {
        com.blaise.SPTTS.entity.User user = userRepo.findByEmail(principal.getUsername()).orElseThrow();
        return notifService.unread(user);
    }

    @DeleteMapping("/trips/{tripId}/cancel")
    public void cancel(@PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails principal) {
        tripService.cancel(tripId, principal.getUsername());
    }

    @GetMapping("/trips/history")
    public List<TripDto> history(@AuthenticationPrincipal UserDetails principal) {
        return tripService.historyByPassenger(principal.getUsername());
    }

    @PostMapping("/trips/{tripId}/pay")
    public PaymentDto pay(@PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails principal) {
        return paymentService.processForTrip(tripId, principal.getUsername());
    }

    @PostMapping("/trips/{tripId}/rate")
    public void rate(@PathVariable UUID tripId,
            @Valid @RequestBody RatingDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        tripService.rate(tripId, principal.getUsername(), dto.score());
    }

    @GetMapping("/routes/{routeId}/buses")
    public List<BusDto> busesOnRoute(@PathVariable UUID routeId) {
        return busRepo.findByRoute_RouteId(routeId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping("/trips/{tripId}/payment")
    public PaymentDto makePayment(@PathVariable UUID tripId,
            @AuthenticationPrincipal UserDetails principal) {
        return paymentService.processForTrip(tripId, principal.getUsername());
    }

    @PostMapping("/incidents")
    public void reportIncident(@Valid @RequestBody IncidentDto dto,
            @AuthenticationPrincipal UserDetails principal) {
        incidentService.submitPassenger(dto, principal.getUsername());
    }
}