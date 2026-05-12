package com.blaise.SPTTS.entity;

import com.blaise.SPTTS.entity.ENUMS.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;


import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private User passenger;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}