package com.hoteleria.backend.catalog.repository;

import com.hoteleria.backend.catalog.domain.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {

    long countByIdIn(Collection<UUID> ids);
}
