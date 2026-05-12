package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    private final ReportService reportService;

    public ReportExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] exportTripsToExcel(LocalDate startDate, LocalDate endDate) {
        List<TripReportDto> trips = reportService.generateTripReport(startDate, endDate);
        return createTripExcel(trips, "Trips Report", startDate, endDate);
    }

    public byte[] exportRevenueToExcel(LocalDate startDate, LocalDate endDate) {
        List<RevenueReportDto> payments = reportService.generateRevenueReport(startDate, endDate);
        return createRevenueExcel(payments, "Revenue Report", startDate, endDate);
    }

    public byte[] exportUsersToExcel(LocalDate startDate, LocalDate endDate) {
        List<UserReportDto> users = reportService.generateUserReport(startDate, endDate);
        return createUserExcel(users, "Users Report", startDate, endDate);
    }

    public byte[] exportBusesToExcel() {
        List<BusReportDto> buses = reportService.generateBusReport();
        return createBusExcel(buses, "Buses Report");
    }

    public byte[] exportIncidentsToExcel(LocalDate startDate, LocalDate endDate) {
        List<IncidentReportDto> incidents = reportService.generateIncidentReport(startDate, endDate);
        return createIncidentExcel(incidents, "Incidents Report", startDate, endDate);
    }

    private byte[] createTripExcel(List<TripReportDto> trips, String title, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trips");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + " (" + startDate + " to " + endDate + ")");
            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Trip ID", "Passenger", "Email", "Bus Plate", "Route", "Start Time", "End Time", "Status", "Fare", "Paid", "Rating"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (TripReportDto trip : trips) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(trip.tripId() != null ? trip.tripId().substring(0, 8) : "");
                row.createCell(1).setCellValue(trip.passengerName());
                row.createCell(2).setCellValue(trip.passengerEmail());
                row.createCell(3).setCellValue(trip.busPlateNumber());
                row.createCell(4).setCellValue(trip.routeName());
                row.createCell(5).setCellValue(trip.startTime() != null ? trip.startTime().format(formatter) : "");
                row.createCell(6).setCellValue(trip.endTime() != null ? trip.endTime().format(formatter) : "");
                row.createCell(7).setCellValue(trip.status());
                row.createCell(8).setCellValue(trip.fare());
                row.createCell(9).setCellValue(trip.isPaid() ? "Yes" : "No");
                row.createCell(10).setCellValue(trip.rating() != null ? trip.rating() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export trips to Excel", e);
        }
    }

    private byte[] createRevenueExcel(List<RevenueReportDto> payments, String title, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue");

            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + " (" + startDate + " to " + endDate + ")");
            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Payment ID", "Passenger", "Trip ID", "Amount", "Status", "Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (RevenueReportDto payment : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(payment.paymentId() != null ? payment.paymentId().substring(0, 8) : "");
                row.createCell(1).setCellValue(payment.passengerName());
                row.createCell(2).setCellValue(payment.tripId() != null ? payment.tripId().substring(0, 8) : "");
                row.createCell(3).setCellValue(payment.amount());
                row.createCell(4).setCellValue(payment.status());
                row.createCell(5).setCellValue(payment.paymentDate() != null ? payment.paymentDate().format(formatter) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export revenue to Excel", e);
        }
    }

    private byte[] createUserExcel(List<UserReportDto> users, String title, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + " (" + startDate + " to " + endDate + ")");
            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"User ID", "Full Name", "Email", "User Type", "Company", "Status", "Created At", "Total Trips", "Total Payments"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (UserReportDto user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.userId() != null ? user.userId().substring(0, 8) : "");
                row.createCell(1).setCellValue(user.fullName());
                row.createCell(2).setCellValue(user.email());
                row.createCell(3).setCellValue(user.userType());
                row.createCell(4).setCellValue(user.company() != null ? user.company() : "");
                row.createCell(5).setCellValue(user.accountStatus());
                row.createCell(6).setCellValue(user.createdAt() != null ?
                    java.time.LocalDateTime.ofInstant(user.createdAt(), java.time.ZoneId.systemDefault()).format(formatter) : "");
                row.createCell(7).setCellValue(user.totalTrips());
                row.createCell(8).setCellValue(user.totalPayments());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export users to Excel", e);
        }
    }

    private byte[] createBusExcel(List<BusReportDto> buses, String title) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Buses");

            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Bus ID", "License Plate", "Status", "Driver", "Route", "Total Trips", "Completed Trips", "Revenue", "Completion Rate"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (BusReportDto bus : buses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(bus.busId() != null ? bus.busId().substring(0, 8) : "");
                row.createCell(1).setCellValue(bus.licensePlate());
                row.createCell(2).setCellValue(bus.status());
                row.createCell(3).setCellValue(bus.driverName());
                row.createCell(4).setCellValue(bus.routeName());
                row.createCell(5).setCellValue(bus.totalTrips());
                row.createCell(6).setCellValue(bus.completedTrips());
                row.createCell(7).setCellValue(bus.totalRevenue());
                row.createCell(8).setCellValue(String.format("%.2f%%", bus.completionRate()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export buses to Excel", e);
        }
    }

    private byte[] createIncidentExcel(List<IncidentReportDto> incidents, String title, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Incidents");

            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title + " (" + startDate + " to " + endDate + ")");
            titleCell.setCellStyle(headerStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Incident ID", "Type", "Description", "Location", "Status", "Bus", "Operator", "Passenger", "Reported At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (IncidentReportDto incident : incidents) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(incident.incidentId() != null ? incident.incidentId().substring(0, 8) : "");
                row.createCell(1).setCellValue(incident.type());
                row.createCell(2).setCellValue(incident.description() != null ? incident.description() : "");
                row.createCell(3).setCellValue(incident.location() != null ? incident.location() : "");
                row.createCell(4).setCellValue(incident.status());
                row.createCell(5).setCellValue(incident.busPlateNumber());
                row.createCell(6).setCellValue(incident.operatorName());
                row.createCell(7).setCellValue(incident.passengerName());
                row.createCell(8).setCellValue(incident.reportedAt() != null ? incident.reportedAt().format(formatter) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export incidents to Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }
}
