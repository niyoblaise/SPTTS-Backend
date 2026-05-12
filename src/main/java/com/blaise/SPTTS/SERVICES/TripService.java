package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.*;
import com.blaise.SPTTS.entity.ENUMS.NotificationType;
import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import com.blaise.SPTTS.repository.*;
import com.blaise.SPTTS.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final BusRepository busRepo;
    private final RouteRepository routeRepo;
    private final FareRepository fareRepo;
    private final NotificationService notifService;
    private final BusLocationRepository busLocationRepo;
    private final TransportMapper mapper;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Set up bidirectional dependency for scheduled notifications
        notifService.setTripService(this);
    }

    @Transactional
    public TripDto createTrip(String passengerEmail, BusRequestDto dto) {
        User passenger = userRepo.findByEmail(passengerEmail).orElseThrow();
        Route route = routeRepo.findById(dto.routeId()).orElseThrow();

        double fareAmount = fareRepo
                .findTopByRouteIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(route.getRouteId(),
                        java.time.LocalDate.now())
                .map(Fare::getAmount)
                .orElse(route.getPrice() != null ? route.getPrice() : 0.0);

        Trip trip = Trip.builder()
                .passenger(passenger)
                .route(route)
                .status(TripStatus.REQUESTED)
                .startTime(LocalDateTime.now())
                .fare(fareAmount)
                .build();

        if (dto.busId() != null) {
            Bus bus = busRepo.findById(dto.busId()).orElse(null);
            if (bus != null) {
                trip.setBus(bus);
                // Status remains REQUESTED until operator confirms
            }
        }

        trip = tripRepo.save(trip);
        notifService.notifyOperators(trip, NotificationType.BUS_REQUEST);
        return mapper.toDto(trip);
    }

    @Transactional
    public void cancel(UUID tripId, String passengerEmail) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (!trip.getPassenger().getEmail().equals(passengerEmail)) {
            throw new IllegalArgumentException("Not your trip");
        }
        if (trip.getStatus() == TripStatus.TRIP_COMPLETED) {
            throw new IllegalArgumentException("Trip already completed");
        }
        trip.setStatus(TripStatus.CANCELLED);
        tripRepo.save(trip);
    }

    @Transactional
    public void rate(UUID tripId, String passengerEmail, int score) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (!trip.getPassenger().getEmail().equals(passengerEmail)) {
            throw new IllegalArgumentException("Not your trip");
        }
        if (trip.getStatus() != TripStatus.TRIP_COMPLETED) {
            throw new IllegalArgumentException("Trip not completed yet");
        }
        if (trip.getRating() != null) {
            throw new IllegalArgumentException("Trip has already been rated");
        }
        trip.setRating(score);
        tripRepo.save(trip);

        // Send rating confirmation notification
        String routeName = trip.getRoute() != null ? trip.getRoute().getRouteName() : "your trip";
        String message = String.format("Thank you for rating your trip to %s with %d stars",
                routeName, score);
        notifService.notifyPassenger(
                trip.getPassenger().getUserId().toString(),
                NotificationType.TRIP_RATED,
                message);
    }

    public List<TripDto> historyByPassenger(String passengerEmail) {
        return tripRepo.findByPassenger_Email(passengerEmail)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void accept(BusAcceptDto dto) {
        Trip trip = tripRepo.findById(dto.tripId()).orElseThrow();

        // Find a bus owned by this operator that is on the same route as the trip
        Bus bus = busRepo.findByOperator_UserId(dto.operatorId())
                .stream()
                .filter(b -> b.getRoute().getRouteId().equals(trip.getRoute().getRouteId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No bus found for operator on this route"));

        trip.setBus(bus);
        trip.setStatus(TripStatus.CONFIRMATION);
        trip.setStartTime(LocalDateTime.now()); // Reset start time for simulation
        tripRepo.save(trip);
        notifService.notifyPassenger(trip.getPassenger().getUserId().toString(),
                NotificationType.CONFIRMATION,
                "Bus " + bus.getLicensePlate() + " assigned");
    }

    public TripSummaryDto track(UUID tripId) {
        Trip trip = tripRepo.findById(tripId).orElseThrow();

        if (trip.getStatus() == TripStatus.REQUESTED) {
            return new TripSummaryDto(
                    tripId,
                    null,
                    -1,
                    trip.getStartTime(),
                    trip.getBus() != null ? trip.getBus().getLicensePlate() : "Pending Assignment",
                    trip.getFare(),
                    "WAITING_FOR_APPROVAL",
                    0.0);
        }

        // Define route: Nyabugogo to Remera (approximate coordinates)
        double startLat = -1.9397;
        double startLon = 30.0444;
        double endLat = -1.9585;
        double endLon = 30.1112;

        // Calculate total distance
        double totalDistanceKm = GeoUtil.haversine(startLat, startLon, endLat, endLon);
        double averageSpeedKph = 40.0; // 40 km/h

        // Calculate expected duration in minutes
        int totalDurationMinutes = 10; // Fixed 10 minutes simulation as requested

        // Calculate elapsed time
        long elapsedMinutes = java.time.Duration.between(trip.getStartTime(), LocalDateTime.now()).toMinutes();

        // Calculate progress (0.0 to 1.0)
        double progress = Math.min(1.0, (double) elapsedMinutes / totalDurationMinutes);

        // Interpolate current position
        double currentLat = startLat + (endLat - startLat) * progress;
        double currentLon = startLon + (endLon - startLon) * progress;

        // Add some small random jitter for "realism" (traffic, GPS noise)
        // But keep it deterministic-ish if we wanted, or just random noise is fine for
        // "live" feel
        // We'll add very small noise so it doesn't look like a perfect line if zoomed
        // in
        double noiseFactor = 0.0002; // ~20 meters
        currentLat += (Math.random() * noiseFactor * 2) - noiseFactor;
        currentLon += (Math.random() * noiseFactor * 2) - noiseFactor;

        double remainingDistance = totalDistanceKm * (1.0 - progress);
        int etaMinutes = Math.max(0, totalDurationMinutes - (int) elapsedMinutes);

        // If real bus data exists, use it (optional, but for now we force simulation as
        // requested)
        // If you want to fallback to real data:
        // if (trip.getBus() != null) { ... check real location ... }

        // For this task, we return the simulated data
        BusLocationDto simulatedLocation = new BusLocationDto(
                trip.getBus() != null ? trip.getBus().getBusId() : null,
                currentLat,
                currentLon,
                10.0,
                averageSpeedKph,
                Instant.now());

        return new TripSummaryDto(
                tripId,
                simulatedLocation,
                etaMinutes,
                trip.getStartTime(),
                trip.getBus() != null ? trip.getBus().getLicensePlate() : "Simulated Bus",
                trip.getFare() > 0 ? trip.getFare() : 500.0,
                progress >= 1.0 ? "ARRIVED" : trip.getStatus().name(),
                remainingDistance);
    }

    @Transactional
    public void markArrived(UUID tripId, String operatorEmail) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (!trip.getBus().getOperator().getEmail().equals(operatorEmail)) {
            throw new IllegalArgumentException("Not your trip");
        }
        trip.setStatus(TripStatus.SOON_TO_ARRIVE);
        tripRepo.save(trip);
    }

    @Transactional
    public void markCompleted(UUID tripId, String operatorEmail) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (!trip.getBus().getOperator().getEmail().equals(operatorEmail)) {
            throw new IllegalArgumentException("Not your trip");
        }
        trip.setStatus(TripStatus.TRIP_COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        tripRepo.save(trip);
    }

    public List<TripDto> tripsByOperator(String operatorEmail) {

        List<Bus> operatorBuses = busRepo.findByOperator_Email(operatorEmail);

        List<UUID> routeIds = operatorBuses.stream()
                .map(bus -> bus.getRoute().getRouteId())
                .distinct()
                .toList();

        List<Trip> unassignedRequests = new java.util.ArrayList<>();
        for (UUID routeId : routeIds) {
            unassignedRequests.addAll(tripRepo.findByRoute_RouteIdAndStatus(routeId, TripStatus.REQUESTED));
        }

        List<Trip> assignedTrips = tripRepo.findByBus_Operator_Email(operatorEmail);

        List<Trip> allTrips = new java.util.ArrayList<>(unassignedRequests);
        allTrips.addAll(assignedTrips);

        allTrips.sort((t1, t2) -> t2.getStartTime().compareTo(t1.getStartTime()));

        return allTrips.stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<TripDto> getAllTrips() {
        return tripRepo.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }
}
