package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.SERVICES.ReportService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/regulator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('REGULATOR') or hasRole('SYSTEM_ADMIN')")
public class RegulatorController {

    private final ReportService reportService;

    @GetMapping("/compliance-report")
    public Map<String, Object> getComplianceReport(@RequestParam LocalDate date) {
        return reportService.generateDashboardStats();
    }
}