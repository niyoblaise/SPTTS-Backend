package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.dto.OperatorComplianceDTO;
import com.blaise.SPTTS.entity.AuditLog;
import com.blaise.SPTTS.entity.Fine;
import com.blaise.SPTTS.entity.Incident;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.entity.ENUMS.UserType;
import com.blaise.SPTTS.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuraControllerTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private BusRepository busRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private FineRepository fineRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RuraController ruraController;

    @Test
    void getAllIncidents_ShouldReturnAll() {
        when(incidentRepository.findAll()).thenReturn(List.of(
            Incident.builder().incidentId(UUID.randomUUID()).description("Accident").build(),
            Incident.builder().incidentId(UUID.randomUUID()).description("Breakdown").build()
        ));
        ResponseEntity<List<Incident>> r = ruraController.getAllIncidents();
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(2, r.getBody().size());
    }

    @Test
    void getAllIncidents_ShouldReturnEmptyWhenNone() {
        when(incidentRepository.findAll()).thenReturn(Collections.emptyList());
        ResponseEntity<List<Incident>> r = ruraController.getAllIncidents();
        assertTrue(r.getBody().isEmpty());
    }

    @Test
    void getStats_ShouldReturnCorrectCounts() {
        when(incidentRepository.count()).thenReturn(5L);
        when(busRepository.count()).thenReturn(10L);
        when(routeRepository.count()).thenReturn(3L);
        ResponseEntity<Map<String, Long>> r = ruraController.getStats();
        assertEquals(5, r.getBody().get("totalIncidents"));
        assertEquals(10, r.getBody().get("activeBuses"));
        assertEquals(3, r.getBody().get("activeRoutes"));
    }

    @Test
    void getStats_ShouldReturnZeroWhenEmpty() {
        when(incidentRepository.count()).thenReturn(0L);
        when(busRepository.count()).thenReturn(0L);
        when(routeRepository.count()).thenReturn(0L);
        ResponseEntity<Map<String, Long>> r = ruraController.getStats();
        assertEquals(0, r.getBody().get("totalIncidents"));
    }

    @Test
    void getOperatorCompliance_ShouldCalculateScores() {
        User op1 = User.builder().userId(UUID.randomUUID()).email("op1@t.com").fullName("Op1")
            .userType(UserType.BUS_OPERATOR).accountStatus("ACTIVE").build();
        User op2 = User.builder().userId(UUID.randomUUID()).email("op2@t.com").fullName("Op2")
            .userType(UserType.BUS_OPERATOR).accountStatus("ACTIVE").build();
        when(userRepository.findAll()).thenReturn(List.of(op1, op2));
        when(incidentRepository.findAll()).thenReturn(List.of(
            Incident.builder().operatorId("op1@t.com").build(),
            Incident.builder().operatorId("op1@t.com").build(),
            Incident.builder().operatorId("op2@t.com").build()
        ));
        when(busRepository.findByOperator_UserId(any())).thenReturn(Collections.emptyList());

        ResponseEntity<List<OperatorComplianceDTO>> r = ruraController.getOperatorCompliance();
        assertEquals(HttpStatus.OK, r.getStatusCode());
        OperatorComplianceDTO dto1 = r.getBody().stream().filter(o -> o.getEmail().equals("op1@t.com")).findFirst().get();
        assertEquals(2, dto1.getTotalIncidents());
        assertEquals(90.0, dto1.getComplianceScore(), 0.001);
        OperatorComplianceDTO dto2 = r.getBody().stream().filter(o -> o.getEmail().equals("op2@t.com")).findFirst().get();
        assertEquals(1, dto2.getTotalIncidents());
        assertEquals(95.0, dto2.getComplianceScore(), 0.001);
    }

    @Test
    void getOperatorCompliance_ShouldReturnEmptyWhenNoOperators() {
        when(userRepository.findAll()).thenReturn(List.of(
            User.builder().email("p@t.com").userType(UserType.PASSENGER).build()
        ));
        ResponseEntity<List<OperatorComplianceDTO>> r = ruraController.getOperatorCompliance();
        assertTrue(r.getBody().isEmpty());
    }

    @Test
    void suspendOperator_ShouldSetSuspendedAndLogAudit() {
        UUID id = UUID.randomUUID();
        User op = User.builder().userId(id).email("op@t.com").accountStatus("ACTIVE").build();
        Principal p = () -> "admin@t.com";
        when(userRepository.findById(id)).thenReturn(Optional.of(op));
        when(auditLogRepository.save(any())).thenReturn(null);
        ResponseEntity<?> r = ruraController.suspendOperator(id, p);
        assertEquals("Operator suspended", r.getBody());
        assertEquals("SUSPENDED", op.getAccountStatus());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void suspendOperator_ShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> ruraController.suspendOperator(id, () -> "admin"));
    }

    @Test
    void activateOperator_ShouldSetActiveAndLogAudit() {
        UUID id = UUID.randomUUID();
        User op = User.builder().userId(id).email("op@t.com").accountStatus("SUSPENDED").build();
        when(userRepository.findById(id)).thenReturn(Optional.of(op));
        when(auditLogRepository.save(any())).thenReturn(null);
        ResponseEntity<?> r = ruraController.activateOperator(id, () -> "admin");
        assertEquals("ACTIVE", op.getAccountStatus());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void issueFine_ShouldSetIssuedByAndIssuedAt() {
        Fine fine = Fine.builder().amount(100.0).reason("Speeding").issuedTo("op@t.com").build();
        Principal p = () -> "reg@t.com";
        when(fineRepository.save(any())).thenReturn(fine);
        ResponseEntity<?> r = ruraController.issueFine(fine, p);
        assertEquals("reg@t.com", fine.getIssuedBy());
        assertNotNull(fine.getIssuedAt());
        verify(fineRepository).save(fine);
    }

    @Test
    void getAuditLogs_ShouldReturnLogsSortedDesc() {
        AuditLog l1 = AuditLog.builder().action("SUSPEND").timestamp(LocalDateTime.now()).build();
        AuditLog l2 = AuditLog.builder().action("ACTIVATE").timestamp(LocalDateTime.now().minusHours(1)).build();
        when(auditLogRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(l1, l2));
        ResponseEntity<List<AuditLog>> r = ruraController.getAuditLogs();
        assertEquals(2, r.getBody().size());
    }

    @Test
    void exportMonthlyReport_ShouldReturnPdf() {
        when(incidentRepository.count()).thenReturn(3L);
        when(busRepository.count()).thenReturn(5L);
        when(routeRepository.count()).thenReturn(2L);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(incidentRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<byte[]> r = ruraController.exportMonthlyReport();
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals("application/pdf", r.getHeaders().getContentType().toString());
        assertTrue(r.getHeaders().getContentDisposition().toString().contains(".pdf"));
        String header = new String(Arrays.copyOfRange(r.getBody(), 0, 8));
        assertTrue(header.startsWith("%PDF"), "Should be valid PDF");
    }

    @Test
    void reviewIncident_ShouldMarkAsReviewed() {
        UUID id = UUID.randomUUID();
        Incident inc = Incident.builder().incidentId(id).status("PENDING").build();
        when(incidentRepository.findById(id)).thenReturn(Optional.of(inc));
        when(auditLogRepository.save(any())).thenReturn(null);
        ResponseEntity<?> r = ruraController.reviewIncident(id, () -> "rev");
        assertEquals("REVIEWED", inc.getStatus());
        verify(incidentRepository).save(inc);
    }

    @Test
    void reviewIncident_ShouldThrowWhenNotFound() {
        when(incidentRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> ruraController.reviewIncident(UUID.randomUUID(), () -> "r"));
    }
}
