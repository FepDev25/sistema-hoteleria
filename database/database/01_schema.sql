-- 01_schema.sql — DDL completo del sistema de Hotelería
-- Motor: PostgreSQL 15+
-- Convención: snake_case, tablas en plural, PK = uuid, FK = singular_table_id
-- Auditado por @Auditor — 2026-03-15


-- ENUMS

CREATE TYPE room_status         AS ENUM ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE');
CREATE TYPE reservation_status  AS ENUM ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED');
CREATE TYPE invoice_status      AS ENUM ('DRAFT', 'ISSUED', 'PAID', 'VOID');
CREATE TYPE payment_status      AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');
CREATE TYPE payment_method      AS ENUM ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'ONLINE');
CREATE TYPE document_type       AS ENUM ('PASSPORT', 'DNI', 'NIE', 'DRIVER_LICENSE', 'OTHER');
CREATE TYPE maintenance_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE maintenance_status  AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');


-- FUNCIÓN UTILITARIA: auto-actualización de updated_at
-- sin esto, updated_at nunca se actualiza automáticamente.
-- Se aplica como trigger BEFORE UPDATE en todas las tablas que tienen el campo.

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


-- HOTELS

CREATE TABLE hotels (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(150)  NOT NULL,
    address      TEXT          NOT NULL,
    city         VARCHAR(100)  NOT NULL,
    country      VARCHAR(100)  NOT NULL,
    phone        VARCHAR(30),
    email        VARCHAR(150),
    star_rating  SMALLINT      NOT NULL CHECK (star_rating BETWEEN 1 AND 5),
    description  TEXT,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ
);

COMMENT ON TABLE  hotels             IS 'Propiedades hoteleras gestionadas por el sistema.';
COMMENT ON COLUMN hotels.star_rating IS 'Clasificación oficial del hotel, de 1 a 5 estrellas.';
COMMENT ON COLUMN hotels.deleted_at  IS 'Soft-delete: hotel inactivo sin perder historial de reservas.';

CREATE TRIGGER trg_hotels_updated_at
    BEFORE UPDATE ON hotels
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ROOM_TYPES

CREATE TABLE room_types (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id             UUID          NOT NULL REFERENCES hotels(id) ON DELETE RESTRICT,
    name                 VARCHAR(100)  NOT NULL,
    description          TEXT,
    max_capacity         SMALLINT      NOT NULL CHECK (max_capacity > 0),
    base_price_per_night NUMERIC(10,2) NOT NULL CHECK (base_price_per_night > 0),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ,

    CONSTRAINT uq_room_type_name_per_hotel UNIQUE (hotel_id, name)
);

COMMENT ON TABLE  room_types                      IS 'Categorías de habitación de un hotel (Doble, Suite, etc.).';
COMMENT ON COLUMN room_types.base_price_per_night IS 'Precio base por noche. Sirve de referencia; el precio real se guarda en reservation_rooms.';
COMMENT ON COLUMN room_types.max_capacity         IS 'Número máximo de huéspedes (adultos + niños) para este tipo.';

CREATE TRIGGER trg_room_types_updated_at
    BEFORE UPDATE ON room_types
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ROOMS

CREATE TABLE rooms (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id     UUID        NOT NULL REFERENCES hotels(id)      ON DELETE RESTRICT,
    room_type_id UUID        NOT NULL REFERENCES room_types(id)  ON DELETE RESTRICT,
    room_number  VARCHAR(10) NOT NULL,
    floor        SMALLINT    NOT NULL CHECK (floor >= 0),
    status       room_status NOT NULL DEFAULT 'AVAILABLE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,

    CONSTRAINT uq_room_number_per_hotel UNIQUE (hotel_id, room_number)
);

COMMENT ON TABLE  rooms             IS 'Habitaciones físicas del hotel. El número de habitación es único dentro del hotel.';
COMMENT ON COLUMN rooms.hotel_id    IS 'Denormalización intencional para queries directas sin JOIN a room_types. INVARIANTE: rooms.hotel_id debe ser igual a room_types.hotel_id del room_type_id asociado. La aplicación es responsable de garantizarlo al crear habitaciones.';
COMMENT ON COLUMN rooms.status      IS 'Estado operativo actual: AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_SERVICE.';
COMMENT ON COLUMN rooms.deleted_at  IS 'Soft-delete: la habitación se da de baja lógicamente para conservar historial.';

