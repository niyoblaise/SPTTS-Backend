package com.blaise.SPTTS.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class OperatorComplianceDTO {
    private UUID operatorId;
    private String name;
    private String email;
    private String company;
    private String licenseNumber;
    private int totalIncidents;
    private double complianceScore;
    private String status;
    private java.util.List<String> activePlates;
}
