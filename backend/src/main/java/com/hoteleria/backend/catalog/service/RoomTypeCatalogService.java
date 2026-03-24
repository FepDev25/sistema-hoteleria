package com.hoteleria.backend.catalog.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.RoomTypeCreateRequest;
import com.hoteleria.backend.catalog.api.RoomTypeResponse;
import com.hoteleria.backend.catalog.domain.RoomType;
import com.hoteleria.backend.catalog.domain.RoomTypeAmenity;
import com.hoteleria.backend.catalog.domain.RoomTypeAmenityId;
import com.hoteleria.backend.catalog.repository.AmenityRepository;
import com.hoteleria.backend.catalog.repository.RoomTypeAmenityRepository;
import com.hoteleria.backend.catalog.repository.RoomTypeRepository;
import com.hoteleria.backend.common.exception.BusinessException;

import jakarta.persistence.EntityManager;

// servicio de tipos de habitacion
@Service
public class RoomTypeCatalogService {

    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RoomTypeAmenityRepository roomTypeAmenityRepository;
    private final HotelCatalogService hotelCatalogService;
    private final EntityManager entityManager;

    public RoomTypeCatalogService(
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            RoomTypeAmenityRepository roomTypeAmenityRepository,
            HotelCatalogService hotelCatalogService,
            EntityManager entityManager) {
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.roomTypeAmenityRepository = roomTypeAmenityRepository;
        this.hotelCatalogService = hotelCatalogService;
        this.entityManager = entityManager;
    }

    // obtener tipos de habitacion, opcionalmente filtrados por hotel
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getRoomTypes(UUID hotelId) {
        List<RoomType> roomTypes = hotelId == null
                ? roomTypeRepository.findAll()
                : roomTypeRepository.findByHotelId(hotelId);

        return mapWithAmenities(roomTypes);
    }

    // crear tipo de habitacion
    @Transactional
    public RoomTypeResponse createRoomType(RoomTypeCreateRequest request) {
        hotelCatalogService.getByIdOrThrow(request.hotelId());

        if (request.maxCapacity() < 1) {
            // validar que la capacidad maxima sea al menos 1
            throw new BusinessException(
                    "ROOM_TYPE_CAPACITY_INVALID",
                    "La capacidad maxima debe ser mayor a 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        RoomType entity = new RoomType();
        entity.setHotelId(request.hotelId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setMaxCapacity(request.maxCapacity());
        entity.setBasePricePerNight(request.basePricePerNight());

        RoomType saved = roomTypeRepository.saveAndFlush(entity);
        entityManager.refresh(saved);
        syncAmenities(saved.getId(), request.amenityIds());

        return mapWithAmenities(List.of(saved)).getFirst();
    }

    // sincronizar las comodidades asociadas a un tipo de habitacion
    // es decir, eliminar las existentes y crear las nuevas segun la lista de ids proporcionada
    private void syncAmenities(UUID roomTypeId, List<UUID> amenityIds) {

        // eliminar los enlaces existentes entre el tipo de habitacion y las comodidades
        roomTypeAmenityRepository.deleteByIdRoomTypeId(roomTypeId);

        if (amenityIds == null || amenityIds.isEmpty()) {
            return;
        }

        // validar que todas las comodidades existan 
        Set<UUID> uniqueAmenityIds = Set.copyOf(amenityIds);
        long existingCount = amenityRepository.countByIdIn(uniqueAmenityIds);
        if (existingCount != uniqueAmenityIds.size()) {
            throw new BusinessException(
                    "AMENITY_NOT_FOUND",
                    "Una o mas comodidades no existen",
                    HttpStatus.NOT_FOUND
            );
        }

        // crear los enlaces entre el tipo de habitacion y las comodidades
        List<RoomTypeAmenity> links = uniqueAmenityIds.stream().map(amenityId -> {
            RoomTypeAmenity link = new RoomTypeAmenity();
            link.setId(new RoomTypeAmenityId(roomTypeId, amenityId));
            return link;
        }).toList();

        roomTypeAmenityRepository.saveAll(links);
    }

    // mapear entidades de tipo de habitacion a respuestas, incluyendo las comodidades asociadas
    private List<RoomTypeResponse> mapWithAmenities(List<RoomType> roomTypes) {
        if (roomTypes.isEmpty()) {
            return List.of();
        }

        // ids de los tipos de habitacion
        List<UUID> roomTypeIds = roomTypes.stream().map(RoomType::getId).toList();
        // una sola consulta para obtener todos los enlaces entre esos tipos de habitacion y sus comodidades
        List<RoomTypeAmenity> links = roomTypeAmenityRepository.findByIdRoomTypeIdIn(roomTypeIds);

        // Key: id de tipo de habitacion, Value: lista de ids de comodidades
        Map<UUID, List<UUID>> amenitiesByRoomType = new HashMap<>();
        for (RoomTypeAmenity link : links) {
            // para cada enlace, agregar la comodidad al tipo de habitacion correspondiente en el mapa
            amenitiesByRoomType
                    .computeIfAbsent(link.getId().getRoomTypeId(), ignored -> new ArrayList<>())
                    .add(link.getId().getAmenityId());
        }

        // mapear cada tipo de habitacion a una respuesta, incluyendo la lista de ids de comodidades asociadas obtenida del mapa
        return roomTypes.stream().map(entity -> new RoomTypeResponse(
                entity.getId(),
                entity.getHotelId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMaxCapacity(),
                entity.getBasePricePerNight(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                amenitiesByRoomType.getOrDefault(entity.getId(), Collections.emptyList())
        )).toList();
    }
}
