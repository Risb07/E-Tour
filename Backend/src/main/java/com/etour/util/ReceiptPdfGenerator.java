package com.etour.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.etour.entity.Booking;
import com.etour.entity.Invoice;
import com.etour.entity.Passenger;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * BRD 3.7 / Tour Page - printable PDF receipt for a confirmed booking.
 *
 * <p>The layout mirrors the .NET backend's ReceiptPdfService section for
 * section, because compose advertises that either profile produces the same
 * receipt for the same booking. Changing the design here means changing it
 * there too.
 *
 * <p>Colours are the frontend's "Dusk horizon" palette (see tailwind.config.js)
 * so a printed receipt and the site look like the same company.
 */
@Component
public class ReceiptPdfGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    // Palette, lifted from tailwind.config.js.
    private static final Color INK_800 = new Color(0x18, 0x1A, 0x38);
    private static final Color INK_700 = new Color(0x22, 0x25, 0x4D);
    private static final Color INK_500 = new Color(0x45, 0x4D, 0x85);
    private static final Color INK_100 = new Color(0xE6, 0xE8, 0xF5);
    private static final Color INK_50 = new Color(0xF4, 0xF5, 0xFA);
    private static final Color AMBER_500 = new Color(0xF5, 0x85, 0x0A);
    private static final Color AMBER_50 = new Color(0xFF, 0xF8, 0xEC);
    private static final Color WHITE = Color.WHITE;

    public byte[] generate(Booking booking, Invoice invoice, List<Passenger> passengers) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margins are tighter at the top than the default 36pt so the
            // coloured masthead sits close to the page edge.
            Document doc = new Document(PageSize.A4, 36f, 36f, 28f, 36f);
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(masthead(invoice));
            doc.add(metaStrip(booking, invoice));
            doc.add(detailPanels(booking, passengers));

            if (passengers != null && !passengers.isEmpty()) {
                doc.add(sectionHeading("Passenger details"));
                doc.add(passengerTable(passengers));
            }

            // Right-aligned to sit over the charges table, which is itself
            // right-aligned - a left-hanging heading reads as unrelated to it.
            Paragraph chargesHeading = sectionHeading("Charges");
            chargesHeading.setAlignment(Element.ALIGN_RIGHT);
            doc.add(chargesHeading);
            doc.add(chargesTable(invoice));
            doc.add(footer());

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt PDF: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Sections
    // -----------------------------------------------------------------------

    /** Deep indigo banner: company on the left, receipt title and PAID pill right. */
    private PdfPTable masthead(Invoice invoice) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 60f, 40f });
        header.setSpacingAfter(0f);

        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(INK_800);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(18f);
        left.addElement(text("TourIndia Travels Pvt Ltd", font(17f, true, WHITE)));
        Paragraph tagline = text("Explore the world, one journey at a time", font(8.5f, false, INK_100));
        tagline.setSpacingBefore(3f);
        left.addElement(tagline);
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(INK_800);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(18f);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph kind = text("PAYMENT RECEIPT", font(12f, true, WHITE));
        kind.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(kind);

        // The amber pill is the one piece of accent colour on the page, which
        // is what makes it read as a status rather than decoration.
        PdfPTable pill = new PdfPTable(1);
        pill.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pill.setWidthPercentage(42f);
        PdfPCell pillCell = new PdfPCell(new Phrase("PAID", font(9f, true, INK_800)));
        pillCell.setBackgroundColor(AMBER_500);
        pillCell.setBorder(Rectangle.NO_BORDER);
        pillCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pillCell.setPadding(5f);
        pill.addCell(pillCell);
        pill.setSpacingBefore(8f);
        right.addElement(pill);

        header.addCell(right);
        return header;
    }

    /** Light band directly under the masthead: invoice number, order, date. */
    private PdfPTable metaStrip(Booking booking, Invoice invoice) throws Exception {
        PdfPTable strip = new PdfPTable(3);
        strip.setWidthPercentage(100);
        strip.setWidths(new float[] { 38f, 32f, 30f });
        strip.setSpacingAfter(18f);

        strip.addCell(metaCell("INVOICE NUMBER", invoice.getInvoiceNumber()));
        strip.addCell(metaCell("ORDER NUMBER", nullToDash(booking.getOrderNumber())));
        strip.addCell(metaCell("INVOICE DATE", invoice.getInvoiceDate().format(DATE_FMT)));
        return strip;
    }

    /** Two panels side by side: who it is for, and what was booked. */
    private PdfPTable detailPanels(Booking booking, List<Passenger> passengers) throws Exception {
        PdfPTable panels = new PdfPTable(2);
        panels.setWidthPercentage(100);
        panels.setWidths(new float[] { 50f, 50f });
        panels.setSpacingAfter(20f);

        PdfPCell billed = panelCell();
        billed.addElement(panelTitle("BILLED TO"));
        billed.addElement(panelLine(booking.getCustomer().getFullName(), true));

        // BRD 3.7 - the receipt shows the billing address captured from the
        // primary passenger.
        if (passengers != null && !passengers.isEmpty()) {
            String address = formatAddress(passengers.get(0));
            if (address != null) {
                billed.addElement(panelLine(address, false));
            }
        }
        panels.addCell(billed);

        PdfPCell trip = panelCell();
        trip.addElement(panelTitle("TOUR DETAILS"));
        trip.addElement(panelLine(booking.getSchedule().getTour().getTitle(), true));
        trip.addElement(panelLine("Departure  " + booking.getSchedule().getDepartureDate().format(DATE_FMT)
                + "     Return  " + booking.getSchedule().getReturnDate().format(DATE_FMT), false));
        trip.addElement(panelLine("Passengers  " + booking.getNumberOfPassengers(), false));
        panels.addCell(trip);

        return panels;
    }

    /** Passenger list with an indigo header row and zebra striping. */
    private PdfPTable passengerTable(List<Passenger> passengers) throws Exception {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 42f, 24f, 34f });
        table.setSpacingAfter(20f);

        addHeaderCell(table, "Name");
        addHeaderCell(table, "Gender");
        addHeaderCell(table, "ID Proof");

        boolean shaded = false;
        for (Passenger p : passengers) {
            Color background = shaded ? INK_50 : WHITE;
            table.addCell(bodyCell(nullToDash(p.getFullName()), background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(nullToDash(p.getGender()), background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(nullToDash(p.getIdProofNumber()), background, Element.ALIGN_LEFT));
            shaded = !shaded;
        }
        return table;
    }

    /**
     * Charges, right-aligned and narrower than full width so the eye lands on
     * the total rather than sweeping the page.
     */
    private PdfPTable chargesTable(Invoice invoice) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(58f);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[] { 55f, 45f });

        addChargeRow(table, "Sub-total", currency(invoice.getSubTotal()));
        addChargeRow(table, "Tax", currency(invoice.getTaxAmount()));
        addChargeRow(table, "Discount", currency(invoice.getDiscountAmount()));

        PdfPCell label = new PdfPCell(new Phrase("Total paid", font(11f, true, INK_800)));
        label.setBackgroundColor(AMBER_50);
        label.setBorder(Rectangle.TOP);
        label.setBorderColor(AMBER_500);
        label.setBorderWidthTop(1.2f);
        label.setPadding(9f);
        table.addCell(label);

        PdfPCell value = new PdfPCell(new Phrase(currency(invoice.getTotalAmount()), font(11f, true, INK_800)));
        value.setBackgroundColor(AMBER_50);
        value.setBorder(Rectangle.TOP);
        value.setBorderColor(AMBER_500);
        value.setBorderWidthTop(1.2f);
        value.setPadding(9f);
        value.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(value);

        return table;
    }

    /** Hairline rule, then the closing note. */
    private PdfPTable footer() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(34f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(INK_100);
        cell.setBorderWidthTop(1f);
        cell.setPadding(0f);
        cell.setPaddingTop(10f);

        Paragraph thanks = text("Thank you for booking with TourIndia Travels.", font(9.5f, true, INK_700));
        thanks.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(thanks);

        Paragraph note = text("This is a system-generated receipt and does not require a signature.",
                font(8f, false, INK_500));
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(3f);
        cell.addElement(note);

        table.addCell(cell);
        return table;
    }

    // -----------------------------------------------------------------------
    // Building blocks
    // -----------------------------------------------------------------------

    private Paragraph sectionHeading(String label) {
        Paragraph heading = text(label.toUpperCase(), font(9.5f, true, INK_500));
        heading.setSpacingAfter(7f);
        return heading;
    }

    private PdfPCell metaCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(INK_50);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);
        cell.addElement(text(label, font(7.5f, true, INK_500)));
        Paragraph valueLine = text(value, font(11f, true, INK_800));
        valueLine.setSpacingBefore(2f);
        cell.addElement(valueLine);
        return cell;
    }

    private PdfPCell panelCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(INK_100);
        cell.setBorderWidth(1f);
        cell.setPadding(12f);
        return cell;
    }

    private Paragraph panelTitle(String label) {
        Paragraph title = text(label, font(7.5f, true, INK_500));
        title.setSpacingAfter(5f);
        return title;
    }

    private Paragraph panelLine(String value, boolean emphasised) {
        Paragraph line = text(value, emphasised ? font(10.5f, true, INK_800) : font(9f, false, INK_700));
        line.setSpacingBefore(emphasised ? 0f : 3f);
        line.setLeading(12f);
        return line;
    }

    private void addHeaderCell(PdfPTable table, String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, font(9f, true, WHITE)));
        cell.setBackgroundColor(INK_700);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String value, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font(9.5f, false, INK_800)));
        cell.setBackgroundColor(background);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(INK_100);
        cell.setBorderWidthBottom(0.7f);
        cell.setPadding(7f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void addChargeRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font(9.5f, false, INK_700)));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(INK_100);
        labelCell.setBorderWidthBottom(0.7f);
        labelCell.setPadding(7f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font(9.5f, false, INK_800)));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(INK_100);
        valueCell.setBorderWidthBottom(0.7f);
        valueCell.setPadding(7f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private Paragraph text(String value, Font font) {
        return new Paragraph(value, font);
    }

    private Font font(float size, boolean bold, Color color) {
        return FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size,
                Font.NORMAL, color);
    }

    /**
     * "INR" rather than the rupee sign on purpose: Helvetica's WinAnsi encoding
     * has no glyph for U+20B9, so the symbol would silently render as nothing.
     */
    private String currency(java.math.BigDecimal amount) {
        // Locale.ROOT, not the default: grouping style follows the JVM's locale
        // otherwise, so the same booking would print 184,500.00 on one host and
        // 1,84,500.00 on another - and would stop matching the .NET backend,
        // which pins the invariant culture for exactly this reason.
        return amount == null ? "-" : String.format(java.util.Locale.ROOT, "INR %,.2f", amount);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    /** Joins whichever address parts are present; null if none are. */
    private String formatAddress(Passenger p) {
        String joined = java.util.stream.Stream
                .of(p.getAddressLine1(), p.getAddressLine2(), p.getCity(), p.getState(),
                        p.getCountry(), p.getPincode())
                .filter(part -> part != null && !part.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
        return joined.isBlank() ? null : joined;
    }
}
