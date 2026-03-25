package com.hoteleria.backend.reservation.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// entidad que representa la relación entre una reserva y una habitación
@Entity
@Table(name = "reservation_rooms")
public class ReservationRoom {

    @Id
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private Short adults;

    @Column(nullable = false)
    private Short children;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (adults == null) {
            adults = 1;
        }
        if (children == null) {
            children = 0;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Short getAdults() {
        return adults;
    }

    public void setAdults(Short adults) {
        this.adults = adults;
    }

    public Short getChildren() {
        return children;
    }

    public void setChildren(Short children) {
        this.children = children;
    }
}
