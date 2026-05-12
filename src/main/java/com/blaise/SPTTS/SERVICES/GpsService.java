package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.BusLocationDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.Bus;
import com.blaise.SPTTS.entity.ENUMS.BusStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.blaise.SPTTS.repository.BusLocationRepository;
import com.blaise.SPTTS.repository.BusRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GpsService {

    private final BusLocationRepository repo;
    private final BusRepository busRepo;
    private final TransportMapper mapper;


    @Transactional
    public void updateBusStatus(UUID busId, BusStatus status, String operatorEmail) {
        Bus bus = busRepo.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found"));
        if (!bus.getOperator().getEmail().equals(operatorEmail)) {
            throw new IllegalArgumentException("Not your bus");
        }
        bus.setStatus(status);
        busRepo.save(bus);
    }

    public void save(BusLocationDto dto) {
        var bus = busRepo.findById(dto.busId()).orElseThrow(() -> new IllegalArgumentException("Bus not found"));
        var loc = mapper.toEntity(dto);
        loc.setBus(bus);
        repo.save(loc);
    }

    public BusLocationDto getLatest(UUID busId) {
        return repo.findTopByBus_BusIdOrderByRecordedAtDesc(busId)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("No location"));
    }

    public List<BusLocationDto> getHistory(UUID busId) {
        return repo.findAll().stream()
                .filter(l -> l.getBus().getBusId().equals(busId))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}