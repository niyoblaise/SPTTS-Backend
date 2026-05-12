package com.blaise.SPTTS.repository;


import com.blaise.SPTTS.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
}