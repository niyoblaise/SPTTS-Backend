package com.blaise.SPTTS.repository;

import com.blaise.SPTTS.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FareRepository extends JpaRepository<Fare, UUID> {
    Optional<Fare> findTopByRouteNameAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(String routeName,
            LocalDate date);

    Optional<Fare> findTopByRouteIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(UUID routeId, LocalDate date);
}