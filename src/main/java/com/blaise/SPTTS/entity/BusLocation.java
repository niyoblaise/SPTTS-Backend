package com.blaise.SPTTS.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bus_locations", indexes = {
        @Index(name = "idx_bus_time", columnList = "bus_id,recorded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusLocation {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID locationId;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    private double latitude;
    private double longitude;
    private double accuracy;
    private Double speed;
    private Instant recordedAt;
    @Builder.Default
    private Instant createdAt = Instant.now();
}