package com.blaise.SPTTS.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue
    private UUID incidentId;
    private String description;
    private UUID tripId;
    private UUID busId;
    private String operatorId;
    private String passengerId;
    private String type;
    private String location;
    private String status = "PENDING";
    private java.time.LocalDateTime reportedAt = java.time.LocalDateTime.now();
}