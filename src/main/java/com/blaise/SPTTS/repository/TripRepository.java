package com.blaise.SPTTS.repository;

import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import com.blaise.SPTTS.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByPassenger_UserIdAndStatus(UUID passengerId, TripStatus status);

    List<Trip> findByRoute_RouteIdAndStatus(UUID routeId, TripStatus status);

    List<Trip> findByBus_Operator_UserId(UUID operatorId);

    List<Trip> findByBus_Operator_Email(String email);

    List<Trip> findByStartTimeBetween(java.time.LocalDate start,
            java.time.LocalDate end);

    List<Trip> findByPassenger_Email(String email);
}