package com.hoteleria.backend.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hoteleria.backend.reservation.domain.ReservationRoom;

public interface ReservationRoomRepository extends JpaRepository<ReservationRoom, UUID> {

    List<ReservationRoom> findByReservationId(UUID reservationId);

    void deleteByReservationId(UUID reservationId);

    @Query(value = """
            SELECT DISTINCT rr.room_id
            FROM reservation_rooms rr
            JOIN reservations r ON r.id = rr.reservation_id
            WHERE r.hotel_id = :hotelId
              AND r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
              AND r.check_in_date < :checkOutDate
              AND r.check_out_date > :checkInDate
            """, nativeQuery = true)
    List<UUID> findConflictingRoomIds(
            @Param("hotelId") UUID hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query(value = """
            SELECT DISTINCT rr.reservation_id
            FROM reservation_rooms rr
            JOIN reservations r ON r.id = rr.reservation_id
            WHERE rr.room_id = :roomId
              AND r.status IN ('PENDING', 'CONFIRMED')
              AND r.check_in_date >= :fromDate
            """, nativeQuery = true)
    List<UUID> findFuturePendingOrConfirmedReservationIdsByRoomId(
            @Param("roomId") UUID roomId,
            @Param("fromDate") LocalDate fromDate
    );
}
