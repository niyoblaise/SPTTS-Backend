package com.blaise.SPTTS.repository;

import com.blaise.SPTTS.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}