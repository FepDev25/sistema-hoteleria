# Guía de Sprints - Frontend (React + TypeScript + Vite)

Esta guía define la hoja de ruta obligatoria para las sesiones del frontend. 
**Regla de oro:** No inventar tipos ni payloads. Todo el modelo de datos y rutas API debe consumirse estrictamente desde el `openapi.yaml` auto-generado.

## Sprint 0: Configuración y Generación de Cliente
**Objetivo:** Establecer la arquitectura base del monorepo y la tipificación segura.
1. Configurar Vite con React, TypeScript y TailwindCSS en `apps/web`.
2. Instalar herramientas de generación de cliente OpenAPI (ej. `@orval/core` o `openapi-typescript-codegen`).
3. Crear el script en `package.json` para generar automáticamente los hooks/servicios (`npm run generate-api`) apuntando al `openapi.yaml` de la raíz.
4. Configurar React Router DOM v6+ con un layout base y manejo de zonas:
   - Rutas `/` (público)
   - Rutas `/admin/*` (staff)
5. **Verificación:** Tipos estrictos generados y aplicación levantando con "Hello World" en ambas zonas de enrutamiento.

## Sprint 1: Catálogos y Admin Base
**Objetivo:** Implementar la interfaz para la configuración central del hotel.
1. Crear layout para el Dashboard del Staff (Sidebar + Navbar).
2. Pantalla de listado y ABM de Hoteles.
3. Pantalla de listado de Tipos de Habitación, Habitaciones y sus Amenidades.
4. Pantalla de Servicios Adicionales (con listado de precios vigentes).
5. Implementar el manejo de fechas estricto: Todo el `Datepicker` debe emitir strings `YYYY-MM-DD` sin hora y sin mutar el timezone local.
6. **Verificación:** El administrador puede visualizar y crear un Hotel con sus habitaciones sin errores de tipado o mutaciones no deseadas.

## Sprint 2: Portal de Reservas y Gestión de Huéspedes
**Objetivo:** Flujo público de reservas y gestión administrativa del cliente.
1. Vista Pública: Motor de Reservas (Selección de Hotel -> Selección de Fechas -> Selección de Habitación -> Carga de datos de Huésped).
2. Vista Pública: Confirmación de reserva y generación de PNR / Localizador.
3. Vista Admin: Directorio de Huéspedes (listado, creación manual, detalle).
4. Vista Admin: Calendario o listado de reservas con estado visual (colores para `PENDING`, `CONFIRMED`, `CANCELLED`).
5. **Verificación:** Un huésped puede reservar a través del wizard y el staff la visualiza `CONFIRMED` en su panel.

## Sprint 3: Recepción (Check-in / Check-out) y Consumos
**Objetivo:** Operativa del Front-Desk.
1. Vista Admin (Recepción): Dashboard "Llegadas y Salidas de Hoy".
2. Funcionalidad: Botón de "Check-in" que ejecuta la transición de estado.
3. Vista Detalle de Reserva (Activa): Panel para agregar consumos extras (Servicios) a la habitación. 
   - La UI debe alertar si el servicio está desactivado (BR-11).
4. Panel de Pre-facturación: Visualización de los subtotales, impuestos y monto total. Botón de emitir factura (`ISSUED`).
5. **Verificación:** Recepcionista ingresa a un huésped, le carga un consumo extra de Restaurante y pre-visualiza el balance de la factura correctamente sumado.

## Sprint 4: Pagos, Mantenimiento y Cierre
**Objetivo:** Cierre del ciclo contable y manejo de contingencias.
1. Vista Factura (`ISSUED`): Módulo de Pagos. Permite registrar un pago total o parcial según métodos de pago (Cash, Card).
   - Botón `Refund`: Permite ingresar un monto negativo para anular un cobro y cancelar la reserva.
2. Vista Admin: Listado de Facturas (DRAFT, ISSUED, PAID, VOID).
3. Vista de Mantenimiento: Formulario para bloquear habitación y abrir ticket.
   - La UI del calendario de reservas debe mostrar visualmente un "Alerta de Reasignación" (`requiresManualReassignment === true`) si el ticket de mantenimiento afectó reservas futuras.
4. Vista Pública / Portal de Cliente: Formulario de Reseñas (Reviews) habilitado solo si la estadía terminó (`COMPLETED`).
5. **Verificación:** Un recepcionista finaliza el pago completo, la factura pasa a `PAID` y la interfaz refleja el fin de la estadía permitiendo al usuario dejar una reseña post-checkout.
