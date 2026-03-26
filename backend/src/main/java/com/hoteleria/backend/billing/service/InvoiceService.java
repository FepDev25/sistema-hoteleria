package com.hoteleria.backend.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoteleria.backend.billing.api.CreateInvoiceRequest;
import com.hoteleria.backend.billing.api.InvoiceResponse;
import com.hoteleria.backend.billing.domain.Invoice;
import com.hoteleria.backend.billing.domain.InvoiceStatus;
import com.hoteleria.backend.billing.repository.InvoiceRepository;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.common.exception.NotFoundException;
import com.hoteleria.backend.reservation.domain.Reservation;
import com.hoteleria.backend.reservation.domain.ReservationRoom;
import com.hoteleria.backend.reservation.domain.ReservationStatus;
import com.hoteleria.backend.reservation.repository.ReservationRepository;
import com.hoteleria.backend.reservation.repository.ReservationRoomRepository;
import com.hoteleria.backend.reservation.repository.ReservationServiceRepository;

import jakarta.persistence.EntityManager;

// servicio para manejar logica de facturas
@Service
public class InvoiceService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.15");

    private final InvoiceRepository invoiceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final ReservationServiceRepository reservationServiceRepository;
    private final EntityManager entityManager;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            ReservationRepository reservationRepository,
            ReservationRoomRepository reservationRoomRepository,
            ReservationServiceRepository reservationServiceRepository,
            EntityManager entityManager) {
        this.invoiceRepository = invoiceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationRoomRepository = reservationRoomRepository;
        this.reservationServiceRepository = reservationServiceRepository;
        this.entityManager = entityManager;
    }

    // metodo para crear una factura a partir de una reserva
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        // obtener la reserva y validar que exista
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new NotFoundException("RESERVATION_NOT_FOUND", "Reserva no encontrada"));

        // validar que la reserva no tenga factura ya creada
        if (invoiceRepository.existsByReservationId(request.reservationId())) {
            throw new BusinessException("INVOICE_ALREADY_EXISTS", "La reserva ya tiene factura", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        // validar que la reserva esté en un estado que permita facturación
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.COMPLETED
                && reservation.getStatus() != ReservationStatus.CANCELLED) {
            throw new BusinessException(
                    "RESERVATION_CANNOT_BE_INVOICED",
                    "Solo reservas CHECKED_IN/COMPLETED/CANCELLED pueden facturarse",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        // calcular subtotal, impuestos y total
        BigDecimal subtotal = calculateSubtotal(reservation.getId(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        BigDecimal taxAmount = subtotal.multiply(DEFAULT_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        // crear la factura en estado DRAFT y guardarla
        Invoice invoice = new Invoice();
        invoice.setReservationId(reservation.getId());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setSubtotal(subtotal);
        invoice.setTaxRate(DEFAULT_TAX_RATE);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);
        invoice.setStatus(InvoiceStatus.DRAFT);

        Invoice saved = invoiceRepository.saveAndFlush(invoice);
        entityManager.refresh(saved);
        return toResponse(saved);
    }

    // metodo para emitir una factura, cambiando su estado a ISSUED
    @Transactional
    public InvoiceResponse issueInvoice(UUID invoiceId) {
        // obtener la factura y validar que exista
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("INVOICE_NOT_FOUND", "Factura no encontrada"));

        // validar que la factura esté en estado DRAFT para poder emitirla
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException(
                    "INVOICE_CANNOT_BE_ISSUED",
                    "Solo facturas DRAFT pueden emitirse",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Invoice saved = invoiceRepository.saveAndFlush(invoice);
        return toResponse(saved);
    }

    // metodo privado para calcular el subtotal de una factura a partir de los detalles de la reserva
    private BigDecimal calculateSubtotal(UUID reservationId, java.time.LocalDate checkInDate, java.time.LocalDate checkOutDate) {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        BigDecimal roomsTotal = reservationRoomRepository.findByReservationId(reservationId).stream()
                .map(ReservationRoom::getPricePerNight)
                .map(price -> price.multiply(BigDecimal.valueOf(nights)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal servicesTotal = reservationServiceRepository.findByReservationId(reservationId).stream()
                .map(service -> service.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(service.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return roomsTotal.add(servicesTotal).setScale(2, RoundingMode.HALF_UP);
    }

    // metodo privado para generar un numero de factura unico
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    // metodo privado para convertir una entidad Invoice a un DTO InvoiceResponse
    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getReservationId(),
                invoice.getInvoiceNumber(),
                invoice.getSubtotal(),
                invoice.getTaxRate(),
                invoice.getTaxAmount(),
                invoice.getTotal(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getPaidAt(),
                invoice.getCreatedAt()
        );
    }
}
