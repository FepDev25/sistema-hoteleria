package com.hoteleria.backend.billing.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoteleria.backend.billing.api.CreateInvoiceRequest;
import com.hoteleria.backend.billing.api.InvoiceResponse;
import com.hoteleria.backend.billing.service.InvoiceService;
import com.hoteleria.backend.operations.api.CreatePaymentRequest;
import com.hoteleria.backend.operations.api.PaymentResponse;
import com.hoteleria.backend.operations.api.RefundRequest;
import com.hoteleria.backend.operations.service.PaymentService;

import jakarta.validation.Valid;

// controlador para manejar las solicitudes HTTP relacionadas con las facturas
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @PostMapping("/{id}/issue")
    public InvoiceResponse issueInvoice(@PathVariable UUID id) {
        return invoiceService.issueInvoice(id);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("@hotelTenantGuard.canAccessGuestOwned(authentication, @resourceOwnershipQuery.hotelIdByInvoiceId(#id), @resourceOwnershipQuery.guestIdByInvoiceId(#id))")
    public PaymentResponse payInvoice(@PathVariable UUID id, @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.payInvoice(id, request);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refundInvoice(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        return paymentService.refundInvoice(id, request);
    }
}
