package com.blaise.SPTTS.entity;

import com.blaise.SPTTS.entity.ENUMS.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID userId;

    @Column(unique = true)
    private String email;

    private String fullName;
    private String phone;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String licenseNumber;
    private String company;
    private String authority;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private String accountStatus = "ACTIVE";
}