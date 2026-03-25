-- =============================================================================
-- seeds.sql — Datos de prueba realistas y contextuales
-- Sistema: Hotelería  |  Motor: PostgreSQL 15+
-- Fecha de referencia: 2026-03-15
--
-- ESCENARIOS CUBIERTOS:
--   A) Valentina Herrera  → Estadía COMPLETADA con servicios, factura pagada y reseña 5★
--   B) Alejandro Reyes    → Huésped actualmente en el hotel (CHECKED_IN)
--   C) María F. Castro    → Reserva CONFIRMADA para el próximo mes
--   D) Roberto Iglesias   → Cancelación por NO_SHOW (pagó reserva pero nunca llegó)
--   E) Sofía Montoya      → Reserva con DOS habitaciones y servicios de spa
--   F) Habitación en MAINTENANCE con solicitud abierta
--   G) Empleado dado de baja (soft-delete)
-- =============================================================================

-- Usar transacción para atomicidad de los seeds

-- ---------------------------------------------------------------------------
-- HOTELS
-- ---------------------------------------------------------------------------

INSERT INTO hotels (id, name, address, city, country, phone, email, star_rating, description) VALUES
(
    'aaaaaaaa-0001-0001-0001-000000000001',
    'Hotel Gran Palacio',
    'Calle de Alcalá 42',
    'Madrid',
    'España',
    '+34 91 555 0100',
    'reservas@granpalacio.es',
    5,
    'Referente del lujo en el corazón de Madrid. Arquitectura belle époque con todas las comodidades del siglo XXI.'
),
(
    'aaaaaaaa-0002-0001-0001-000000000002',
    'Hotel Costa Azul',
    'Blvd. Kukulcán Km 12.5, Zona Hotelera',
    'Cancún',
    'México',
    '+52 998 555 0200',
    'reservas@hotelcostaazul.mx',
    4,
    'Todo incluido frente al mar en la Zona Hotelera de Cancún. Ideal para familias y parejas.'
);


-- ---------------------------------------------------------------------------
-- AMENITIES  (catálogo global, no pertenecen a un hotel)
-- ---------------------------------------------------------------------------

INSERT INTO amenities (id, name, icon, category) VALUES
('cccccccc-0001-0001-0001-000000000001', 'WiFi de alta velocidad',      'wifi',          'TECHNOLOGY'),
('cccccccc-0002-0001-0001-000000000002', 'Smart TV 55"',                 'tv',            'TECHNOLOGY'),
('cccccccc-0003-0001-0001-000000000003', 'Aire acondicionado',           'ac',            'COMFORT'),
('cccccccc-0004-0001-0001-000000000004', 'Minibar surtido',              'minibar',       'COMFORT'),
('cccccccc-0005-0001-0001-000000000005', 'Caja fuerte digital',          'safe',          'COMFORT'),
('cccccccc-0006-0001-0001-000000000006', 'Bañera de hidromasaje',        'bathtub',       'BATHROOM'),
('cccccccc-0007-0001-0001-000000000007', 'Ducha tipo lluvia',            'shower',        'BATHROOM'),
('cccccccc-0008-0001-0001-000000000008', 'Terraza privada con vista',    'balcony',       'VIEW'),
('cccccccc-0009-0001-0001-000000000009', 'Vista panorámica a la ciudad', 'city-view',     'VIEW'),
('cccccccc-0010-0001-0001-000000000010', 'Vista al mar',                 'ocean-view',    'VIEW'),
('cccccccc-0011-0001-0001-000000000011', 'Servicio de mayordomo 24h',    'concierge',     'COMFORT'),
('cccccccc-0012-0001-0001-000000000012', 'Cama king size',               'bed-king',      'COMFORT'),
('cccccccc-0013-0001-0001-000000000013', 'Escritorio ejecutivo',         'desk',          'TECHNOLOGY'),
('cccccccc-0014-0001-0001-000000000014', 'Acceso directo a piscina',     'pool',          'COMFORT');


-- ---------------------------------------------------------------------------
-- ROOM TYPES — Gran Palacio (Madrid)
-- ---------------------------------------------------------------------------

