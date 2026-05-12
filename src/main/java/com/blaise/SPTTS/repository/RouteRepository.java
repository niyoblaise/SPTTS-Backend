package com.blaise.SPTTS.repository;


import com.blaise.SPTTS.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {
}