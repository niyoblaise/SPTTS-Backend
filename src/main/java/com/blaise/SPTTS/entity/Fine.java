package com.blaise.SPTTS.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fine {
    @Id
    @GeneratedValue
    private UUID fineId;
    private Double amount;
    private String reason;
    private String issuedTo; // Email or ID of the operator
    private String issuedBy; // Email of the regulator
    private LocalDateTime issuedAt;
}
