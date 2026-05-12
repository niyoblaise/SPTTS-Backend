package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.PaymentDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.ENUMS.PaymentStatus;
import com.blaise.SPTTS.entity.ENUMS.TripStatus;
import com.blaise.SPTTS.entity.Payment;
import com.blaise.SPTTS.entity.Trip;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.exception.ResourceNotFoundException;
import com.blaise.SPTTS.repository.PaymentRepository;
import com.blaise.SPTTS.repository.TripRepository;
import com.blaise.SPTTS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository repo;
    @Mock private UserRepository userRepo;
    @Mock private TripRepository tripRepo;
    @Mock private TransportMapper mapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void process_ShouldCreateCompletedPayment() {
        UUID pid = UUID.randomUUID(), tid = UUID.randomUUID();
        when(userRepo.getReferenceById(pid)).thenReturn(new User());
        when(tripRepo.getReferenceById(tid)).thenReturn(new Trip());
        when(repo.save(any())).thenReturn(Payment.builder().paymentId(UUID.randomUUID()).amount(50.0).status(PaymentStatus.COMPLETED).build());
        when(mapper.toDto(any(Payment.class))).thenReturn(new PaymentDto(UUID.randomUUID(), pid, tid, 50.0, PaymentStatus.COMPLETED));

        PaymentDto result = paymentService.process(pid, tid, 50.0);

        assertEquals(50.0, result.amount());
        assertEquals(PaymentStatus.COMPLETED, result.status());
        verify(repo).save(any());
    }


    @Test
    void create_ShouldSaveAndReturnDto() {
        PaymentDto input = new PaymentDto(null, UUID.randomUUID(), UUID.randomUUID(), 30.0, PaymentStatus.PENDING);
        Payment entity = Payment.builder().paymentId(UUID.randomUUID()).amount(30.0).status(PaymentStatus.PENDING).build();
        when(mapper.toEntity(input)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(new PaymentDto(entity.getPaymentId(), null, null, 30.0, PaymentStatus.PENDING));

        PaymentDto result = paymentService.create(input);

        assertEquals(30.0, result.amount());
        assertEquals(PaymentStatus.PENDING, result.status());
    }

    @Test
    void findById_ShouldReturnPayment_WhenFound() {
        UUID id = UUID.randomUUID();
        Payment payment = Payment.builder().paymentId(id).amount(25.0).status(PaymentStatus.COMPLETED).build();
        when(repo.findById(id)).thenReturn(Optional.of(payment));
        when(mapper.toDto(payment)).thenReturn(new PaymentDto(id, null, null, 25.0, PaymentStatus.COMPLETED));

        PaymentDto result = paymentService.findById(id);

        assertEquals(25.0, result.amount());
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> paymentService.findById(UUID.randomUUID()));
    }

    @Test
    void update_ShouldModifyAmountAndStatus() {
        UUID id = UUID.randomUUID();
        Payment existing = Payment.builder().paymentId(id).amount(10.0).status(PaymentStatus.PENDING).build();
        PaymentDto input = new PaymentDto(id, null, null, 99.0, PaymentStatus.COMPLETED);

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new PaymentDto(id, null, null, 99.0, PaymentStatus.COMPLETED));

        PaymentDto result = paymentService.update(id, input);

        assertEquals(99.0, result.amount());
        assertEquals(PaymentStatus.COMPLETED, result.status());
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
            paymentService.update(UUID.randomUUID(), new PaymentDto(null, null, null, 0, PaymentStatus.PENDING)));
    }

    @Test
    void list_ShouldReturnAllPayments() {
        when(repo.findAll()).thenReturn(List.of(
            Payment.builder().paymentId(UUID.randomUUID()).amount(10.0).build(),
            Payment.builder().paymentId(UUID.randomUUID()).amount(20.0).build()
        ));
        when(mapper.toDto(any(Payment.class))).thenAnswer(i ->
            new PaymentDto(((Payment) i.getArgument(0)).getPaymentId(), null, null, ((Payment) i.getArgument(0)).getAmount(), PaymentStatus.COMPLETED));

        List<PaymentDto> result = paymentService.list();

        assertEquals(2, result.size());
    }

    @Test
    void delete_ShouldRemovePayment() {
        UUID id = UUID.randomUUID();
        paymentService.delete(id);
        verify(repo).deleteById(id);
    }

    @Test
    void processForTrip_ShouldCompletePayment_WhenValid() {
        UUID tripId = UUID.randomUUID();
        User passenger = User.builder().userId(UUID.randomUUID()).email("p@x.com").build();
        Trip trip = Trip.builder().tripId(tripId).passenger(passenger).fare(100.0).status(TripStatus.CONFIRMATION).isPaid(false).build();
        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));
        when(repo.save(any())).thenReturn(Payment.builder().amount(100.0).status(PaymentStatus.COMPLETED).build());
        when(mapper.toDto(any(Payment.class))).thenReturn(new PaymentDto(UUID.randomUUID(), passenger.getUserId(), tripId, 100.0, PaymentStatus.COMPLETED));

        PaymentDto result = paymentService.processForTrip(tripId, "p@x.com");

        assertEquals(100.0, result.amount());
        assertEquals(PaymentStatus.COMPLETED, result.status());
        assertTrue(trip.getIsPaid());
        assertEquals(TripStatus.TRIP_COMPLETED, trip.getStatus());
        verify(notificationService).notifyPassenger(anyString(), any(), anyString());
    }

    @Test
    void processForTrip_ShouldThrow_WhenWrongPassenger() {
        Trip trip = Trip.builder().passenger(User.builder().email("other@x.com").build()).build();
        when(tripRepo.findById(any())).thenReturn(Optional.of(trip));
        assertThrows(IllegalArgumentException.class, () -> paymentService.processForTrip(UUID.randomUUID(), "me@x.com"));
    }

    @Test
    void processForTrip_ShouldThrow_WhenTripCancelled() {
        Trip trip = Trip.builder().passenger(User.builder().email("p@x.com").build()).status(TripStatus.CANCELLED).build();
        when(tripRepo.findById(any())).thenReturn(Optional.of(trip));
        assertThrows(IllegalArgumentException.class, () -> paymentService.processForTrip(UUID.randomUUID(), "p@x.com"));
    }

    @Test
    void processForTrip_ShouldThrow_WhenAlreadyPaid() {
        Trip trip = Trip.builder().passenger(User.builder().email("p@x.com").build()).status(TripStatus.CONFIRMATION).isPaid(true).build();
        when(tripRepo.findById(any())).thenReturn(Optional.of(trip));
        assertThrows(IllegalArgumentException.class, () -> paymentService.processForTrip(UUID.randomUUID(), "p@x.com"));
    }
}
