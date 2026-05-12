package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.entity.Incident;
import com.blaise.SPTTS.repository.BusRepository;
import com.blaise.SPTTS.repository.IncidentRepository;
import com.blaise.SPTTS.repository.RouteRepository;
import com.blaise.SPTTS.repository.FineRepository;
import com.blaise.SPTTS.repository.AuditLogRepository;
import com.blaise.SPTTS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/rura")
@RequiredArgsConstructor
public class RuraController {

    private final IncidentRepository incidentRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final FineRepository fineRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @GetMapping("/incidents")
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalIncidents", incidentRepository.count());
        stats.put("activeBuses", busRepository.count());
        stats.put("activeRoutes", routeRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/operators/compliance")
    public ResponseEntity<List<com.blaise.SPTTS.dto.OperatorComplianceDTO>> getOperatorCompliance() {
        List<com.blaise.SPTTS.entity.User> operators = userRepository.findAll().stream()
                .filter(u -> u.getUserType() == com.blaise.SPTTS.entity.ENUMS.UserType.BUS_OPERATOR)
                .collect(java.util.stream.Collectors.toList());

        List<Incident> allIncidents = incidentRepository.findAll();

        List<com.blaise.SPTTS.dto.OperatorComplianceDTO> complianceList = operators.stream().map(op -> {
            long incidentCount = allIncidents.stream()
                    .filter(inc -> inc.getOperatorId() != null && inc.getOperatorId().equals(op.getEmail()))
                    .count();

            double score = Math.max(0, 100 - (incidentCount * 5));

            List<String> plates = busRepository.findByOperator_UserId(op.getUserId()).stream()
                    .map(com.blaise.SPTTS.entity.Bus::getLicensePlate)
                    .collect(java.util.stream.Collectors.toList());

            return com.blaise.SPTTS.dto.OperatorComplianceDTO.builder()
                    .operatorId(op.getUserId())
                    .name(op.getFullName())
                    .email(op.getEmail())
                    .company(op.getCompany())
                    .licenseNumber(op.getLicenseNumber())
                    .totalIncidents((int) incidentCount)
                    .complianceScore(score)
                    .status(op.getAccountStatus())
                    .activePlates(plates)
                    .build();
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(complianceList);
    }

    @PutMapping("/operators/{id}/suspend")
    public ResponseEntity<?> suspendOperator(@PathVariable java.util.UUID id,
            java.security.Principal principal) {
        com.blaise.SPTTS.entity.User operator = userRepository.findById(id).orElseThrow();
        operator.setAccountStatus("SUSPENDED");
        userRepository.save(operator);

        com.blaise.SPTTS.entity.AuditLog log = com.blaise.SPTTS.entity.AuditLog.builder()
                .action("SUSPEND_OPERATOR")
                .performedBy(principal.getName())
                .timestamp(java.time.LocalDateTime.now())
                .details("Suspended operator " + operator.getEmail())
                .build();
        auditLogRepository.save(log);

        return ResponseEntity.ok("Operator suspended");
    }

    @PutMapping("/operators/{id}/activate")
    public ResponseEntity<?> activateOperator(@PathVariable java.util.UUID id,
            java.security.Principal principal) {
        com.blaise.SPTTS.entity.User operator = userRepository.findById(id).orElseThrow();
        operator.setAccountStatus("ACTIVE");
        userRepository.save(operator);

        com.blaise.SPTTS.entity.AuditLog log = com.blaise.SPTTS.entity.AuditLog.builder()
                .action("ACTIVATE_OPERATOR")
                .performedBy(principal.getName())
                .timestamp(java.time.LocalDateTime.now())
                .details("Activated operator " + operator.getEmail())
                .build();
        auditLogRepository.save(log);

        return ResponseEntity.ok("Operator activated");
    }

    @PostMapping("/fines")
    public ResponseEntity<?> issueFine(
            @RequestBody com.blaise.SPTTS.entity.Fine fine,
            java.security.Principal principal) {
        fine.setIssuedBy(principal.getName());
        fine.setIssuedAt(java.time.LocalDateTime.now());
        fineRepository.save(fine);

        com.blaise.SPTTS.entity.AuditLog log = com.blaise.SPTTS.entity.AuditLog.builder()
                .action("ISSUE_FINE")
                .performedBy(principal.getName())
                .timestamp(java.time.LocalDateTime.now())
                .details("Issued fine of " + fine.getAmount() + " to " + fine.getIssuedTo())
                .build();
        auditLogRepository.save(log);

        return ResponseEntity.ok("Fine issued successfully");
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<com.blaise.SPTTS.entity.AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp")));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<byte[]> exportMonthlyReport() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("SPTTS Monthly Report")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE));
            
            document.add(new Paragraph("Generated: " + java.time.LocalDate.now())
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph("\n"));

            // System Statistics Section
            document.add(new Paragraph("System Statistics")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));

            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            
            statsTable.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold()));
            statsTable.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));
            
            statsTable.addCell("Total Incidents");
            statsTable.addCell(String.valueOf(incidentRepository.count()));
            
            statsTable.addCell("Active Buses");
            statsTable.addCell(String.valueOf(busRepository.count()));
            
            statsTable.addCell("Active Routes");
            statsTable.addCell(String.valueOf(routeRepository.count()));
            
            statsTable.addCell("Total Operators");
            statsTable.addCell(String.valueOf(userRepository.count()));

            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // Operator Compliance Section
            document.add(new Paragraph("Operator Compliance Summary")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));

            List<com.blaise.SPTTS.entity.User> operators = userRepository.findAll().stream()
                    .filter(u -> u.getUserType() == com.blaise.SPTTS.entity.ENUMS.UserType.BUS_OPERATOR)
                    .collect(java.util.stream.Collectors.toList());

            List<Incident> allIncidents = incidentRepository.findAll();

            if (!operators.isEmpty()) {
                Table complianceTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 20, 15, 15}))
                        .setWidth(UnitValue.createPercentValue(100));

                complianceTable.addHeaderCell(new Cell().add(new Paragraph("Operator").setBold()));
                complianceTable.addHeaderCell(new Cell().add(new Paragraph("Company").setBold()));
                complianceTable.addHeaderCell(new Cell().add(new Paragraph("License").setBold()));
                complianceTable.addHeaderCell(new Cell().add(new Paragraph("Incidents").setBold()));
                complianceTable.addHeaderCell(new Cell().add(new Paragraph("Score").setBold()));

                for (com.blaise.SPTTS.entity.User op : operators) {
                    long incidentCount = allIncidents.stream()
                            .filter(inc -> inc.getOperatorId() != null && inc.getOperatorId().equals(op.getEmail()))
                            .count();

                    double score = Math.max(0, 100 - (incidentCount * 5));

                    complianceTable.addCell(op.getFullName() != null ? op.getFullName() : "N/A");
                    complianceTable.addCell(op.getCompany() != null ? op.getCompany() : "N/A");
                    complianceTable.addCell(op.getLicenseNumber() != null ? op.getLicenseNumber() : "N/A");
                    complianceTable.addCell(String.valueOf(incidentCount));
                    complianceTable.addCell(String.format("%.1f%%", score));
                }

                document.add(complianceTable);
            } else {
                document.add(new Paragraph("No operators found.").setFontColor(ColorConstants.GRAY));
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "SPTTS_Monthly_Report_" + java.time.YearMonth.now() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    @PutMapping("/incidents/{id}/review")
    public ResponseEntity<?> reviewIncident(@PathVariable java.util.UUID id,
            java.security.Principal principal) {
        Incident incident = incidentRepository.findById(id).orElseThrow();
        incident.setStatus("REVIEWED");
        incidentRepository.save(incident);

        com.blaise.SPTTS.entity.AuditLog log = com.blaise.SPTTS.entity.AuditLog.builder()
                .action("REVIEW_INCIDENT")
                .performedBy(principal.getName())
                .timestamp(java.time.LocalDateTime.now())
                .details("Reviewed incident " + id)
                .build();
        auditLogRepository.save(log);

        return ResponseEntity.ok("Incident marked as reviewed");
    }
}
