package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.InvoiceResponse;
import com.etour.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<InvoiceResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(invoiceService.getByBooking(bookingId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices() {
        return ResponseEntity.ok(invoiceService.getMyInvoices());
    }

    // BRD 3.7 - printable/downloadable PDF receipt.
    @GetMapping("/booking/{bookingId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long bookingId) {
        byte[] pdf = invoiceService.getReceiptPdf(bookingId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"receipt-" + bookingId + ".pdf\"")
                .body(pdf);
    }
}
