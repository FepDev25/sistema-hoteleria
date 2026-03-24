package com.hoteleria.backend.catalog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.HotelRequest;
import com.hoteleria.backend.catalog.api.HotelResponse;
import com.hoteleria.backend.catalog.domain.Hotel;
import com.hoteleria.backend.catalog.repository.HotelRepository;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.common.exception.NotFoundException;

import jakarta.persistence.EntityManager;

// servicio para manejar la logica de negocio relacionada con los hoteles
@Service
public class HotelCatalogService {

    private final HotelRepository hotelRepository;
    private final EntityManager entityManager;

    public HotelCatalogService(HotelRepository hotelRepository, EntityManager entityManager) {
        this.hotelRepository = hotelRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> getHotels() {
        return hotelRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public HotelResponse createHotel(HotelRequest request) {
        // validar rating entre 1 y 5
        if (request.starRating() < 1 || request.starRating() > 5) {
            throw new BusinessException(
                    "HOTEL_STAR_RATING_INVALID",
                    "La clasificacion del hotel debe estar entre 1 y 5",
                    HttpStatus.BAD_REQUEST
            );
        }

        Hotel hotel = new Hotel();
        hotel.setName(request.name());
        hotel.setAddress(request.address());
        hotel.setCity(request.city());
        hotel.setCountry(request.country());
        hotel.setPhone(request.phone());
        hotel.setEmail(request.email());
        hotel.setStarRating(request.starRating().shortValue());
        hotel.setDescription(request.description());

        Hotel saved = hotelRepository.saveAndFlush(hotel);
        entityManager.refresh(saved);
        Hotel reloaded = hotelRepository.findById(saved.getId())
                .orElseThrow(() -> new NotFoundException("HOTEL_NOT_FOUND", "Hotel no encontrado"));
        return toResponse(reloaded);
    }

    @Transactional(readOnly = true)
    public Hotel getByIdOrThrow(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("HOTEL_NOT_FOUND", "Hotel no encontrado"));
    }

    // metodo para mapear la entidad Hotel a HotelResponse
    private HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getPhone(),
                hotel.getEmail(),
                Integer.valueOf(hotel.getStarRating()),
                hotel.getDescription(),
                hotel.getCreatedAt(),
                hotel.getUpdatedAt(),
                hotel.getDeletedAt()
        );
    }
}
