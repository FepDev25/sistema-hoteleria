package com.hoteleria.backend.catalog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.catalog.api.ServiceCreateRequest;
import com.hoteleria.backend.catalog.api.ServiceResponse;
import com.hoteleria.backend.catalog.domain.HotelServiceEntity;
import com.hoteleria.backend.catalog.repository.HotelServiceRepository;

import jakarta.persistence.EntityManager;

// servicio de catalogo de servicios
@Service
public class ServiceCatalogService {

    private final HotelServiceRepository hotelServiceRepository;
    private final HotelCatalogService hotelCatalogService;
    private final EntityManager entityManager;

    public ServiceCatalogService(
            HotelServiceRepository hotelServiceRepository,
            HotelCatalogService hotelCatalogService,
            EntityManager entityManager) {
        this.hotelServiceRepository = hotelServiceRepository;
        this.hotelCatalogService = hotelCatalogService;
        this.entityManager = entityManager;
    }

    // obtiene los servicios de un hotel, si el hotelId es null, obtiene todos los servicios
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServices(UUID hotelId) {
        List<HotelServiceEntity> services = hotelId == null
                ? hotelServiceRepository.findAll()
                : hotelServiceRepository.findByHotelId(hotelId);
        return services.stream().map(this::toResponse).toList();
    }

    // crea un nuevo servicio para un hotel, valida que el hotel exista antes de crear el servicio
    @Transactional
    public ServiceResponse createService(ServiceCreateRequest request) {
        hotelCatalogService.getByIdOrThrow(request.hotelId());

        HotelServiceEntity entity = new HotelServiceEntity();
        entity.setHotelId(request.hotelId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setUnitPrice(request.unitPrice());
        entity.setCategory(request.category());
        entity.setIsActive(request.isActive());

        HotelServiceEntity saved = hotelServiceRepository.saveAndFlush(entity);
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    // convierte una entidad de servicio a una respuesta de servicio
    private ServiceResponse toResponse(HotelServiceEntity entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getHotelId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUnitPrice(),
                entity.getCategory(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
