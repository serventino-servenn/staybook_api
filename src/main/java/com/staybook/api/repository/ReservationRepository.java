package com.staybook.api.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
}
