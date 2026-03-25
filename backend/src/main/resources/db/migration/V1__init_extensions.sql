-- =============================================================================
-- 00_init.sql — Extensiones y configuración base
-- Sistema: Hotelería
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- Generación de UUIDs v4
CREATE EXTENSION IF NOT EXISTS "pgcrypto";    -- Funciones criptográficas (hashing)
CREATE EXTENSION IF NOT EXISTS "unaccent";    -- Búsqueda de texto sin acentos