CREATE TRIGGER trg_rooms_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_rooms_hotel_id     ON rooms(hotel_id);
CREATE INDEX idx_rooms_room_type_id ON rooms(room_type_id);
CREATE INDEX idx_rooms_status       ON rooms(status) WHERE deleted_at IS NULL;


-- AMENITIES

CREATE TABLE amenities (
    id       UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name     VARCHAR(100) NOT NULL UNIQUE,
    icon     VARCHAR(50),
    category VARCHAR(50)  NOT NULL   -- 'COMFORT', 'TECHNOLOGY', 'BATHROOM', 'VIEW', etc.
);

COMMENT ON TABLE  amenities          IS 'Catálogo de comodidades/servicios incluidos en tipos de habitación.';
COMMENT ON COLUMN amenities.icon     IS 'Nombre de icono (ej: "wifi", "tv", "jacuzzi") para uso en el frontend.';
COMMENT ON COLUMN amenities.category IS 'Agrupación para filtrado: COMFORT, TECHNOLOGY, BATHROOM, VIEW, etc.';


-- ROOM_TYPE_AMENITIES (N:M)

CREATE TABLE room_type_amenities (
    room_type_id UUID NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
    amenity_id   UUID NOT NULL REFERENCES amenities(id)  ON DELETE CASCADE,

    PRIMARY KEY (room_type_id, amenity_id)
);

COMMENT ON TABLE room_type_amenities IS 'Relación N:M entre tipos de habitación y sus comodidades incluidas.';

-- la PK cubre (room_type_id, amenity_id) pero no la dirección inversa.
-- Este índice permite la consulta "¿qué tipos de habitación tienen esta amenidad?"
CREATE INDEX idx_room_type_amenities_amenity_id ON room_type_amenities(amenity_id);


-- GUESTS

CREATE TABLE guests (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  NOT NULL UNIQUE,
    phone           VARCHAR(30),
    document_type   document_type NOT NULL,
    document_number VARCHAR(50)   NOT NULL,
    nationality     VARCHAR(100),
    date_of_birth   DATE          NOT NULL CHECK (date_of_birth <= CURRENT_DATE - INTERVAL '18 years'),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,

    CONSTRAINT uq_guest_document UNIQUE (document_type, document_number)
);

COMMENT ON TABLE  guests                IS 'Huéspedes registrados en el sistema.';
COMMENT ON COLUMN guests.email          IS 'Identificador de negocio único. Usado para login y comunicaciones.';
COMMENT ON COLUMN guests.date_of_birth  IS 'La BD rechaza el registro de menores de 18 años.';
COMMENT ON COLUMN guests.document_type  IS 'Tipo de documento: PASSPORT, DNI, NIE, DRIVER_LICENSE, OTHER.';

CREATE TRIGGER trg_guests_updated_at
    BEFORE UPDATE ON guests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_guests_document ON guests(document_type, document_number);


-- RESERVATIONS

CREATE TABLE reservations (
    id                  UUID               PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id            UUID               NOT NULL REFERENCES hotels(id) ON DELETE RESTRICT,
    guest_id            UUID               NOT NULL REFERENCES guests(id) ON DELETE RESTRICT,
    check_in_date       DATE               NOT NULL,
    check_out_date      DATE               NOT NULL,
    status              reservation_status NOT NULL DEFAULT 'PENDING',
    total_amount        NUMERIC(12,2)      NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    cancellation_reason TEXT,
    confirmed_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,

    CONSTRAINT chk_reservation_dates
        CHECK (check_out_date > check_in_date),

    CONSTRAINT chk_cancellation_fields
        CHECK (
            (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL)
            OR status != 'CANCELLED'
        )
);

COMMENT ON TABLE  reservations                   IS 'Núcleo del negocio. Agrupa habitaciones, servicios, factura y pagos de una estadía.';
COMMENT ON COLUMN reservations.total_amount      IS 'Suma de noches * precio + servicios consumidos. Se recalcula antes de emitir la factura.';
COMMENT ON COLUMN reservations.cancellation_reason IS 'Motivo obligatorio cuando status = CANCELLED. Ej: GUEST_REQUEST, NO_SHOW, HOTEL_ERROR.';
COMMENT ON COLUMN reservations.confirmed_at      IS 'Timestamp en que la reserva pasó a estado CONFIRMED.';

