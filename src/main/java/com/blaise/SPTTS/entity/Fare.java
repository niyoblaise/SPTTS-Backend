package com.blaise.SPTTS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID fareId;

    private String routeName;
    private UUID routeId;
    private double amount;
    private LocalDate effectiveDate;
}