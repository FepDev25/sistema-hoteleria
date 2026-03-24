package com.hoteleria.backend.catalog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.RoomCreateRequest;
import com.hoteleria.backend.catalog.api.RoomResponse;
import com.hoteleria.backend.catalog.domain.Room;
import com.hoteleria.backend.catalog.repository.RoomRepository;
import com.hoteleria.backend.catalog.repository.RoomTypeRepository;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.common.exception.NotFoundException;

import jakarta.persistence.EntityManager;

// servicio para manejar las habitaciones de los hoteles
@Service
public class RoomCatalogService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelCatalogService hotelCatalogService;
    private final EntityManager entityManager;

    public RoomCatalogService(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            HotelCatalogService hotelCatalogService,
            EntityManager entityManager) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelCatalogService = hotelCatalogService;
        this.entityManager = entityManager;
    }

    // obtener las habitaciones de un hotel, si no se especifica el hotel, se obtienen todas las habitaciones
    @Transactional(readOnly = true)
    public List<RoomResponse> getRooms(UUID hotelId) {
        List<Room> rooms = hotelId == null ? roomRepository.findAll() : roomRepository.findByHotelId(hotelId);
        return rooms.stream().map(this::toResponse).toList();
    }

    // crear una nueva habitacion para un hotel
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        // validar que el hotel exista
        hotelCatalogService.getByIdOrThrow(request.hotelId());

        // validar que el piso de la habitacion sea mayor o igual a 0
        if (request.floor() < 0) {
            throw new BusinessException(
                    "ROOM_FLOOR_INVALID",
                    "El piso de la habitacion debe ser mayor o igual a 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        // validar que el tipo de habitacion exista y pertenezca al hotel indicado
        var roomType = roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new BusinessException(
                        "ROOM_TYPE_NOT_FOUND",
                        "Tipo de habitacion no encontrado",
                        HttpStatus.NOT_FOUND
                ));

        if (!roomType.getHotelId().equals(request.hotelId())) {
            throw new BusinessException(
                    "ROOM_TYPE_HOTEL_MISMATCH",
                    "El tipo de habitacion no pertenece al hotel indicado",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        Room room = new Room();
        room.setHotelId(request.hotelId());
        room.setRoomTypeId(request.roomTypeId());
        room.setRoomNumber(request.roomNumber());
        room.setFloor(request.floor());

        Room saved = roomRepository.saveAndFlush(room);
        entityManager.refresh(saved);
        Room reloaded = roomRepository.findById(saved.getId())
                .orElseThrow(() -> new NotFoundException("ROOM_NOT_FOUND", "Habitacion no encontrada"));
        return toResponse(reloaded);
    }

    // convertir una entidad de habitacion a una respuesta de habitacion
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
                room.getDeletedAt()
        );
    }
}