CREATE TRIGGER trg_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_reservations_hotel_id        ON reservations(hotel_id);
CREATE INDEX idx_reservations_guest_id        ON reservations(guest_id);
CREATE INDEX idx_reservations_status          ON reservations(status);
CREATE INDEX idx_reservations_check_in_date   ON reservations(check_in_date);
CREATE INDEX idx_reservations_date_range      ON reservations(hotel_id, check_in_date, check_out_date);


-- RESERVATION_ROOMS (N:M Reservations — Rooms)

CREATE TABLE reservation_rooms (
    id             UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID          NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    room_id        UUID          NOT NULL REFERENCES rooms(id)        ON DELETE RESTRICT,
    price_per_night NUMERIC(10,2) NOT NULL CHECK (price_per_night > 0),
    adults         SMALLINT      NOT NULL DEFAULT 1 CHECK (adults >= 1),
    children       SMALLINT      NOT NULL DEFAULT 0 CHECK (children >= 0),

    CONSTRAINT uq_room_per_reservation UNIQUE (reservation_id, room_id)
);

COMMENT ON TABLE  reservation_rooms                 IS 'Habitaciones asignadas a una reserva con el precio vigente al momento de confirmar (snapshot).';
COMMENT ON COLUMN reservation_rooms.price_per_night IS 'Snapshot del precio. Inmutable tras la confirmación; no cambia si el tipo sube de precio.';
COMMENT ON COLUMN reservation_rooms.adults          IS 'Adultos alojados en esta habitación específica.';

CREATE INDEX idx_reservation_rooms_reservation_id ON reservation_rooms(reservation_id);
CREATE INDEX idx_reservation_rooms_room_id        ON reservation_rooms(room_id);

-- Prevención de doble reserva de habitación.
-- Un CHECK constraint no puede consultar otras tablas, por eso se usa un trigger.
-- Este trigger bloquea el INSERT si la habitación ya tiene una reserva activa
-- (PENDING, CONFIRMED, CHECKED_IN) con fechas solapadas.
CREATE OR REPLACE FUNCTION trg_check_room_availability()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_check_in  DATE;
    v_check_out DATE;
    v_conflict  INT;
BEGIN
    -- Obtener las fechas de la reserva que se está creando
    SELECT check_in_date, check_out_date
      INTO v_check_in, v_check_out
      FROM reservations
     WHERE id = NEW.reservation_id;

    -- Buscar conflictos: misma habitación, estados activos, fechas solapadas
    SELECT COUNT(*)
      INTO v_conflict
      FROM reservation_rooms rr
      JOIN reservations r ON r.id = rr.reservation_id
     WHERE rr.room_id = NEW.room_id
       AND rr.id      != NEW.id  -- excluir la fila actual en UPDATEs
       AND r.status   IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
       AND r.check_in_date  < v_check_out
       AND r.check_out_date > v_check_in;

    IF v_conflict > 0 THEN
        RAISE EXCEPTION
            'La habitación % ya tiene una reserva activa en el rango [%, %].',
            NEW.room_id, v_check_in, v_check_out;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_prevent_double_booking
    BEFORE INSERT OR UPDATE ON reservation_rooms
    FOR EACH ROW EXECUTE FUNCTION trg_check_room_availability();


-- SERVICES

CREATE TABLE services (
    id          UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id    UUID          NOT NULL REFERENCES hotels(id) ON DELETE RESTRICT,
    name        VARCHAR(150)  NOT NULL,
    description TEXT,
    unit_price  NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    category    VARCHAR(50)   NOT NULL,  -- 'FOOD', 'SPA', 'TRANSPORT', 'LAUNDRY', etc.
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uq_service_name_per_hotel UNIQUE (hotel_id, name)
);

COMMENT ON TABLE  services           IS 'Catálogo de servicios adicionales facturables del hotel.';
COMMENT ON COLUMN services.is_active IS 'Servicio inactivo: no se puede agregar a nuevas reservas, pero conserva histórico.';
COMMENT ON COLUMN services.category  IS 'FOOD, SPA, TRANSPORT, LAUNDRY, ENTERTAINMENT, etc.';

CREATE TRIGGER trg_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_services_hotel_id ON services(hotel_id);


-- RESERVATION_SERVICES

