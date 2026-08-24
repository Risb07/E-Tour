package com.etour.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.etour.dto.InvoiceResponse;
import com.etour.entity.Booking;
import com.etour.entity.Customer;
import com.etour.entity.Invoice;
import com.etour.entity.Passenger;
import com.etour.entity.Payment;
import com.etour.enums.InvoiceStatus;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.InvoiceRepository;
import com.etour.repository.PassengerRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.InvoiceService;
import com.etour.util.ReceiptPdfGenerator;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    // GST rate as a decimal fraction (0.05 = 5%). Configurable via the
    // app.invoice.gst-rate property / INVOICE_GST_RATE env var so a rate
    // change is a config change, not a redeploy of new code.
    private final BigDecimal taxRate;

    private final InvoiceRepository invoiceRepository;
    private final PassengerRepository passengerRepository;
    private final ReceiptPdfGenerator receiptPdfGenerator;
    private final CurrentUserProvider currentUserProvider;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, PassengerRepository passengerRepository,
            ReceiptPdfGenerator receiptPdfGenerator, CurrentUserProvider currentUserProvider,
            @Value("${app.invoice.gst-rate:0.05}") BigDecimal taxRate) {
        this.invoiceRepository = invoiceRepository;
        this.passengerRepository = passengerRepository;
        this.receiptPdfGenerator = receiptPdfGenerator;
        this.currentUserProvider = currentUserProvider;
        this.taxRate = taxRate;
    }

    /** Called by PaymentServiceImpl right after a payment succeeds. */
    public Invoice generate(Booking booking, Payment payment) {
        BigDecimal subTotal = booking.getTotalAmount();
        BigDecimal tax = subTotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subTotal.add(tax);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + booking.getBookingId() + "-" + System.currentTimeMillis());
        invoice.setBooking(booking);
        invoice.setPayment(payment);
        invoice.setCustomer(booking.getCustomer());
        invoice.setSubTotal(subTotal);
        invoice.setTaxAmount(tax);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(total);
        invoice.setInvoiceStatus(InvoiceStatus.GENERATED);

        return invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceResponse getByBooking(Long bookingId) {
        Invoice invoice = invoiceRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No invoice for this booking"));

        if (!currentUserProvider.isAdmin()) {
            Customer customer = currentUserProvider.currentCustomer();
            if (!invoice.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new ResourceNotFoundException("No invoice for this booking");
            }
        }
        return toDto(invoice);
    }

    @Override
    public List<InvoiceResponse> getMyInvoices() {
        Customer customer = currentUserProvider.currentCustomer();
        return invoiceRepository.findByCustomer_CustomerId(customer.getCustomerId()).stream()
                .map(this::toDto).toList();
    }

    @Override
    public byte[] getReceiptPdf(Long bookingId) {
        Invoice invoice = invoiceRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No invoice for this booking"));

        if (!currentUserProvider.isAdmin()) {
            Customer customer = currentUserProvider.currentCustomer();
            if (!invoice.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new ResourceNotFoundException("No invoice for this booking");
            }
        }

        List<Passenger> passengers = passengerRepository.findByBooking_BookingId(bookingId);
        return receiptPdfGenerator.generate(invoice.getBooking(), invoice, passengers);
    }

    private InvoiceResponse toDto(Invoice i) {
        return new InvoiceResponse(
                i.getInvoiceId(), i.getInvoiceNumber(), i.getBooking().getBookingId(),
                i.getPayment().getPaymentId(), i.getInvoiceDate(), i.getSubTotal(), i.getTaxAmount(),
                i.getDiscountAmount(), i.getTotalAmount(), i.getInvoiceStatus().name());
    }
}
