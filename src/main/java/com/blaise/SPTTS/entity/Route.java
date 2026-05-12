package com.blaise.SPTTS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID routeId;

    private String routeName;
    private String description;
    private String startStop;
    private String destinationStop;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<BusStop> stops;

    private Double endStopLatitude;
    private Double endStopLongitude;

    @Column(nullable = false, columnDefinition = "double precision default 0.0")
    private Double price;
}