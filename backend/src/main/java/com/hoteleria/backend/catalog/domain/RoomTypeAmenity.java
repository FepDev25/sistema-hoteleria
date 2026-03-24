package com.hoteleria.backend.catalog.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// Entidad que representa la relación entre RoomType y Amenity
@Entity
@Table(name = "room_type_amenities")
public class RoomTypeAmenity {

    @EmbeddedId
    private RoomTypeAmenityId id;

    public RoomTypeAmenityId getId() {
        return id;
    }

    public void setId(RoomTypeAmenityId id) {
        this.id = id;
    }
}
