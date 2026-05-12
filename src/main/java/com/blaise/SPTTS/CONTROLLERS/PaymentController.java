package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.DTOs.PaymentDto;
import com.blaise.SPTTS.SERVICES.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public PaymentDto create(@Valid @RequestBody PaymentDto paymentDto) {
        return paymentService.create(paymentDto);
    }

    @GetMapping("/{paymentId}")
    public PaymentDto findById(@PathVariable UUID paymentId) {
        return paymentService.findById(paymentId);
    }

    @PutMapping("/{paymentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public PaymentDto update(@PathVariable UUID paymentId, @Valid @RequestBody PaymentDto paymentDto) {
        return paymentService.update(paymentId, paymentDto);
    }

    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID paymentId) {
        paymentService.delete(paymentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<PaymentDto> list() {
        return paymentService.list();
    }
}