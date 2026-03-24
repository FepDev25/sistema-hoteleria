package com.hoteleria.backend.catalog.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Clase que representa la clave compuesta para la relación entre RoomType y Amenity
@Embeddable
public class RoomTypeAmenityId implements Serializable {

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "amenity_id", nullable = false)
    private UUID amenityId;

    public RoomTypeAmenityId() {
    }

    public RoomTypeAmenityId(UUID roomTypeId, UUID amenityId) {
        this.roomTypeId = roomTypeId;
        this.amenityId = amenityId;
    }

    public UUID getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(UUID roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public UUID getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(UUID amenityId) {
        this.amenityId = amenityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoomTypeAmenityId that)) {
            return false;
        }
        return Objects.equals(roomTypeId, that.roomTypeId)
                && Objects.equals(amenityId, that.amenityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTypeId, amenityId);
    }
}
