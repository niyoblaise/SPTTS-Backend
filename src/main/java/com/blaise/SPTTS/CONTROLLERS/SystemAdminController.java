package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.SERVICES.ReportExportService;
import com.blaise.SPTTS.SERVICES.ReportService;
import com.blaise.SPTTS.SERVICES.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemAdminController {

    private final UserService userService;
    private final ReportService reportService;
    private final ReportExportService exportService;

    @PostMapping("/users")
    public UserDto addUser(@RequestBody RegisterRequest dto) {
        return userService.add(dto);
    }

    @GetMapping("/users")
    public List<UserDto> listUsers() {
        return userService.findAll();
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable java.util.UUID userId) {
        userService.delete(userId);
    }

    @PutMapping("/users/{userId}")
    public UserDto updateUser(@PathVariable java.util.UUID userId, @RequestBody UserDto dto) {
        return userService.update(userId, dto);
    }

    @GetMapping("/reports/summary")
    public ReportSummaryDto getSummaryReport() {
        return reportService.generateSummaryReport();
    }

    @GetMapping("/reports/users")
    public List<UserReportDto> getUserReport(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().minusMonths(1)}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.generateUserReport(startDate, endDate);
    }

    @GetMapping("/reports/trips")
    public List<TripReportDto> getTripReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.generateTripReport(startDate, endDate);
    }

    @GetMapping("/reports/revenue")
    public List<RevenueReportDto> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.generateRevenueReport(startDate, endDate);
    }

    @GetMapping("/reports/buses")
    public List<BusReportDto> getBusReport() {
        return reportService.generateBusReport();
    }

    @GetMapping("/reports/incidents")
    public List<IncidentReportDto> getIncidentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.generateIncidentReport(startDate, endDate);
    }

    @GetMapping("/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        return reportService.generateDashboardStats();
    }

    @GetMapping("/export/trips")
    public void exportTripsExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        try {
            byte[] excelData = exportService.exportTripsToExcel(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=trips_report_" + startDate + "_to_" + endDate + ".xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export trips", e);
        }
    }

    @GetMapping("/export/revenue")
    public void exportRevenueExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        try {
            byte[] excelData = exportService.exportRevenueToExcel(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=revenue_report_" + startDate + "_to_" + endDate + ".xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export revenue", e);
        }
    }

    @GetMapping("/export/users")
    public void exportUsersExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        try {
            byte[] excelData = exportService.exportUsersToExcel(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=users_report_" + startDate + "_to_" + endDate + ".xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export users", e);
        }
    }

    @GetMapping("/export/buses")
    public void exportBusesExcel(HttpServletResponse response) {
        try {
            byte[] excelData = exportService.exportBusesToExcel();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=buses_report.xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export buses", e);
        }
    }

    @GetMapping("/export/incidents")
    public void exportIncidentsExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        try {
            byte[] excelData = exportService.exportIncidentsToExcel(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=incidents_report_" + startDate + "_to_" + endDate + ".xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export incidents", e);
        }
    }
}
