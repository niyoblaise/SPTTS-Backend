package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.entity.*;
import com.blaise.SPTTS.entity.ENUMS.*;
import com.blaise.SPTTS.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TripRepository tripRepo;
    @Mock private BusLocationRepository locRepo;
    @Mock private BusRepository busRepo;
    @Mock private UserRepository userRepo;
    @Mock private PaymentRepository paymentRepo;
    @Mock private IncidentRepository incidentRepo;
    @Mock private RouteRepository routeRepo;
    @Mock private FareRepository fareRepo;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateSummaryReport_ShouldContainAllFields() {
        when(userRepo.count()).thenReturn(100L);
        when(busRepo.count()).thenReturn(20L);
        when(routeRepo.count()).thenReturn(5L);
        when(tripRepo.count()).thenReturn(500L);
        when(paymentRepo.count()).thenReturn(400L);
        when(incidentRepo.count()).thenReturn(10L);
        when(userRepo.findAll()).thenReturn(List.of(
            User.builder().userType(UserType.PASSENGER).build(),
            User.builder().userType(UserType.BUS_OPERATOR).build()
        ));
        when(tripRepo.findAll()).thenReturn(List.of(
            Trip.builder().status(TripStatus.TRIP_COMPLETED).build(),
            Trip.builder().status(TripStatus.CANCELLED).build()
        ));
        when(paymentRepo.findAll()).thenReturn(List.of(
            Payment.builder().amount(50.0).status(PaymentStatus.COMPLETED).build(),
            Payment.builder().amount(30.0).status(PaymentStatus.PENDING).build()
        ));
        when(incidentRepo.findAll()).thenReturn(List.of(Incident.builder().type("ACCIDENT").build()));
        ReportSummaryDto report = reportService.generateSummaryReport();
        assertEquals(100, report.totalUsers());
        assertEquals(20, report.totalBuses());
        assertEquals(50.0, report.totalRevenue(), 0.001);
        assertFalse(report.usersByType().isEmpty());
    }

    @Test
    void generateSummaryReport_ShouldHandleZeroCounts() {
        when(userRepo.count()).thenReturn(0L); when(busRepo.count()).thenReturn(0L);
        when(routeRepo.count()).thenReturn(0L); when(tripRepo.count()).thenReturn(0L);
        when(paymentRepo.count()).thenReturn(0L); when(incidentRepo.count()).thenReturn(0L);
        when(userRepo.findAll()).thenReturn(Collections.emptyList());
        when(tripRepo.findAll()).thenReturn(Collections.emptyList());
        when(paymentRepo.findAll()).thenReturn(Collections.emptyList());
        when(incidentRepo.findAll()).thenReturn(Collections.emptyList());
        ReportSummaryDto report = reportService.generateSummaryReport();
        assertEquals(0, report.totalUsers());
        assertEquals(0.0, report.totalRevenue(), 0.001);
    }

    @Test
    void generateTripReport_ShouldFilterByDateRange() {
        when(tripRepo.findAll()).thenReturn(List.of(
            Trip.builder().tripId(UUID.randomUUID()).startTime(LocalDateTime.of(2026, 5, 1, 10, 0)).status(TripStatus.TRIP_COMPLETED).build(),
            Trip.builder().tripId(UUID.randomUUID()).startTime(LocalDateTime.of(2026, 4, 1, 10, 0)).status(TripStatus.CONFIRMATION).build()
        ));
        List<TripReportDto> r = reportService.generateTripReport(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertEquals(1, r.size());
        assertEquals("TRIP_COMPLETED", r.get(0).status());
    }

    @Test
    void generateTripReport_ShouldHandleNullStartTime() {
        when(tripRepo.findAll()).thenReturn(List.of(Trip.builder().startTime(null).build()));
        assertTrue(reportService.generateTripReport(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)).isEmpty());
    }

    @Test
    void generateRevenueReport_ShouldFilterByDateRange() {
        Trip trip = Trip.builder().tripId(UUID.randomUUID()).startTime(LocalDateTime.of(2026, 5, 15, 10, 0)).build();
        when(paymentRepo.findAll()).thenReturn(List.of(
            Payment.builder().paymentId(UUID.randomUUID()).amount(25.0).status(PaymentStatus.COMPLETED).trip(trip).build()
        ));
        List<RevenueReportDto> r = reportService.generateRevenueReport(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertEquals(1, r.size());
        assertEquals(25.0, r.get(0).amount(), 0.001);
    }

    @Test
    void generateRevenueReport_ShouldExcludeOutOfRange() {
        Trip trip = Trip.builder().startTime(LocalDateTime.of(2025, 1, 1, 10, 0)).build();
        when(paymentRepo.findAll()).thenReturn(List.of(Payment.builder().amount(100.0).trip(trip).build()));
        assertTrue(reportService.generateRevenueReport(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)).isEmpty());
    }

    @Test
    void generateBusReport_ShouldCalcCompletionRate() {
        Bus b = Bus.builder().busId(UUID.randomUUID()).licensePlate("ABC123").status(BusStatus.ACTIVE).build();
        when(busRepo.findAll()).thenReturn(List.of(b));
        when(tripRepo.findByBus_Operator_Email("")).thenReturn(List.of(
            Trip.builder().tripId(UUID.randomUUID()).status(TripStatus.TRIP_COMPLETED).fare(20.0).isPaid(true).build(),
            Trip.builder().tripId(UUID.randomUUID()).status(TripStatus.CANCELLED).fare(10.0).isPaid(false).build()
        ));
        List<BusReportDto> r = reportService.generateBusReport();
        assertEquals("ABC123", r.get(0).licensePlate());
        assertEquals(20.0, r.get(0).totalRevenue(), 0.001);
        assertEquals(50.0, r.get(0).completionRate(), 0.001);
    }

    @Test
    void generateBusReport_ShouldHandleZeroTrips() {
        Bus b = Bus.builder().busId(UUID.randomUUID()).licensePlate("ABC123").status(BusStatus.ACTIVE).operator(null).build();
        when(busRepo.findAll()).thenReturn(List.of(b));
        when(tripRepo.findByBus_Operator_Email("")).thenReturn(Collections.emptyList());
        assertEquals(0, reportService.generateBusReport().get(0).totalTrips());
    }

    @Test
    void generateIncidentReport_ShouldFilterByDateRange() {
        when(incidentRepo.findAll()).thenReturn(List.of(
            Incident.builder().incidentId(UUID.randomUUID()).type("ACCIDENT").reportedAt(LocalDateTime.of(2026, 5, 15, 10, 0)).build(),
            Incident.builder().incidentId(UUID.randomUUID()).type("BREAKDOWN").reportedAt(LocalDateTime.of(2026, 3, 1, 10, 0)).build()
        ));
        List<IncidentReportDto> r = reportService.generateIncidentReport(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertEquals(1, r.size());
        assertEquals("ACCIDENT", r.get(0).type());
    }

    @Test
    void generateIncidentReport_ShouldHandleNullReportedAt() {
        when(incidentRepo.findAll()).thenReturn(List.of(Incident.builder().type("ACCIDENT").reportedAt(null).build()));
        assertTrue(reportService.generateIncidentReport(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)).isEmpty());
    }

    @Test
    void generateDashboardStats_ShouldContainAllKeys() {
        when(userRepo.count()).thenReturn(50L); when(busRepo.count()).thenReturn(30L);
        when(busRepo.countByStatus(BusStatus.ACTIVE)).thenReturn(25L);
        when(routeRepo.count()).thenReturn(10L); when(tripRepo.count()).thenReturn(200L);
        when(paymentRepo.findAll()).thenReturn(List.of(Payment.builder().amount(100.0).status(PaymentStatus.COMPLETED).build()));
        when(incidentRepo.findAll()).thenReturn(List.of(Incident.builder().status("OPEN").build()));
        when(tripRepo.findByStartTimeBetween(any(), any())).thenReturn(Collections.emptyList());
        when(tripRepo.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> s = reportService.generateDashboardStats();
        assertEquals(50L, s.get("totalUsers"));
        assertEquals(25L, s.get("activeBuses"));
        assertEquals(200L, s.get("totalTrips"));
    }
}
