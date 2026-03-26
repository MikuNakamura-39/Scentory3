package com.scentory.admin.repository;

import com.scentory.admin.entity.Reservation;
import com.scentory.admin.model.ReservationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
