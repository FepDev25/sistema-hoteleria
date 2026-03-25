package com.hoteleria.backend.reservation.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.common.exception.NotFoundException;
import com.hoteleria.backend.reservation.api.GuestRequest;
import com.hoteleria.backend.reservation.api.GuestResponse;
import com.hoteleria.backend.reservation.domain.Guest;
import com.hoteleria.backend.reservation.repository.GuestRepository;

import jakarta.persistence.EntityManager;

// servicio para gestion de huespedes
@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final EntityManager entityManager;

    public GuestService(GuestRepository guestRepository, EntityManager entityManager) {
        this.guestRepository = guestRepository;
        this.entityManager = entityManager;
    }

    // crear un nuevo huesped, validando que el email y documento sean unicos
    @Transactional
    public GuestResponse createGuest(GuestRequest request) {
        if (guestRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("GUEST_EMAIL_ALREADY_EXISTS", "Email de huesped ya registrado", HttpStatus.CONFLICT);
        }

        if (guestRepository.existsByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())) {
            throw new BusinessException("GUEST_DOCUMENT_ALREADY_EXISTS", "Documento de huesped ya registrado", HttpStatus.CONFLICT);
        }

        Guest guest = new Guest();
        guest.setFirstName(request.firstName());
        guest.setLastName(request.lastName());
        guest.setEmail(request.email());
        guest.setPhone(request.phone());
        guest.setDocumentType(request.documentType());
        guest.setDocumentNumber(request.documentNumber());
        guest.setDateOfBirth(request.dateOfBirth());
        guest.setNationality(request.nationality());

        Guest saved = guestRepository.saveAndFlush(guest);
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    // obtener un huesped por id, lanzando excepcion si no se encuentra
    @Transactional(readOnly = true)
    public Guest getByIdOrThrow(UUID guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new NotFoundException("GUEST_NOT_FOUND", "Huesped no encontrado"));
    }

    // convertir entidad Guest a GuestResponse
    private GuestResponse toResponse(Guest guest) {
        return new GuestResponse(
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getEmail(),
                guest.getPhone(),
                guest.getDocumentType(),
                guest.getDocumentNumber(),
                guest.getDateOfBirth(),
                guest.getNationality(),
                guest.getCreatedAt(),
                guest.getUpdatedAt()
        );
    }
}
