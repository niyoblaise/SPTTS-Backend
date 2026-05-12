package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.FareDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.Fare;
import com.blaise.SPTTS.exception.ResourceNotFoundException;
import com.blaise.SPTTS.repository.FareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FareService {

    private final FareRepository repo;
    private final TransportMapper mapper;

    public FareDto create(FareDto fareDto) {
        Fare fare = mapper.toEntity(fareDto);
        if (fare.getEffectiveDate() == null) {
            fare.setEffectiveDate(LocalDate.now());
        }
        return mapper.toDto(repo.save(fare));
    }

    public FareDto findById(UUID fareId) {
        return repo.findById(fareId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found"));
    }

    public FareDto update(UUID fareId, FareDto fareDto) {
        Fare fare = repo.findById(fareId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found"));
        fare.setRouteName(fareDto.routeName());
        fare.setRouteId(fareDto.routeId());
        fare.setAmount(fareDto.amount());
        fare.setEffectiveDate(fareDto.effectiveDate());
        return mapper.toDto(repo.save(fare));
    }

    public void delete(UUID fareId) {
        repo.deleteById(fareId);
    }

    public List<FareDto> list() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    public FareDto currentFare(UUID routeId) {
        return repo.findTopByRouteIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(routeId, LocalDate.now())
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("No fare defined for this route"));
    }
}