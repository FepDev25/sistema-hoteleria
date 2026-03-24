package com.hoteleria.backend.catalog.repository;

import com.hoteleria.backend.catalog.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
}
