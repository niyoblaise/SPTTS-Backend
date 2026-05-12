package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.PaymentDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.ENUMS.NotificationType;
import com.blaise.SPTTS.entity.ENUMS.PaymentStatus;
import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import com.blaise.SPTTS.entity.Payment;
import com.blaise.SPTTS.entity.Trip;
import com.blaise.SPTTS.exception.ResourceNotFoundException;
import com.blaise.SPTTS.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.blaise.SPTTS.repository.UserRepository;
import com.blaise.SPTTS.repository.TripRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repo;
    private final UserRepository userRepo;
    private final TripRepository tripRepo;
    private final TransportMapper mapper;
    private final NotificationService notificationService;

    public PaymentDto process(UUID passengerId, UUID tripId, double amount) {
        Payment p = Payment.builder()
                .passenger(userRepo.getReferenceById(passengerId))
                .trip(tripRepo.getReferenceById(tripId))
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .build();
        return mapper.toDto(repo.save(p));
    }

    public PaymentDto create(PaymentDto paymentDto) {
        Payment payment = mapper.toEntity(paymentDto);
        return mapper.toDto(repo.save(payment));
    }

    public PaymentDto findById(UUID paymentId) {
        return repo.findById(paymentId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    public PaymentDto update(UUID paymentId, PaymentDto paymentDto) {
        Payment payment = repo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setAmount(paymentDto.amount());
        payment.setStatus(paymentDto.status());
        return mapper.toDto(repo.save(payment));
    }

    public List<PaymentDto> list() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    public void delete(UUID paymentId) {
        repo.deleteById(paymentId);
    }

    @Transactional
    public PaymentDto processForTrip(UUID tripId, String passengerEmail) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (!trip.getPassenger().getEmail().equals(passengerEmail)) {
            throw new IllegalArgumentException("Not your trip");
        }
        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new IllegalArgumentException("Trip was cancelled");
        }
        if (Boolean.TRUE.equals(trip.getIsPaid())) {
            throw new IllegalArgumentException("Trip has already been paid");
        }

        Payment payment = Payment.builder()
                .passenger(trip.getPassenger())
                .trip(trip)
                .amount(trip.getFare())
                .status(PaymentStatus.COMPLETED)
                .build();

        trip.setStatus(TripStatus.TRIP_COMPLETED);
        trip.setIsPaid(true);
        tripRepo.save(trip);

        // Send payment confirmation notification
        String routeName = trip.getRoute() != null ? trip.getRoute().getRouteName() : "your trip";
        String message = String.format("Payment of %.0f RWF completed for trip to %s",
                trip.getFare(), routeName);
        notificationService.notifyPassenger(
                trip.getPassenger().getUserId().toString(),
                NotificationType.PAYMENT_CONFIRMED,
                message);

        return mapper.toDto(repo.save(payment));
    }
}