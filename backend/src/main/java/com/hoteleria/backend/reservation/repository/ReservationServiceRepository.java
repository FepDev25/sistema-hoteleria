package com.hoteleria.backend.reservation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hoteleria.backend.reservation.domain.ReservationServiceEntity;

public interface ReservationServiceRepository extends JpaRepository<ReservationServiceEntity, UUID> {

    List<ReservationServiceEntity> findByReservationId(UUID reservationId);
}
