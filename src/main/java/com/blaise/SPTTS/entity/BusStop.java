package com.blaise.SPTTS.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "bus_stops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID stopId;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    private String name;
    private double latitude;
    private double longitude;
    private int stopOrder;
}