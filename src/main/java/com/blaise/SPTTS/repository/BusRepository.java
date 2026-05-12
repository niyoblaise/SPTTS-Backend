package com.blaise.SPTTS.repository;

import com.blaise.SPTTS.entity.Bus;
import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {
    List<Bus> findByOperator_UserId(UUID operatorId);

    List<Bus> findByOperator_Email(String email);

    List<Bus> findByRoute_RouteId(UUID routeId);

    long countByStatus(BusStatus status);
}