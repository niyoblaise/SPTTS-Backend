package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.entity.*;
import com.blaise.SPTTS.entity.ENUMS.*;
import com.blaise.SPTTS.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TripRepository tripRepo;
    private final BusLocationRepository locRepo;
    private final BusRepository busRepo;
    private final UserRepository userRepo;
    private final PaymentRepository paymentRepo;
    private final IncidentRepository incidentRepo;
    private final RouteRepository routeRepo;
    private final FareRepository fareRepo;

    public ReportSummaryDto generateSummaryReport() {
        LocalDateTime now = LocalDateTime.now();

        long totalUsers = userRepo.count();
        long totalBuses = busRepo.count();
        long totalRoutes = routeRepo.count();
        long totalTrips = tripRepo.count();
        long totalPayments = paymentRepo.count();
        long totalIncidents = incidentRepo.count();

        double totalRevenue = paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();

        Map<String, Long> usersByType = userRepo.findAll().stream()
                .collect(Collectors.groupingBy(u -> u.getUserType().name(), Collectors.counting()));

        Map<String, Long> tripsByStatus = tripRepo.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        Map<String, Long> paymentsByStatus = paymentRepo.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        Map<String, Long> incidentsByType = incidentRepo.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getType() != null ? i.getType() : "UNKNOWN", Collectors.counting()));

        return new ReportSummaryDto(
                now,
                totalUsers,
                totalBuses,
                totalRoutes,
                totalTrips,
                totalPayments,
                totalIncidents,
                totalRevenue,
                usersByType,
                tripsByStatus,
                paymentsByStatus,
                incidentsByType
        );
    }

    public List<UserReportDto> generateUserReport(LocalDate startDate, LocalDate endDate) {
        List<User> users = userRepo.findAll();

        return users.stream().map(user -> {
            List<Trip> userTrips = tripRepo.findByPassenger_Email(user.getEmail());
            int totalTrips = userTrips.size();

            List<Payment> userPayments = paymentRepo.findAll().stream()
                    .filter(p -> p.getPassenger() != null && p.getPassenger().getUserId().equals(user.getUserId()))
                    .collect(Collectors.toList());
            int totalPayments = userPayments.size();

            return UserReportDto.fromEntity(user, totalTrips, totalPayments);
        }).collect(Collectors.toList());
    }

    public List<TripReportDto> generateTripReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Trip> trips = tripRepo.findAll().stream()
                .filter(t -> t.getStartTime() != null)
                .filter(t -> !t.getStartTime().isBefore(startDateTime) && !t.getStartTime().isAfter(endDateTime))
                .collect(Collectors.toList());

        return trips.stream().map(trip -> new TripReportDto(
                trip.getTripId().toString(),
                trip.getPassenger() != null ? trip.getPassenger().getFullName() : "N/A",
                trip.getPassenger() != null ? trip.getPassenger().getEmail() : "N/A",
                trip.getBus() != null ? trip.getBus().getLicensePlate() : "N/A",
                trip.getRoute() != null ? trip.getRoute().getRouteName() : "N/A",
                trip.getStartTime(),
                trip.getEndTime(),
                trip.getStatus().name(),
                trip.getFare() != null ? trip.getFare() : 0.0,
                trip.getIsPaid() != null ? trip.getIsPaid() : false,
                trip.getRating()
        )).collect(Collectors.toList());
    }

    public List<RevenueReportDto> generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> p.getTrip() != null && p.getTrip().getStartTime() != null)
                .filter(p -> {
                    LocalDate tripDate = p.getTrip().getStartTime().toLocalDate();
                    return !tripDate.isBefore(startDate) && !tripDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        return payments.stream().map(payment -> new RevenueReportDto(
                payment.getPaymentId().toString(),
                payment.getPassenger() != null ? payment.getPassenger().getFullName() : "N/A",
                payment.getTrip() != null ? payment.getTrip().getTripId().toString() : "N/A",
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getTrip() != null ? payment.getTrip().getStartTime() : null
        )).collect(Collectors.toList());
    }

    public List<BusReportDto> generateBusReport() {
        List<Bus> buses = busRepo.findAll();

        return buses.stream().map(bus -> {
            List<Trip> busTrips = tripRepo.findByBus_Operator_Email(bus.getOperator() != null ? bus.getOperator().getEmail() : "");
            long completedTrips = busTrips.stream()
                    .filter(t -> t.getStatus() == TripStatus.TRIP_COMPLETED)
                    .count();
            double revenue = busTrips.stream()
                    .filter(t -> t.getIsPaid() != null && t.getIsPaid())
                    .mapToDouble(t -> t.getFare() != null ? t.getFare() : 0.0)
                    .sum();
            double completionRate = busTrips.isEmpty() ? 0 : (completedTrips * 100.0 / busTrips.size());

            return new BusReportDto(
                    bus.getBusId().toString(),
                    bus.getLicensePlate(),
                    bus.getStatus().name(),
                    bus.getOperator() != null ? bus.getOperator().getFullName() : "N/A",
                    bus.getRoute() != null ? bus.getRoute().getRouteName() : "N/A",
                    busTrips.size(),
                    (int) completedTrips,
                    revenue,
                    completionRate
            );
        }).collect(Collectors.toList());
    }

    public List<IncidentReportDto> generateIncidentReport(LocalDate startDate, LocalDate endDate) {
        List<Incident> incidents = incidentRepo.findAll().stream()
                .filter(i -> i.getReportedAt() != null)
                .filter(i -> {
                    LocalDate incidentDate = i.getReportedAt().toLocalDate();
                    return !incidentDate.isBefore(startDate) && !incidentDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        return incidents.stream().map(incident -> new IncidentReportDto(
                incident.getIncidentId().toString(),
                incident.getType(),
                incident.getDescription(),
                incident.getLocation(),
                incident.getStatus(),
                incident.getBusId() != null ? incident.getBusId().toString() : "N/A",
                incident.getOperatorId() != null ? incident.getOperatorId() : "N/A",
                incident.getPassengerId() != null ? incident.getPassengerId().toString() : "N/A",
                incident.getReportedAt()
        )).collect(Collectors.toList());
    }

    public Map<String, Object> generateDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepo.count());
        stats.put("totalBuses", busRepo.count());
        stats.put("activeBuses", busRepo.countByStatus(BusStatus.ACTIVE));
        stats.put("totalRoutes", routeRepo.count());
        stats.put("totalTrips", tripRepo.count());
        stats.put("completedTrips", tripRepo.findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.TRIP_COMPLETED)
                .count());
        stats.put("totalRevenue", paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum());
        stats.put("pendingPayments", paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count());
        stats.put("totalIncidents", incidentRepo.count());
        stats.put("openIncidents", incidentRepo.findAll().stream()
                .filter(i -> "OPEN".equalsIgnoreCase(i.getStatus()))
                .count());

        List<Trip> todayTrips = tripRepo.findByStartTimeBetween(
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );
        stats.put("todayTrips", todayTrips.size());

        double avgRating = tripRepo.findAll().stream()
                .filter(t -> t.getRating() != null)
                .mapToInt(Trip::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);

        return stats;
    }
}
