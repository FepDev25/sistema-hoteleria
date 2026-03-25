package com.hoteleria.backend.reservation.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.domain.HotelServiceEntity;
import com.hoteleria.backend.catalog.repository.HotelServiceRepository;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.common.exception.NotFoundException;
import com.hoteleria.backend.reservation.api.AddReservationServiceRequest;
import com.hoteleria.backend.reservation.api.ReservationServiceResponse;
import com.hoteleria.backend.reservation.domain.Reservation;
import com.hoteleria.backend.reservation.domain.ReservationServiceEntity;
import com.hoteleria.backend.reservation.domain.ReservationStatus;
import com.hoteleria.backend.reservation.repository.ReservationRepository;
import com.hoteleria.backend.reservation.repository.ReservationServiceRepository;

import jakarta.persistence.EntityManager;

// servicio para agregar consumos a una reserva CHECKED_IN
@Service
public class ReservationConsumptionService {

    private final ReservationRepository reservationRepository;
    private final ReservationServiceRepository reservationServiceRepository;
    private final HotelServiceRepository hotelServiceRepository;
    private final EntityManager entityManager;

    public ReservationConsumptionService(
            ReservationRepository reservationRepository,
            ReservationServiceRepository reservationServiceRepository,
            HotelServiceRepository hotelServiceRepository,
            EntityManager entityManager) {
        this.reservationRepository = reservationRepository;
        this.reservationServiceRepository = reservationServiceRepository;
        this.hotelServiceRepository = hotelServiceRepository;
        this.entityManager = entityManager;
    }

    // agregar un servicio a una reserva CHECKED_IN
    @Transactional
    public ReservationServiceResponse addService(UUID reservationId, AddReservationServiceRequest request) {
        // encontrar la reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada"));

        // validar que esté CHECKED_IN
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessException(
                    "RESERVATION_NOT_CHECKED_IN",
                    "Solo reservas CHECKED_IN pueden registrar consumos",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        // validar que el servicio exista y pertenezca al mismo hotel
        HotelServiceEntity service = hotelServiceRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("SERVICE_NOT_FOUND", "Servicio no encontrado"));

        if (!service.getHotelId().equals(reservation.getHotelId())) {
            throw new BusinessException(
                    "SERVICE_HOTEL_MISMATCH",
                    "El servicio no pertenece al hotel de la reserva",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        // validar que el servicio esté activo
        if (Boolean.FALSE.equals(service.getIsActive())) {
            throw new BusinessException(
                    "SERVICE_INACTIVE",
                    "No se puede agregar un servicio inactivo a nuevas reservas",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        ReservationServiceEntity consumption = new ReservationServiceEntity();
        consumption.setReservationId(reservationId);
        consumption.setServiceId(service.getId());
        consumption.setQuantity(request.quantity());
        consumption.setUnitPriceSnapshot(service.getUnitPrice());

        ReservationServiceEntity saved = reservationServiceRepository.saveAndFlush(consumption);
        entityManager.refresh(saved);

        // retornar el consumo agregado
        return new ReservationServiceResponse(
                saved.getId(),
                saved.getReservationId(),
                saved.getServiceId(),
                saved.getQuantity(),
                saved.getUnitPriceSnapshot(),
                saved.getRequestedAt()
        );
    }
}
