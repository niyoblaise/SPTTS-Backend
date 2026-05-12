package com.blaise.SPTTS.entity;

import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID tripId;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private User passenger;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @Column(name = "rating")
    private Integer rating;

    private Double fare;

    @Column(name = "is_paid")
    private Boolean isPaid = false;
}