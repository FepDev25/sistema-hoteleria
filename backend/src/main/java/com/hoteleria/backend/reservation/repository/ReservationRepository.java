package com.hoteleria.backend.reservation.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hoteleria.backend.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
            select r.hotelId
            from Reservation r
            where r.id = :reservationId
            """)
    Optional<UUID> findHotelIdByReservationId(UUID reservationId);

    @Query("""
            select r.guestId
            from Reservation r
            where r.id = :reservationId
            """)
    Optional<UUID> findGuestIdByReservationId(UUID reservationId);
}
