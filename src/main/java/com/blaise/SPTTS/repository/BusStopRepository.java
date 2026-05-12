package com.blaise.SPTTS.repository;


import com.blaise.SPTTS.entity.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface BusStopRepository extends JpaRepository<BusStop, UUID> {
    List<BusStop> findByRoute_RouteIdOrderByStopOrder(UUID routeId);
}