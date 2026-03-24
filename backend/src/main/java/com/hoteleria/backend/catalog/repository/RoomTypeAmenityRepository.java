package com.hoteleria.backend.catalog.repository;

import com.hoteleria.backend.catalog.domain.RoomTypeAmenity;
import com.hoteleria.backend.catalog.domain.RoomTypeAmenityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoomTypeAmenityRepository extends JpaRepository<RoomTypeAmenity, RoomTypeAmenityId> {

    void deleteByIdRoomTypeId(UUID roomTypeId);

    List<RoomTypeAmenity> findByIdRoomTypeIdIn(Collection<UUID> roomTypeIds);
}
