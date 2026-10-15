package com.staybook.api.repository;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.Reservation;
import com.staybook.api.entity.ReservationStatus;
import com.staybook.api.entity.ReservationType;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :reservationId")
    Optional<Reservation> findByIdForUpdate(
            @Param("reservationId") Long reservationId
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.room.id = :roomId
        AND r.type = :type
        AND r.status <> :cancelledStatus
        AND r.checkIn < :checkOut
        AND r.checkOut > :checkIn
    """)
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("type") ReservationType type,
            @Param("cancelledStatus") ReservationStatus cancelledStatus,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
