package com.etour.service;

import java.util.List;

import com.etour.dto.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse getByBooking(Long bookingId);
    List<InvoiceResponse> getMyInvoices();

    // BRD 3.7 - printable/downloadable PDF receipt for a confirmed booking.
    byte[] getReceiptPdf(Long bookingId);
}