INSERT INTO room_types (id, hotel_id, name, description, max_capacity, base_price_per_night) VALUES
(
    'bbbbbbbb-0001-0001-0001-000000000001',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'Individual Clásica',
    'Habitación individual con estilo clásico madrileño. Perfecta para viajeros de negocios.',
    1, 120.00
),
(
    'bbbbbbbb-0002-0001-0001-000000000002',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'Doble Deluxe',
    'Amplias dimensiones, cama king size y vistas al patio interior. La opción más solicitada por parejas.',
    2, 210.00
),
(
    'bbbbbbbb-0003-0001-0001-000000000003',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'Suite Junior',
    'Sala de estar independiente, bañera de hidromasaje y terraza con vistas a la Gran Vía.',
    3, 380.00
),
(
    'bbbbbbbb-0004-0001-0001-000000000004',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'Suite Presidencial',
    'El máximo lujo del hotel. Dos habitaciones, comedor privado, jacuzzi exterior y servicio de mayordomo exclusivo.',
    4, 750.00
);

-- ROOM TYPES — Costa Azul (Cancún)
INSERT INTO room_types (id, hotel_id, name, description, max_capacity, base_price_per_night) VALUES
(
    'bbbbbbbb-0005-0001-0001-000000000005',
    'aaaaaaaa-0002-0001-0001-000000000002',
    'Estándar Vista Jardín',
    'Habitación cómoda con vista a los jardines tropicales. Incluye acceso ilimitado a todas las amenidades del resort.',
    2, 1800.00
),
(
    'bbbbbbbb-0006-0001-0001-000000000006',
    'aaaaaaaa-0002-0001-0001-000000000002',
    'Superior Vista Mar',
    'Primera línea de playa con balcón privado. Despertarás con el sonido del Caribe a tus pies.',
    2, 2950.00
),
(
    'bbbbbbbb-0007-0001-0001-000000000007',
    'aaaaaaaa-0002-0001-0001-000000000002',
    'Suite Familiar',
    'Dos habitaciones conectadas con área de juegos para niños, acceso preferente a la piscina y cenas sin reserva previa.',
    5, 4500.00
);


-- ---------------------------------------------------------------------------
-- ROOM TYPE ↔ AMENITIES
-- ---------------------------------------------------------------------------

-- Individual Clásica
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0001-0001-0001-000000000001', 'cccccccc-0001-0001-0001-000000000001'),  -- WiFi
('bbbbbbbb-0001-0001-0001-000000000001', 'cccccccc-0002-0001-0001-000000000002'),  -- Smart TV
('bbbbbbbb-0001-0001-0001-000000000001', 'cccccccc-0003-0001-0001-000000000003'),  -- A/C
('bbbbbbbb-0001-0001-0001-000000000001', 'cccccccc-0005-0001-0001-000000000005'),  -- Caja fuerte
('bbbbbbbb-0001-0001-0001-000000000001', 'cccccccc-0013-0001-0001-000000000013'); -- Escritorio

-- Doble Deluxe
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0004-0001-0001-000000000004'),
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0005-0001-0001-000000000005'),
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0007-0001-0001-000000000007'),  -- Ducha lluvia
('bbbbbbbb-0002-0001-0001-000000000002', 'cccccccc-0012-0001-0001-000000000012'); -- King size

-- Suite Junior
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0004-0001-0001-000000000004'),
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0005-0001-0001-000000000005'),
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0006-0001-0001-000000000006'),  -- Bañera hidromasaje
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0008-0001-0001-000000000008'),  -- Terraza privada
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0009-0001-0001-000000000009'), -- Vista ciudad
('bbbbbbbb-0003-0001-0001-000000000003', 'cccccccc-0012-0001-0001-000000000012');

-- Suite Presidencial
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0004-0001-0001-000000000004'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0005-0001-0001-000000000005'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0006-0001-0001-000000000006'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0008-0001-0001-000000000008'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0009-0001-0001-000000000009'),
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0011-0001-0001-000000000011'), -- Mayordomo
('bbbbbbbb-0004-0001-0001-000000000004', 'cccccccc-0012-0001-0001-000000000012');

-- Estándar Vista Jardín (Costa Azul)
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0005-0001-0001-000000000005', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0005-0001-0001-000000000005', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0005-0001-0001-000000000005', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0005-0001-0001-000000000005', 'cccccccc-0014-0001-0001-000000000014'); -- Acceso piscina

-- Superior Vista Mar (Costa Azul)
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0004-0001-0001-000000000004'),
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0008-0001-0001-000000000008'),  -- Terraza
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0010-0001-0001-000000000010'),  -- Vista al mar
('bbbbbbbb-0006-0001-0001-000000000006', 'cccccccc-0014-0001-0001-000000000014');

