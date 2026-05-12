package com.blaise.SPTTS.entity;



import com.blaise.SPTTS.entity.ENUMS.NotificationType;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID notificationId;

    private String passengerId;
    private String operatorId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;
    @Builder.Default
    private boolean read = false;
    @Builder.Default
    private Instant timestamp = Instant.now();
}