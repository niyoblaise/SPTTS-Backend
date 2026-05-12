package com.blaise.SPTTS.repository;

import com.blaise.SPTTS.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByPassengerIdAndReadOrderByTimestampDesc(String passengerId, boolean read);

    List<Notification> findByOperatorIdAndReadOrderByTimestampDesc(String operatorId, boolean read);
}