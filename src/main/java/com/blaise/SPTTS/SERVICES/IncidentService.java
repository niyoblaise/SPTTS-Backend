package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.IncidentDto;
import com.blaise.SPTTS.entity.Incident;
import com.blaise.SPTTS.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository repo;

    public void submit(IncidentDto dto, String operatorEmail) {
        Incident inc = Incident.builder()
                .description(dto.description())
                .tripId(dto.tripId())
                .busId(dto.busId())
                .operatorId(operatorEmail)
                .type(dto.type())
                .location(dto.location())
                .build();
        repo.save(inc);
    }

    public void submitPassenger(IncidentDto dto, String passengerEmail) {
        Incident inc = Incident.builder()
                .description(dto.description())
                .tripId(dto.tripId())
                .busId(dto.busId())
                .passengerId(passengerEmail)
                .type(dto.type())
                .location(dto.location())
                .build();
        repo.save(inc);
    }
}