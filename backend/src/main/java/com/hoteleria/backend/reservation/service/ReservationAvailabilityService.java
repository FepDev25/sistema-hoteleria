package com.hoteleria.backend.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.RoomResponse;
import com.hoteleria.backend.catalog.domain.Room;
import com.hoteleria.backend.catalog.domain.RoomStatus;
import com.hoteleria.backend.catalog.repository.RoomRepository;
import com.hoteleria.backend.catalog.repository.RoomTypeRepository;
import com.hoteleria.backend.catalog.service.HotelCatalogService;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.reservation.repository.ReservationRoomRepository;

// servicio para consultar disponibilidad de habitaciones en un hotel para un rango de fechas y ocupacion dada
@Service
public class ReservationAvailabilityService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final HotelCatalogService hotelCatalogService;

    public ReservationAvailabilityService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository,
            ReservationRoomRepository reservationRoomRepository, HotelCatalogService hotelCatalogService) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.reservationRoomRepository = reservationRoomRepository;
        this.hotelCatalogService = hotelCatalogService;
    }

    // consultar habitaciones disponibles en un hotel para un rango de fechas y ocupacion dada
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(UUID hotelId, LocalDate checkInDate, LocalDate checkOutDate,
            short adults, short children) {

        // validaciones de fecha
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException(
                    "INVALID_RESERVATION_DATES",
                    "La fecha de salida debe ser posterior a la fecha de entrada",
                    HttpStatus.BAD_REQUEST);
        }

        // validacion de ocupacion
        if (adults < 1 || children < 0) {
            throw new BusinessException(
                    "INVALID_OCCUPANCY",
                    "La ocupacion solicitada es invalida",
                    HttpStatus.BAD_REQUEST);
        }

        hotelCatalogService.getByIdOrThrow(hotelId);

        // obtener habitaciones del hotel y filtrar por disponibilidad y capacidad
        List<Room> hotelRooms = roomRepository.findByHotelId(hotelId);
        // obtener habitaciones que ya estan reservadas para el rango de fechas dado
        Set<UUID> conflictingRoomIds = Set.copyOf(
                reservationRoomRepository.findConflictingRoomIds(hotelId, checkInDate, checkOutDate));

        // obtener capacidad maxima de cada tipo de habitacion para filtrar por ocupacion
        Map<UUID, Short> maxCapacityByRoomType = roomTypeRepository.findAllById(
                hotelRooms.stream().map(Room::getRoomTypeId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(roomType -> roomType.getId(),
                        roomType -> roomType.getMaxCapacity()));

        int occupancy = adults + children;

        // filtrar habitaciones por disponibilidad y capacidad
        return hotelRooms.stream()
                .filter(room -> room.getStatus() != RoomStatus.MAINTENANCE&& room.getStatus() != RoomStatus.OUT_OF_SERVICE)
                .filter(room -> !conflictingRoomIds.contains(room.getId()))
                .filter(room -> {
                    Short maxCapacity = maxCapacityByRoomType.get(room.getRoomTypeId());
                    return maxCapacity != null && occupancy <= maxCapacity;
                })
                .map(this::toResponse)
                .toList();
    }

    // convertir entidad Room a RoomResponse
    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomTypeId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getStatus(),
                room.getCreatedAt(),
                room.getUpdatedAt(),
                room.getDeletedAt());
    }
}
