# BUSINESS RULES — Sistema de Hotelería

> @Architect — Dominio: Hotel Management
> Fecha: 2026-03-15

---

## 1. ENTIDADES PRINCIPALES

| Entidad | Descripción |
|---|---|
| `hotels` | Propiedades hoteleras. Un sistema puede gestionar múltiples hoteles. |
| `room_types` | Categorías de habitación (Simple, Doble, Suite, etc.) con precio base. |
| `rooms` | Habitaciones físicas individuales, cada una pertenece a un tipo. |
| `amenities` | Servicios/comodidades disponibles (WiFi, TV, Jacuzzi, etc.). |
| `room_type_amenities` | Qué amenidades incluye cada tipo de habitación (N:M). |
| `guests` | Huéspedes registrados con sus datos de identificación. |
| `reservations` | Reservas realizadas por huéspedes. Núcleo del negocio. |
| `reservation_rooms` | Habitaciones específicas asignadas a una reserva (N:M con precio snapshot). |
| `services` | Servicios adicionales que el hotel cobra aparte (room service, spa, parking). |
| `reservation_services` | Servicios consumidos durante una reserva. |
| `invoices` | Factura generada al finalizar o confirmar una reserva. |
| `payments` | Pagos realizados contra una factura (una factura puede tener múltiples pagos parciales). |
| `departments` | Departamentos del hotel (Recepción, Limpieza, Mantenimiento, F&B). |
| `employees` | Personal del hotel asignado a departamentos. |
| `maintenance_requests` | Solicitudes de mantenimiento de habitaciones, reportadas por empleados. |
| `reviews` | Reseñas que los huéspedes dejan al finalizar una estadía. |

---

## 2. CICLO DE VIDA DE UNA RESERVA

```
PENDING → CONFIRMED → CHECKED_IN → COMPLETED
    ↓           ↓
 CANCELLED   CANCELLED
```

| Estado | Significado |
|---|---|
| `PENDING` | Reserva creada, pendiente de confirmación/pago. |
| `CONFIRMED` | Reserva confirmada con pago anticipado o garantía registrada. |
| `CHECKED_IN` | El huésped realizó el check-in. La habitación pasa a `OCCUPIED`. |
| `COMPLETED` | Check-out realizado, factura emitida. |
| `CANCELLED` | Cancelada por el huésped o el hotel. Registra motivo. |

**Regla crítica:** Solo puede existir una reserva en estado `CONFIRMED` o `CHECKED_IN` para la misma habitación en un rango de fechas superpuesto.

---

## 3. REGLAS DE HABITACIONES

- **Estados de habitación:** `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`, `OUT_OF_SERVICE`.
- Una habitación solo puede estar `OCCUPIED` si existe una reserva en estado `CHECKED_IN` que la incluye.
- Si una habitación entra en `MAINTENANCE` u `OUT_OF_SERVICE`, no puede ser asignada a nuevas reservas. Las reservas futuras existentes deben ser reasignadas manualmente.
- Las habitaciones se eliminan lógicamente (`deleted_at`), nunca físicamente, para preservar el historial de reservas.
- El `room_number` es único **dentro de un hotel** (no globalmente).

---

## 4. REGLAS DE PRECIOS Y FACTURACIÓN

- El precio en `reservation_rooms.price_per_night` es un **snapshot** del precio al momento de confirmar la reserva. Si el `base_price_per_night` del tipo de habitación cambia, las reservas existentes no se ven afectadas.
- Igualmente, `reservation_services.unit_price_snapshot` es el precio del servicio en el momento de consumirlo.
- El `total_amount` de la reserva = Σ(precio_noche × noches) + Σ(servicios consumidos).
- `invoices.total` = `subtotal` + `tax_amount`.
- Una factura puede ser pagada en múltiples transacciones (`payments`). El estado de la factura es `PAID` cuando la suma de pagos exitosos iguala el total.
- Estados de factura: `DRAFT`, `ISSUED`, `PAID`, `VOID`.
- Estados de pago: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`.

---

## 5. REGLAS DE CANCELACIÓN

- Si `status = CANCELLED`, los campos `cancelled_at` y `cancellation_reason` son **obligatorios**.
- Al cancelar, todas las habitaciones asociadas en `reservation_rooms` deben regresar a `AVAILABLE` si no hay otra reserva activa.
- La política de reembolso (completo, parcial, ninguno) se define a nivel de negocio según la antelación de la cancelación, pero se registra como un `PAYMENT` con `status = REFUNDED`.

---

## 6. REGLAS DE HUÉSPEDES

- El `email` del huésped es único en la tabla `guests`. Es el identificador de negocio.
- El par `(document_type, document_number)` debe ser único. Un huésped no puede registrarse dos veces con el mismo documento.
- `document_type` acepta: `PASSPORT`, `DNI`, `NIE`, `DRIVER_LICENSE`, `OTHER`.
- La edad mínima para realizar una reserva es 18 años (`date_of_birth`).

---

## 7. REGLAS DE CAPACIDAD

- `reservation_rooms.adults + reservation_rooms.children` **no puede superar** `room_types.max_capacity`.
- Una reserva puede incluir múltiples habitaciones (ej: una familia que reserva dos habitaciones contiguas).

---

## 8. REGLAS DE RESEÑAS

- Un huésped solo puede dejar **una reseña por reserva**.
- Solo se puede dejar una reseña si la reserva tiene estado `COMPLETED`.
- Los ratings (`overall_rating`, `cleanliness_rating`, `service_rating`) son enteros entre 1 y 5.

---

## 9. REGLAS DE EMPLEADOS Y MANTENIMIENTO

- Un empleado pertenece a **un único hotel** y **un único departamento**.
- Al dar de baja a un empleado se usa soft-delete (`deleted_at`).
- Las solicitudes de mantenimiento tienen prioridad: `LOW`, `MEDIUM`, `HIGH`, `URGENT`.
- Estados de mantenimiento: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.
- Una habitación en estado `MAINTENANCE` debe tener al menos un `maintenance_request` en estado `OPEN` o `IN_PROGRESS`.

---

## 10. CASOS BORDE IDENTIFICADOS

| Caso | Decisión |
|---|---|
| Se elimina un tipo de habitación que tiene habitaciones activas. | `RESTRICT` — No se permite hasta reasignar habitaciones. |
| Se elimina un hotel con datos históricos. | Soft-delete (`deleted_at`). Las reservas y facturas pasadas se conservan. |
| Un huésped quiere reservar, pero no hay habitaciones disponibles del tipo solicitado. | El sistema no debe crear la reserva. Validación a nivel de aplicación y restricción de BD. |
| Se solicita un servicio que ya no está activo (`is_active = false`). | `RESTRICT` — No se puede agregar a nuevas reservas. Las existentes conservan el snapshot. |
| Un pago falla durante el check-in. | La reserva permanece en `CONFIRMED`. El pago queda en `FAILED`. Se puede reintentar con un nuevo `PAYMENT`. |
| El huésped no hace check-in en la fecha reservada (no-show). | Debe existir un proceso (cron/manual) que marque como `CANCELLED` con `cancellation_reason = 'NO_SHOW'`. |
