package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.NotificationDto;
import com.blaise.SPTTS.DTOs.TripSummaryDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.ENUMS.NotificationType;
import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import com.blaise.SPTTS.entity.ENUMS.UserType;
import com.blaise.SPTTS.entity.Notification;
import com.blaise.SPTTS.entity.Trip;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.repository.NotificationRepository;
import com.blaise.SPTTS.repository.TripRepository;
import com.blaise.SPTTS.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final TripRepository tripRepo;
    private final BusRepository busRepo;
    private final TransportMapper mapper;
    private TripService tripService;

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    public void notifyOperators(Trip trip, NotificationType type) {

        List<com.blaise.SPTTS.entity.Bus> busesOnRoute = busRepo.findByRoute_RouteId(trip.getRoute().getRouteId());

        for (com.blaise.SPTTS.entity.Bus bus : busesOnRoute) {
            Notification n = Notification.builder()
                    .operatorId(bus.getOperator().getUserId().toString())
                    .type(type)
                    .message("New trip request on route " + trip.getRoute().getRouteName())
                    .build();
            repo.save(n);
        }
    }

    public void notifyPassenger(String passengerId, NotificationType type, String msg) {
        Notification n = Notification.builder()
                .passengerId(passengerId)
                .type(type)
                .message(msg)
                .build();
        repo.save(n);
    }

    public List<NotificationDto> unread(User user) {
        if (user.getUserType() == UserType.BUS_OPERATOR) {
            return repo.findByOperatorIdAndReadOrderByTimestampDesc(user.getUserId().toString(), false)
                    .stream().map(mapper::toDto).toList();
        }
        return repo.findByPassengerIdAndReadOrderByTimestampDesc(user.getUserId().toString(), false)
                .stream().map(mapper::toDto).toList();
    }

    @Scheduled(fixedRate = 300000)
    public void sendPeriodicLocationUpdates() {
        if (tripService == null)
            return;
        List<Trip> activeTrips = tripRepo.findAll().stream()
                .filter(trip -> trip.getStatus() == TripStatus.REQUESTED ||
                        trip.getStatus() == TripStatus.CONFIRMATION ||
                        trip.getStatus() == TripStatus.SOON_TO_ARRIVE)
                .toList();

        for (Trip trip : activeTrips) {
            try {
                TripSummaryDto status = tripService.track(trip.getTripId());

                String message = String.format(
                        "Bus Update: %s is %.1f km away. ETA: %d minutes. Current speed: %.0f km/h",
                        status.busPlate(),
                        status.distance(),
                        (int) status.etaMinutes(),
                        status.currentLocation() != null ? status.currentLocation().speed() : 0.0);

                notifyPassenger(
                        trip.getPassenger().getUserId().toString(),
                        NotificationType.LOCATION_UPDATE,
                        message);
            } catch (Exception e) {

                System.err
                        .println("Error sending location update for trip " + trip.getTripId() + ": " + e.getMessage());
            }
        }
    }
}