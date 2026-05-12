package com.blaise.SPTTS.entity;


import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import jakarta.persistence.*;
import lombok.*;


import java.util.UUID;

@Entity
@Table(name = "buses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID busId;

    @Column(unique = true)
    private String licensePlate;

    private int capacity;

    @Enumerated(EnumType.STRING)
    private BusStatus status;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
}