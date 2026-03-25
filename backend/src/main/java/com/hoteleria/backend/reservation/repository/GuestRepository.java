package com.hoteleria.backend.reservation.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hoteleria.backend.reservation.domain.DocumentType;
import com.hoteleria.backend.reservation.domain.Guest;

public interface GuestRepository extends JpaRepository<Guest, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
