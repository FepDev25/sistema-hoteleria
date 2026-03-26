package com.hoteleria.backend.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hoteleria.backend.billing.domain.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // verificar si existe factura por su reservacion
    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);

    // query personal para obtener hotel id y guest id a partir del invoice id
    @Query("""
            select r.hotelId
            from Invoice i
            join Reservation r on r.id = i.reservationId
            where i.id = :invoiceId
            """)
    Optional<UUID> findHotelIdByInvoiceId(UUID invoiceId);

    @Query("""
            select r.guestId
            from Invoice i
            join Reservation r on r.id = i.reservationId
            where i.id = :invoiceId
            """)
    Optional<UUID> findGuestIdByInvoiceId(UUID invoiceId);
}
