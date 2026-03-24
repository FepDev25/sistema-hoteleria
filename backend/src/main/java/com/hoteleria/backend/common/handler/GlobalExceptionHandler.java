package com.hoteleria.backend.common.handler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hoteleria.backend.common.api.ErrorResponse;
import com.hoteleria.backend.common.exception.BusinessException;
import com.hoteleria.backend.security.exception.AccountLockedException;

import jakarta.validation.ConstraintViolationException;

// anotacion para manejo global de excepciones en controladores
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // asegurar que este handler se ejecute antes que otros posibles handlers mas especificos
public class GlobalExceptionHandler {

    // manejo de excepciones personalizadas de negocio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return buildResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), null);
    }

    // manejo de excepciones de integridad de datos, como violaciones de constraints unicos o checks
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String rawMessage = extractMostSpecificMessage(ex);

        // detectar violaciones de constraints unicos o checks especificos por palabras clave en el mensaje de error
        if (containsAny(rawMessage, "uq_guest_document", "guests_document_type_document_number_key")) {
            return buildResponse(HttpStatus.CONFLICT, "GUEST_DOCUMENT_ALREADY_EXISTS", "Documento de huesped ya registrado", null);
        }
        if (containsAny(rawMessage, "guests_email_key")) {
            return buildResponse(HttpStatus.CONFLICT, "GUEST_EMAIL_ALREADY_EXISTS", "Email de huesped ya registrado", null);
        }
        if (containsAny(rawMessage, "uq_room_number_per_hotel")) {
            return buildResponse(HttpStatus.CONFLICT, "ROOM_NUMBER_ALREADY_EXISTS", "El numero de habitacion ya existe para este hotel", null);
        }
        if (containsAny(rawMessage, "amenities_name_key")) {
            return buildResponse(HttpStatus.CONFLICT, "AMENITY_NAME_ALREADY_EXISTS", "La amenidad ya existe", null);
        }
        if (containsAny(rawMessage, "uq_service_name_per_hotel")) {
            return buildResponse(HttpStatus.CONFLICT, "SERVICE_NAME_ALREADY_EXISTS", "El nombre del servicio ya existe para este hotel", null);
        }
        if (containsAny(rawMessage, "uq_room_type_name_per_hotel")) {
            return buildResponse(HttpStatus.CONFLICT, "ROOM_TYPE_NAME_ALREADY_EXISTS", "El nombre del tipo de habitacion ya existe para este hotel", null);
        }
        if (containsAny(rawMessage, "reviews_reservation_id_key")) {
            return buildResponse(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS", "Ya existe una resena para la reserva", null);
        }
        if (containsAny(rawMessage, "invoices_reservation_id_key")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVOICE_ALREADY_EXISTS", "La reserva ya tiene factura", null);
        }
        if (containsAny(rawMessage, "chk_cancellation_fields")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "RESERVATION_CANCELLATION_FIELDS_REQUIRED", "Reserva cancelada requiere cancelledAt y cancellationReason", null);
        }
        if (containsAny(rawMessage, "chk_invoice_issued_at")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVOICE_ISSUED_AT_REQUIRED", "Una factura no DRAFT requiere issuedAt", null);
        }
        if (containsAny(rawMessage, "chk_invoice_paid_at")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVOICE_PAID_AT_REQUIRED", "Una factura PAID requiere paidAt", null);
        }
        if (containsAny(rawMessage, "chk_payment_paid_at")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "PAYMENT_PAID_AT_REQUIRED", "Un pago COMPLETED requiere paidAt", null);
        }
        if (containsAny(rawMessage, "chk_invoice_total")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVOICE_TOTAL_INVALID", "El total de factura no coincide con subtotal + impuestos", null);
        }
        if (containsAny(rawMessage, "chk_reservation_dates")) {
            return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_DATES", "La fecha de salida debe ser posterior a la fecha de entrada", null);
        }
        if (containsAny(rawMessage, "chk_maintenance_resolved")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "MAINTENANCE_RESOLVED_AT_REQUIRED", "Estado RESOLVED/CLOSED requiere resolvedAt", null);
        }
        if (containsAny(rawMessage, "trg_check_room_availability", "ya tiene una reserva activa")) {
            return buildResponse(HttpStatus.CONFLICT, "ROOM_NOT_AVAILABLE", "Una o mas habitaciones no estan disponibles para el rango seleccionado", null);
        }
        if (containsAny(rawMessage, "trg_check_review_eligibility", "Solo se puede dejar una reseña", "Solo se puede dejar una resena")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "REVIEW_RESERVATION_NOT_COMPLETED", "Solo se puede reseñar una reserva COMPLETED", null);
        }
        if (containsAny(rawMessage, "El huésped no coincide", "El huesped no coincide")) {
            return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "REVIEW_GUEST_MISMATCH", "El huesped no coincide con la reserva", null);
        }

        String safeDetail = sanitizeDbError(rawMessage);

        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "Conflicto de integridad de datos",
                safeDetail == null ? null : List.of(safeDetail)
        );
    }

    // manejo de errores de validacion de argumentos en controladores
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Error de validacion", details);
    }

    // manejo de errores de validacion de constraints en entidades o parametros simples
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Error de validacion", details);
    }

    // manejo de errores por falta de parametros requeridos en solicitudes
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_REQUEST_PARAMETER",
                "Falta un parametro requerido",
                List.of(ex.getParameterName() + " es requerido")
        );
    }

    // manejo de errores por cuerpo de solicitud mal formado o JSON invalido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_JSON",
                "El cuerpo de la solicitud no es JSON valido",
                null
        );
    }

    // manejo de errores por uso de metodos HTTP no soportados en endpoints
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        List<String> details = new ArrayList<>();
        if (ex.getSupportedMethods() != null && ex.getSupportedMethods().length > 0) {
            details.add("Metodos soportados: " + String.join(",", ex.getSupportedMethods()));
        }

        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "Metodo HTTP no soportado para este recurso",
                details.isEmpty() ? null : details
        );
    }

    // manejo de errores por uso de Content-Type no soportado en solicitudes
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type no soportado",
                null
        );
    }

    // manejo de errores de autenticacion y autorizacion en seguridad
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        if (ex instanceof AccountLockedException accountLockedException) {
            String detail = "Account locked. Try again in " + accountLockedException.getMinutesRemaining() + " minutes";
            return buildResponse(
                    HttpStatus.LOCKED,
                    "ACCOUNT_LOCKED",
                    "Cuenta bloqueada temporalmente",
                    List.of(detail)
            );
        }
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "No autenticado o token invalido",
                null
        );
    }

    // manejo de errores por falta de permisos para acceder a recursos protegidos
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "No tienes permisos para acceder a este recurso",
                null
        );
    }

    // manejo de cualquier otra excepcion no controlada para evitar fugas de informacion sensible y asegurar respuestas consistentes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Error interno del servidor",
                null
        );
    }

    private String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() == null ? "invalido" : fieldError.getDefaultMessage();
        return fieldError.getField() + ": " + message;
    }

    // metodo utilitario para construir respuestas de error de forma consistente
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            List<String> details
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(code, message, details, OffsetDateTime.now())
        );
    }

    private boolean containsAny(String message, String... needles) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        for (String needle : needles) {
            if (lower.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String extractMostSpecificMessage(Throwable ex) {
        Throwable mostSpecific = ex;
        while (mostSpecific.getCause() != null) {
            mostSpecific = mostSpecific.getCause();
        }
        return mostSpecific.getMessage() == null ? ex.getMessage() : mostSpecific.getMessage();
    }

    private String sanitizeDbError(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String normalized = message.replace('\n', ' ').replace('\r', ' ').trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
    }
}