CREATE TABLE reservation_services (
    id                   UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id       UUID          NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    service_id           UUID          NOT NULL REFERENCES services(id)     ON DELETE RESTRICT,
    quantity             SMALLINT      NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price_snapshot  NUMERIC(10,2) NOT NULL CHECK (unit_price_snapshot >= 0),
    requested_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  reservation_services                    IS 'Servicios adicionales consumidos durante una reserva.';
COMMENT ON COLUMN reservation_services.unit_price_snapshot IS 'Precio unitario en el momento del consumo. Inmutable para preservar integridad de la factura.';

CREATE INDEX idx_reservation_services_reservation_id ON reservation_services(reservation_id);
CREATE INDEX idx_reservation_services_service_id     ON reservation_services(service_id);


-- INVOICES

CREATE TABLE invoices (
    id              UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id  UUID           NOT NULL UNIQUE REFERENCES reservations(id) ON DELETE RESTRICT,
    invoice_number  VARCHAR(30)    NOT NULL UNIQUE,
    subtotal        NUMERIC(12,2)  NOT NULL CHECK (subtotal >= 0),
    tax_rate        NUMERIC(5,4)   NOT NULL DEFAULT 0.16 CHECK (tax_rate >= 0 AND tax_rate <= 1),
    tax_amount      NUMERIC(12,2)  NOT NULL CHECK (tax_amount >= 0),
    total           NUMERIC(12,2)  NOT NULL CHECK (total >= 0),
    status          invoice_status NOT NULL DEFAULT 'DRAFT',
    issued_at       TIMESTAMPTZ,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_invoice_total CHECK (total = subtotal + tax_amount),

    -- issued_at es obligatorio en cualquier estado distinto a DRAFT
    CONSTRAINT chk_invoice_issued_at
        CHECK (
            (status != 'DRAFT' AND issued_at IS NOT NULL)
            OR status = 'DRAFT'
        ),

    -- paid_at es obligatorio cuando la factura está pagada
    CONSTRAINT chk_invoice_paid_at
        CHECK (
            (status = 'PAID' AND paid_at IS NOT NULL)
            OR status != 'PAID'
        )
);

COMMENT ON TABLE  invoices              IS 'Factura generada por reserva. Relación 1:1 con reservations.';
COMMENT ON COLUMN invoices.invoice_number IS 'Número de factura legible por humanos. Ej: INV-2026-000123.';
COMMENT ON COLUMN invoices.tax_rate     IS 'Tasa de impuesto aplicada (ej: 0.16 = 16% IVA). Almacenada como snapshot.';
COMMENT ON COLUMN invoices.total        IS 'BD valida que total = subtotal + tax_amount mediante CHECK constraint.';

CREATE INDEX idx_invoices_status ON invoices(status);


-- PAYMENTS

CREATE TABLE payments (
    id              UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id      UUID           NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount          NUMERIC(12,2)  NOT NULL CHECK (amount > 0),
    payment_method  payment_method NOT NULL,
    transaction_id  VARCHAR(100),
    status          payment_status NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    -- paid_at es obligatorio cuando el pago fue completado
    CONSTRAINT chk_payment_paid_at
        CHECK (
            (status = 'COMPLETED' AND paid_at IS NOT NULL)
            OR status != 'COMPLETED'
        )
);

COMMENT ON TABLE  payments                IS 'Transacciones de pago asociadas a una factura. Una factura puede pagarse en partes.';
COMMENT ON COLUMN payments.transaction_id IS 'ID externo del proveedor de pagos (Stripe, PayPal, etc.). Puede ser NULL para efectivo.';
COMMENT ON COLUMN payments.paid_at        IS 'Timestamp de confirmación del pago. NULL si aún está PENDING o FAILED.';

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_status     ON payments(status);


-- DEPARTMENTS

CREATE TABLE departments (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id    UUID         NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT uq_department_name_per_hotel UNIQUE (hotel_id, name)
);

COMMENT ON TABLE departments IS 'Departamentos internos del hotel (Recepción, Limpieza, Mantenimiento, F&B).';

CREATE INDEX idx_departments_hotel_id ON departments(hotel_id);


-- EMPLOYEES

CREATE TABLE employees (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id      UUID         NOT NULL REFERENCES hotels(id)      ON DELETE RESTRICT,
    department_id UUID         NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    role          VARCHAR(100) NOT NULL,
    hired_at      DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,
    deleted_at    TIMESTAMPTZ
);

COMMENT ON TABLE  employees            IS 'Personal del hotel. Soft-delete para conservar autoría en reportes históricos.';
COMMENT ON COLUMN employees.role       IS 'Cargo del empleado. Ej: RECEPTIONIST, HOUSEKEEPER, MAINTENANCE_TECH, MANAGER.';
COMMENT ON COLUMN employees.deleted_at IS 'Soft-delete al dar de baja al empleado.';

CREATE TRIGGER trg_employees_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_employees_hotel_id      ON employees(hotel_id);
CREATE INDEX idx_employees_department_id ON employees(department_id);


-- MAINTENANCE_REQUESTS

CREATE TABLE maintenance_requests (
    id                    UUID                 PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id               UUID                 NOT NULL REFERENCES rooms(id)      ON DELETE RESTRICT,
    reported_by_employee_id UUID               NOT NULL REFERENCES employees(id)  ON DELETE RESTRICT,
    title                 VARCHAR(200)         NOT NULL,
    description           TEXT,
    priority              maintenance_priority NOT NULL DEFAULT 'MEDIUM',
    status                maintenance_status   NOT NULL DEFAULT 'OPEN',
    resolved_at           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ,

    CONSTRAINT chk_maintenance_resolved
        CHECK (
            (status IN ('RESOLVED', 'CLOSED') AND resolved_at IS NOT NULL)
            OR status NOT IN ('RESOLVED', 'CLOSED')
        )
);

COMMENT ON TABLE  maintenance_requests              IS 'Solicitudes de mantenimiento o reparación en habitaciones.';
COMMENT ON COLUMN maintenance_requests.priority     IS 'LOW, MEDIUM, HIGH, URGENT. Determina el SLA de respuesta.';
COMMENT ON COLUMN maintenance_requests.resolved_at  IS 'Obligatorio cuando status = RESOLVED o CLOSED.';

CREATE TRIGGER trg_maintenance_updated_at
    BEFORE UPDATE ON maintenance_requests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_maintenance_room_id     ON maintenance_requests(room_id);
CREATE INDEX idx_maintenance_status      ON maintenance_requests(status);
CREATE INDEX idx_maintenance_priority    ON maintenance_requests(priority) WHERE status IN ('OPEN', 'IN_PROGRESS');


-- REVIEWS

CREATE TABLE reviews (
    id                 UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id     UUID    NOT NULL UNIQUE REFERENCES reservations(id) ON DELETE RESTRICT,
    guest_id           UUID    NOT NULL REFERENCES guests(id)              ON DELETE RESTRICT,
    overall_rating     SMALLINT NOT NULL CHECK (overall_rating     BETWEEN 1 AND 5),
    cleanliness_rating SMALLINT NOT NULL CHECK (cleanliness_rating BETWEEN 1 AND 5),
    service_rating     SMALLINT NOT NULL CHECK (service_rating     BETWEEN 1 AND 5),
    comment            TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  reviews                   IS 'Reseñas de huéspedes. Solo una por reserva COMPLETED. Inmutables tras su creación.';
COMMENT ON COLUMN reviews.overall_rating    IS 'Rating general de la estadía, de 1 (pésimo) a 5 (excelente).';
COMMENT ON COLUMN reviews.reservation_id    IS 'UNIQUE garantiza máximo una reseña por reserva.';

-- Prevenir reseñas en reservas no completadas.
-- Un CHECK no puede leer otra tabla, así que se usa trigger.
CREATE OR REPLACE FUNCTION trg_check_review_eligibility()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_status     reservation_status;
    v_guest_id   UUID;
BEGIN
    SELECT status, guest_id
      INTO v_status, v_guest_id
      FROM reservations
     WHERE id = NEW.reservation_id;

    IF v_status != 'COMPLETED' THEN
        RAISE EXCEPTION
            'Solo se puede dejar una reseña para reservas en estado COMPLETED. Estado actual: %.',
            v_status;
    END IF;

    IF v_guest_id != NEW.guest_id THEN
        RAISE EXCEPTION
            'El huésped no coincide con el titular de la reserva.';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_enforce_review_eligibility
    BEFORE INSERT ON reviews
    FOR EACH ROW EXECUTE FUNCTION trg_check_review_eligibility();
