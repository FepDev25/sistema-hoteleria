package com.hoteleria.backend.catalog.repository;

import com.hoteleria.backend.catalog.domain.HotelServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelServiceRepository extends JpaRepository<HotelServiceEntity, UUID> {

    List<HotelServiceEntity> findByHotelId(UUID hotelId);
}
