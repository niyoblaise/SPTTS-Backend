package com.blaise.SPTTS.repository;


import com.blaise.SPTTS.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.UUID;

public interface BusLocationRepository extends JpaRepository<BusLocation, UUID> {
    Optional<BusLocation> findTopByBus_BusIdOrderByRecordedAtDesc(UUID busId);

    @Query("select distinct l.bus.busId from BusLocation l where l.recordedAt between ?1 and ?2")
    java.util.Set<UUID> findDistinctBusIdsByRecordedAtBetween(java.time.Instant start,
                                                              java.time.Instant end);
}