-- Suite Familiar (Costa Azul)
INSERT INTO room_type_amenities VALUES
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0001-0001-0001-000000000001'),
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0002-0001-0001-000000000002'),
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0003-0001-0001-000000000003'),
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0004-0001-0001-000000000004'),
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0010-0001-0001-000000000010'),
('bbbbbbbb-0007-0001-0001-000000000007', 'cccccccc-0014-0001-0001-000000000014');


-- ---------------------------------------------------------------------------
-- ROOMS — Gran Palacio
-- ---------------------------------------------------------------------------

INSERT INTO rooms (id, hotel_id, room_type_id, room_number, floor, status) VALUES
-- Individual Clásica
('dddddddd-0101-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0001-0001-0001-000000000001', '101', 1, 'AVAILABLE'),
('dddddddd-0102-0001-0001-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0001-0001-0001-000000000001', '102', 1, 'AVAILABLE'),
-- Doble Deluxe
('dddddddd-0201-0001-0001-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0002-0001-0001-000000000002', '201', 2, 'AVAILABLE'),
('dddddddd-0202-0001-0001-000000000004', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0002-0001-0001-000000000002', '202', 2, 'AVAILABLE'),
('dddddddd-0203-0001-0001-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0002-0001-0001-000000000002', '203', 2, 'AVAILABLE'),
-- Suite Junior  (hab. 301 en MAINTENANCE — escenario F)
('dddddddd-0301-0001-0001-000000000006', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0003-0001-0001-000000000003', '301', 3, 'MAINTENANCE'),
('dddddddd-0302-0001-0001-000000000007', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0003-0001-0001-000000000003', '302', 3, 'AVAILABLE'),
-- Suite Presidencial
('dddddddd-0401-0001-0001-000000000008', 'aaaaaaaa-0001-0001-0001-000000000001', 'bbbbbbbb-0004-0001-0001-000000000004', '401', 4, 'AVAILABLE');

-- ROOMS — Costa Azul
INSERT INTO rooms (id, hotel_id, room_type_id, room_number, floor, status) VALUES
-- Estándar Vista Jardín
('dddddddd-1101-0001-0001-000000000009', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0005-0001-0001-000000000005', '101', 1, 'AVAILABLE'),
('dddddddd-1102-0001-0001-000000000010', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0005-0001-0001-000000000005', '102', 1, 'AVAILABLE'),
('dddddddd-1103-0001-0001-000000000011', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0005-0001-0001-000000000005', '103', 1, 'AVAILABLE'),
-- Superior Vista Mar
('dddddddd-1201-0001-0001-000000000012', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0006-0001-0001-000000000006', '201', 2, 'OCCUPIED'),  -- Alejandro está aquí
('dddddddd-1202-0001-0001-000000000013', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0006-0001-0001-000000000006', '202', 2, 'AVAILABLE'),
-- Suite Familiar
('dddddddd-1301-0001-0001-000000000014', 'aaaaaaaa-0002-0001-0001-000000000002', 'bbbbbbbb-0007-0001-0001-000000000007', '301', 3, 'AVAILABLE');


-- ---------------------------------------------------------------------------
-- SERVICES
-- ---------------------------------------------------------------------------

-- Gran Palacio
INSERT INTO services (id, hotel_id, name, description, unit_price, category, is_active) VALUES
('eeeeeeee-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Cena romántica en habitación', 'Menú de 4 tiempos con vino seleccionado por nuestro sommelier.', 185.00, 'FOOD', TRUE),
('eeeeeeee-0002-0001-0001-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Desayuno buffet', 'Desayuno continental con productos frescos de temporada. Incluido en suites, extra para otras categorías.', 28.00, 'FOOD', TRUE),
('eeeeeeee-0003-0001-0001-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Masaje relajante 60 min', 'Masaje con aceites esenciales en nuestro spa de 5 estrellas.', 120.00, 'SPA', TRUE),
('eeeeeeee-0004-0001-0001-000000000004', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Transfer aeropuerto (ida)', 'Traslado privado Aeropuerto Adolfo Suárez — hotel en vehículo ejecutivo.', 65.00, 'TRANSPORT', TRUE),
('eeeeeeee-0005-0001-0001-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Servicio de lavandería express', 'Recogida, lavado, planchado y entrega en 4 horas.', 35.00, 'LAUNDRY', TRUE),
('eeeeeeee-0006-0001-0001-000000000006', 'aaaaaaaa-0001-0001-0001-000000000001',
    'Tour Madrid Histórico', 'Excursión guiada de 4h por el Madrid de los Austrias y los Borbones. DESCONTINUADO.', 90.00, 'ENTERTAINMENT', FALSE);

-- Costa Azul
INSERT INTO services (id, hotel_id, name, description, unit_price, category, is_active) VALUES
('eeeeeeee-0007-0001-0001-000000000007', 'aaaaaaaa-0002-0001-0001-000000000002',
    'Tour en catamarán', 'Recorrido de día completo por la costa de Cancún con snorkel y comida a bordo.', 1200.00, 'ENTERTAINMENT', TRUE),
('eeeeeeee-0008-0001-0001-000000000008', 'aaaaaaaa-0002-0001-0001-000000000002',
    'Masaje piedras calientes 90 min', 'Terapia de relajación profunda en nuestro spa frente al mar.', 950.00, 'SPA', TRUE),
('eeeeeeee-0009-0001-0001-000000000009', 'aaaaaaaa-0002-0001-0001-000000000002',
    'Cena en restaurante de playa', 'Mesa privada con los pies en la arena, mariscos frescos y mariachi.', 2200.00, 'FOOD', TRUE),
('eeeeeeee-0010-0001-0001-000000000010', 'aaaaaaaa-0002-0001-0001-000000000002',
    'Renta de equipo de snorkel', 'Aletas, careta y tubo por día. Incluye seguro de equipo.', 320.00, 'ENTERTAINMENT', TRUE);


-- ---------------------------------------------------------------------------
-- GUESTS
-- ---------------------------------------------------------------------------

INSERT INTO guests (id, first_name, last_name, email, phone, document_type, document_number, nationality, date_of_birth) VALUES
(
    'ffffffff-0001-0001-0001-000000000001',
    'Valentina', 'Herrera Ospina',
    'valentina.herrera@gmail.com',
    '+57 310 555 0101',
    'PASSPORT', 'CC-52847193',
    'Colombiana',
    '1990-07-14'
),
(
    'ffffffff-0002-0001-0001-000000000002',
    'Alejandro', 'Reyes Vidal',
    'alejandro.reyes@outlook.com',
    '+34 612 555 0202',
    'DNI', '47382910X',
    'Española',
    '1985-03-22'
),
(
    'ffffffff-0003-0001-0001-000000000003',
    'María Fernanda', 'Castro Ríos',
    'mfcastro@techcorp.com',
    '+52 55 555 0303',
    'PASSPORT', 'G-28471039',
    'Mexicana',
    '1992-11-05'
),
(
    'ffffffff-0004-0001-0001-000000000004',
    'Roberto', 'Iglesias Moreno',
    'r.iglesias@hotmail.com',
    '+34 629 555 0404',
    'DNI', '33019274B',
    'Española',
    '1978-08-30'
),
(
    'ffffffff-0005-0001-0001-000000000005',
    'Sofía', 'Montoya Blanco',
    'sofia.montoya@empresa.es',
    '+34 677 555 0505',
    'DNI', '50293847K',
    'Española',
    '1988-01-19'
);


-- ---------------------------------------------------------------------------
-- DEPARTMENTS
-- ---------------------------------------------------------------------------

-- Gran Palacio
INSERT INTO departments (id, hotel_id, name, description) VALUES
('11111111-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', 'Recepción',     'Check-in, check-out, atención al huésped y reservas.'),
('11111111-0002-0001-0001-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', 'Housekeeping',  'Limpieza y mantenimiento del estado de habitaciones.'),
('11111111-0003-0001-0001-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001', 'Mantenimiento', 'Reparaciones eléctricas, de plomería y equipamiento general.');

-- Costa Azul
INSERT INTO departments (id, hotel_id, name, description) VALUES
('11111111-0004-0001-0001-000000000004', 'aaaaaaaa-0002-0001-0001-000000000002', 'Recepción',     'Atención al cliente y coordinación de servicios del resort.'),
('11111111-0005-0001-0001-000000000005', 'aaaaaaaa-0002-0001-0001-000000000002', 'Mantenimiento', 'Mantenimiento preventivo y correctivo de instalaciones.');


-- ---------------------------------------------------------------------------
-- EMPLOYEES
-- ---------------------------------------------------------------------------

INSERT INTO employees (id, hotel_id, department_id, first_name, last_name, email, role, hired_at) VALUES
-- Gran Palacio — Recepción
(
    '22222222-0001-0001-0001-000000000001',
    'aaaaaaaa-0001-0001-0001-000000000001',
    '11111111-0001-0001-0001-000000000001',
    'Carmen', 'Aldana Fuentes',
    'c.aldana@granpalacio.es',
    'RECEPTIONIST',
    '2021-04-10'
),
-- Gran Palacio — Mantenimiento (activo)
(
    '22222222-0002-0001-0001-000000000002',
    'aaaaaaaa-0001-0001-0001-000000000001',
    '11111111-0003-0001-0001-000000000003',
    'Tomás', 'Vega Rueda',
    't.vega@granpalacio.es',
    'MAINTENANCE_TECH',
    '2019-09-01'
),
-- Gran Palacio — Housekeeping (dado de baja — escenario G)
(
    '22222222-0003-0001-0001-000000000003',
    'aaaaaaaa-0001-0001-0001-000000000001',
    '11111111-0002-0001-0001-000000000002',
    'Luis', 'Paredes Ortega',
    'l.paredes@granpalacio.es',
    'HOUSEKEEPER',
    '2020-02-15'
),
-- Costa Azul — Recepción
(
    '22222222-0004-0001-0001-000000000004',
    'aaaaaaaa-0002-0001-0001-000000000002',
    '11111111-0004-0001-0001-000000000004',
    'Itzel', 'Ramírez Balam',
    'i.ramirez@hotelcostaazul.mx',
    'RECEPTIONIST',
    '2022-06-20'
),
-- Costa Azul — Mantenimiento
(
    '22222222-0005-0001-0001-000000000005',
    'aaaaaaaa-0002-0001-0001-000000000002',
    '11111111-0005-0001-0001-000000000005',
    'Ernesto', 'Chan Poot',
    'e.chan@hotelcostaazul.mx',
    'MAINTENANCE_TECH',
    '2023-01-08'
);

-- Escenario G: Luis Paredes fue dado de baja por reestructuración del área
UPDATE employees
   SET deleted_at = '2026-01-31 18:00:00+01'
 WHERE id = '22222222-0003-0001-0001-000000000003';


-- ---------------------------------------------------------------------------
-- RESERVATIONS
-- ---------------------------------------------------------------------------

-- ── ESCENARIO A: Valentina — Estadía completada en Gran Palacio (Suite Junior 302) ──
INSERT INTO reservations (id, hotel_id, guest_id, check_in_date, check_out_date, status, total_amount, confirmed_at) VALUES
(
    '33333333-0001-0001-0001-000000000001',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'ffffffff-0001-0001-0001-000000000001',
    '2026-02-10',
    '2026-02-14',   -- 4 noches
    'COMPLETED',
    1722.20,        -- 4×380 (noches) + 185 (cena) + 28×2 (desayunos) + IVA 10%
    '2026-01-28 14:32:00+01'
);

-- ── ESCENARIO B: Alejandro — Actualmente en el hotel (Costa Azul, hab. 201) ──
INSERT INTO reservations (id, hotel_id, guest_id, check_in_date, check_out_date, status, total_amount, confirmed_at) VALUES
(
    '33333333-0002-0001-0001-000000000002',
    'aaaaaaaa-0002-0001-0001-000000000002',
    'ffffffff-0002-0001-0001-000000000002',
    '2026-03-12',
    '2026-03-17',   -- 5 noches
    'CHECKED_IN',
    17110.00,       -- 5×2950 + IVA 16%
    '2026-03-01 10:15:00+01'
);

-- ── ESCENARIO C: María Fernanda — Reserva confirmada para próximo mes ──
INSERT INTO reservations (id, hotel_id, guest_id, check_in_date, check_out_date, status, total_amount, confirmed_at) VALUES
(
    '33333333-0003-0001-0001-000000000003',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'ffffffff-0003-0001-0001-000000000003',
    '2026-04-20',
    '2026-04-25',   -- 5 noches
    'CONFIRMED',
    1215.50,        -- 5×210 + desayuno 5×28 + IVA 10%
    '2026-03-10 09:00:00+01'
);

-- ── ESCENARIO D: Roberto — NO_SHOW (confirmó, pagó, nunca llegó) ──
INSERT INTO reservations (
    id, hotel_id, guest_id, check_in_date, check_out_date,
    status, total_amount, confirmed_at, cancelled_at, cancellation_reason
) VALUES
(
    '33333333-0004-0001-0001-000000000004',
    'aaaaaaaa-0002-0001-0001-000000000002',
    'ffffffff-0004-0001-0001-000000000004',
    '2026-03-01',
    '2026-03-05',   -- 4 noches, nunca llegó
    'CANCELLED',
    8352.00,        -- 4×1800 + IVA 16%
    '2026-02-10 16:45:00+01',
    '2026-03-02 12:00:00+01',
    'NO_SHOW'
);

-- ── ESCENARIO E: Sofía — 2 habitaciones Doble Deluxe + servicios de spa ──
INSERT INTO reservations (id, hotel_id, guest_id, check_in_date, check_out_date, status, total_amount, confirmed_at) VALUES
(
    '33333333-0005-0001-0001-000000000005',
    'aaaaaaaa-0001-0001-0001-000000000001',
    'ffffffff-0005-0001-0001-000000000005',
    '2026-01-15',
    '2026-01-20',   -- 5 noches
    'COMPLETED',
    2684.20,        -- (201+202)×5×210 + 2×masaje120 + IVA 10%
    '2026-01-05 11:00:00+01'
);


-- ---------------------------------------------------------------------------
-- RESERVATION_ROOMS
-- ---------------------------------------------------------------------------

-- Valentina → Suite Junior 302 (hab. 301 está en mantenimiento, se le asignó la 302)
INSERT INTO reservation_rooms (id, reservation_id, room_id, price_per_night, adults, children) VALUES
(
    '44444444-0001-0001-0001-000000000001',
    '33333333-0001-0001-0001-000000000001',
    'dddddddd-0302-0001-0001-000000000007',  -- Suite Junior 302
    380.00, 2, 0
);

-- Alejandro → Superior Vista Mar 201
INSERT INTO reservation_rooms (id, reservation_id, room_id, price_per_night, adults, children) VALUES
(
    '44444444-0002-0001-0001-000000000002',
    '33333333-0002-0001-0001-000000000002',
    'dddddddd-1201-0001-0001-000000000012',  -- Superior Vista Mar 201
    2950.00, 1, 0
);

-- María Fernanda → Doble Deluxe 203
INSERT INTO reservation_rooms (id, reservation_id, room_id, price_per_night, adults, children) VALUES
(
    '44444444-0003-0001-0001-000000000003',
    '33333333-0003-0001-0001-000000000003',
    'dddddddd-0203-0001-0001-000000000005',  -- Doble Deluxe 203
    210.00, 2, 0
);

-- Roberto → Estándar Vista Jardín 101 (nunca se ocupó)
INSERT INTO reservation_rooms (id, reservation_id, room_id, price_per_night, adults, children) VALUES
(
    '44444444-0004-0001-0001-000000000004',
    '33333333-0004-0001-0001-000000000004',
    'dddddddd-1101-0001-0001-000000000009',  -- Estándar Vista Jardín 101
    1800.00, 2, 0
);

-- Sofía → DOS habitaciones Doble Deluxe (201 y 202) para ella y su pareja + amigos
INSERT INTO reservation_rooms (id, reservation_id, room_id, price_per_night, adults, children) VALUES
(
    '44444444-0005-0001-0001-000000000005',
    '33333333-0005-0001-0001-000000000005',
    'dddddddd-0201-0001-0001-000000000003',  -- Doble Deluxe 201
    210.00, 2, 0
),
(
    '44444444-0006-0001-0001-000000000006',
    '33333333-0005-0001-0001-000000000005',
    'dddddddd-0202-0001-0001-000000000004',  -- Doble Deluxe 202
    210.00, 2, 1
);


-- ---------------------------------------------------------------------------
-- RESERVATION_SERVICES
-- ---------------------------------------------------------------------------

-- Valentina: 1 cena romántica + 2 desayunos
INSERT INTO reservation_services (id, reservation_id, service_id, quantity, unit_price_snapshot, requested_at) VALUES
(
    '55555555-0001-0001-0001-000000000001',
    '33333333-0001-0001-0001-000000000001',
    'eeeeeeee-0001-0001-0001-000000000001',  -- Cena romántica
    1, 185.00,
    '2026-02-11 20:30:00+01'
),
(
    '55555555-0002-0001-0001-000000000002',
    '33333333-0001-0001-0001-000000000001',
    'eeeeeeee-0002-0001-0001-000000000002',  -- Desayuno buffet
    2, 28.00,
    '2026-02-11 08:00:00+01'
);

-- Alejandro (en curso): solicitó tour en catamarán para mañana
INSERT INTO reservation_services (id, reservation_id, service_id, quantity, unit_price_snapshot, requested_at) VALUES
(
    '55555555-0003-0001-0001-000000000003',
    '33333333-0002-0001-0001-000000000002',
    'eeeeeeee-0007-0001-0001-000000000007',  -- Tour catamarán
    2, 1200.00,
    '2026-03-14 11:00:00+01'
);

-- Sofía: 2 masajes relajantes (uno para cada habitación)
INSERT INTO reservation_services (id, reservation_id, service_id, quantity, unit_price_snapshot, requested_at) VALUES
(
    '55555555-0004-0001-0001-000000000004',
    '33333333-0005-0001-0001-000000000005',
    'eeeeeeee-0003-0001-0001-000000000003',  -- Masaje 60 min
    2, 120.00,
    '2026-01-17 16:00:00+01'
);


-- ---------------------------------------------------------------------------
-- INVOICES
-- ---------------------------------------------------------------------------

-- Valentina — PAID
-- subtotal: 4×380 + 185 + 2×28 = 1,760.00  |  IVA 10%: 176.00  |  total: 1,936.00
-- (Nota: total_amount en reserva incluye ya el IVA con decimales de redondeo)
INSERT INTO invoices (id, reservation_id, invoice_number, subtotal, tax_rate, tax_amount, total, status, issued_at, paid_at) VALUES
(
    '66666666-0001-0001-0001-000000000001',
    '33333333-0001-0001-0001-000000000001',
    'INV-2026-000001',
    1761.00, 0.10, 176.10, 1937.10,
    'PAID',
    '2026-02-14 11:00:00+01',
    '2026-02-14 11:45:00+01'
);

-- Alejandro — ISSUED (pendiente de pago al checkout)
-- subtotal: 5×2950 + 2×1200 = 17,150.00  |  IVA 16%: 2,744.00  |  total: 19,894.00
INSERT INTO invoices (id, reservation_id, invoice_number, subtotal, tax_rate, tax_amount, total, status, issued_at) VALUES
(
    '66666666-0002-0001-0001-000000000002',
    '33333333-0002-0001-0001-000000000002',
    'INV-2026-000002',
    17150.00, 0.16, 2744.00, 19894.00,
    'ISSUED',
    '2026-03-12 15:00:00+01'
);

-- María Fernanda — DRAFT (no se emite hasta el check-in)
INSERT INTO invoices (id, reservation_id, invoice_number, subtotal, tax_rate, tax_amount, total, status) VALUES
(
    '66666666-0003-0001-0001-000000000003',
    '33333333-0003-0001-0001-000000000003',
    'INV-2026-000003',
    1195.00, 0.10, 119.50, 1314.50,
    'DRAFT'
);

-- Roberto — VOID (factura anulada tras la cancelación por no-show)
INSERT INTO invoices (id, reservation_id, invoice_number, subtotal, tax_rate, tax_amount, total, status, issued_at) VALUES
(
    '66666666-0004-0001-0001-000000000004',
    '33333333-0004-0001-0001-000000000004',
    'INV-2026-000004',
    7200.00, 0.16, 1152.00, 8352.00,
    'VOID',
    '2026-02-10 17:00:00+01'
);

-- Sofía — PAID
-- subtotal: (201+202)×5×210 + 2×120 = 2,340.00  |  IVA 10%: 234.00  |  total: 2,574.00
INSERT INTO invoices (id, reservation_id, invoice_number, subtotal, tax_rate, tax_amount, total, status, issued_at, paid_at) VALUES
(
    '66666666-0005-0001-0001-000000000005',
    '33333333-0005-0001-0001-000000000005',
    'INV-2026-000005',
    2340.00, 0.10, 234.00, 2574.00,
    'PAID',
    '2026-01-20 10:30:00+01',
    '2026-01-20 11:00:00+01'
);


-- ---------------------------------------------------------------------------
-- PAYMENTS
-- ---------------------------------------------------------------------------

-- Valentina — 1 pago completo con tarjeta de crédito
INSERT INTO payments (id, invoice_id, amount, payment_method, transaction_id, status, paid_at) VALUES
(
    '77777777-0001-0001-0001-000000000001',
    '66666666-0001-0001-0001-000000000001',
    1937.10,
    'CREDIT_CARD',
    'stripe_ch_3OqV2HGbNPXQl5Eq0Kvz1YJa',
    'COMPLETED',
    '2026-02-14 11:45:00+01'
);

-- Roberto — pagó al confirmar, luego se procesó el reembolso parcial (política no-show: 50%)
INSERT INTO payments (id, invoice_id, amount, payment_method, transaction_id, status, paid_at) VALUES
(
    '77777777-0002-0001-0001-000000000002',
    '66666666-0004-0001-0001-000000000004',
    8352.00,
    'CREDIT_CARD',
    'stripe_ch_3PaZ9KGbNPXQl5Eq1Mwx8VCd',
    'COMPLETED',
    '2026-02-10 17:05:00+01'
),
(
    '77777777-0003-0001-0001-000000000003',
    '66666666-0004-0001-0001-000000000004',
    4176.00,        -- 50% devuelto (política no-show)
    'CREDIT_CARD',
    'stripe_re_3PaZ9KGbNPXQl5Eq1Mwx8VCd',
    'REFUNDED',
    '2026-03-03 09:00:00+01'
);

-- Sofía — pagó en dos partes: anticipo + saldo en checkout
INSERT INTO payments (id, invoice_id, amount, payment_method, transaction_id, status, paid_at) VALUES
(
    '77777777-0004-0001-0001-000000000004',
    '66666666-0005-0001-0001-000000000005',
    1000.00,        -- anticipo al confirmar
    'BANK_TRANSFER',
    'TRANS-ES-20260105-00842',
    'COMPLETED',
    '2026-01-05 11:30:00+01'
),
(
    '77777777-0005-0001-0001-000000000005',
    '66666666-0005-0001-0001-000000000005',
    1574.00,        -- saldo restante en checkout
    'CREDIT_CARD',
    'stripe_ch_3QbA0LGcOQYRm6Fr2Nxy9WDe',
    'COMPLETED',
    '2026-01-20 11:00:00+01'
);


-- ---------------------------------------------------------------------------
-- MAINTENANCE_REQUESTS
-- Escenario F: habitación 301 del Gran Palacio en MAINTENANCE
-- ---------------------------------------------------------------------------

-- Solicitud activa: fuga de agua en el baño de la Suite Junior 301
INSERT INTO maintenance_requests (
    id, room_id, reported_by_employee_id,
    title, description, priority, status
) VALUES
(
    '88888888-0001-0001-0001-000000000001',
    'dddddddd-0301-0001-0001-000000000006',    -- Suite Junior 301
    '22222222-0002-0001-0001-000000000002',    -- Tomás Vega (técnico)
    'Fuga de agua en tubería bajo lavabo',
    'El huésped anterior reportó goteo constante bajo el mueble del lavabo. Se detectó grieta en sifón de PVC. Requiere cambio de pieza.',
    'HIGH',
    'IN_PROGRESS'
);

-- Solicitud ya resuelta en Costa Azul: fallo de A/C en hab. 103 (ya reparado)
INSERT INTO maintenance_requests (
    id, room_id, reported_by_employee_id,
    title, description, priority, status, resolved_at
) VALUES
(
    '88888888-0002-0001-0001-000000000002',
    'dddddddd-1103-0001-0001-000000000011',    -- Estándar Vista Jardín 103
    '22222222-0005-0001-0001-000000000005',    -- Ernesto Chan (técnico Costa Azul)
    'Aire acondicionado no enfría correctamente',
    'El compresor del A/C no alcanzaba la temperatura programada. Se limpió filtro y se recargó gas refrigerante R-410A.',
    'MEDIUM',
    'RESOLVED',
    '2026-03-08 14:30:00-06'
);


-- ---------------------------------------------------------------------------
-- REVIEWS
-- ---------------------------------------------------------------------------

-- Valentina deja reseña 5★ para su estadía completada
INSERT INTO reviews (id, reservation_id, guest_id, overall_rating, cleanliness_rating, service_rating, comment) VALUES
(
    '99999999-0001-0001-0001-000000000001',
    '33333333-0001-0001-0001-000000000001',
    'ffffffff-0001-0001-0001-000000000001',
    5, 5, 5,
    'Una experiencia absolutamente impecable. La suite tenía vistas espectaculares a la Gran Vía y el personal fue atento en todo momento. La cena romántica en la habitación fue el detalle perfecto para celebrar nuestro aniversario. Volveremos sin duda.'
);

-- Sofía deja reseña 4★ (excelente, pero con sugerencia sobre el spa)
INSERT INTO reviews (id, reservation_id, guest_id, overall_rating, cleanliness_rating, service_rating, comment) VALUES
(
    '99999999-0002-0001-0001-000000000002',
    '33333333-0005-0001-0001-000000000005',
    'ffffffff-0005-0001-0001-000000000005',
    4, 5, 4,
    'Hotel precioso con habitaciones amplias y muy limpias. El desayuno buffet es de lo mejor que hemos probado. Le quitaría una estrella al servicio porque el spa tardó casi una hora en confirmarnos la cita para los masajes. Por lo demás, todo perfecto.'
);

