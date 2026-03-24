package com.hoteleria.backend.catalog.repository;

import com.hoteleria.backend.catalog.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> {

    List<RoomType> findByHotelId(UUID hotelId);
}
