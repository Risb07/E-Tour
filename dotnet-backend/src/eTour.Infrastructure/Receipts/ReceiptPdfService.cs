using eTour.Application.Dtos;
using eTour.Application.Services;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace eTour.Infrastructure.Receipts;

/// <summary>
/// BRD 3.7 / Tour Page - printable PDF receipt for a confirmed booking.
///
/// <para>The layout mirrors the Java backend's ReceiptPdfGenerator section for
/// section, because compose advertises that either profile produces the same
/// receipt for the same booking. Changing the design here means changing it
/// there too.</para>
///
/// <para>Colours are the frontend's "Dusk horizon" palette (see
/// tailwind.config.js) so a printed receipt and the site look like the same
/// company.</para>
/// </summary>
public class ReceiptPdfService : IReceiptPdfService
{
    // Palette, lifted from tailwind.config.js.
    private const string Ink800 = "#181a38";
    private const string Ink700 = "#22254d";
    private const string Ink500 = "#454d85";
    private const string Ink100 = "#e6e8f5";
    private const string Ink50 = "#f4f5fa";
    private const string Amber500 = "#f5850a";
    private const string Amber50 = "#fff8ec";
    private const string White = "#ffffff";

    static ReceiptPdfService()
    {
        QuestPDF.Settings.License = LicenseType.Community;
    }

    public byte[] Generate(ReceiptData data)
    {
        var document = Document.Create(container =>
        {
            container.Page(page =>
            {
                // No page margin: the masthead runs to the paper edge. Every
                // section below applies its own horizontal padding instead.
                page.Margin(0);
                page.Size(PageSizes.A4);
                page.DefaultTextStyle(x => x.FontSize(10).FontColor(Ink800));

                page.Content().Column(column =>
                {
                    Masthead(column);
                    MetaStrip(column, data);
                    DetailPanels(column, data);

                    if (data.Passengers.Count > 0)
                    {
                        column.Item().PaddingHorizontal(30).Element(c => SectionHeading(c, "Passenger details"));
                        PassengerTable(column, data);
                    }

                    // Right-aligned to sit over the charges table, which is
                    // itself right-aligned - a left-hanging heading reads as
                    // unrelated to it.
                    column.Item().PaddingHorizontal(30).AlignRight()
                        .Element(c => SectionHeading(c, "Charges"));
                    ChargesTable(column, data);

                    Footer(column);
                });
            });
        });

        return document.GeneratePdf();

        // -------------------------------------------------------------------
        // Sections
        // -------------------------------------------------------------------

        //Deep indigo banner: company on the left, receipt title and PAID pill right.
        void Masthead(ColumnDescriptor column)
        {
            column.Item().Background(Ink800).Padding(24).Row(row =>
            {
                row.RelativeItem(60).Column(left =>
                {
                    left.Item().Text("TourIndia Travels Pvt Ltd")
                        .FontSize(17).Bold().FontColor(White);
                    left.Item().PaddingTop(3).Text("Explore the world, one journey at a time")
                        .FontSize(8.5f).FontColor(Ink100);
                });

                row.RelativeItem(40).Column(right =>
                {
                    right.Item().AlignRight().Text("PAYMENT RECEIPT")
                        .FontSize(12).Bold().FontColor(White);

                    // The amber pill is the one piece of accent colour on the
                    // page, which is what makes it read as a status rather
                    // than decoration.
                    right.Item().PaddingTop(8).AlignRight()
                        .Background(Amber500).PaddingVertical(5).PaddingHorizontal(18)
                        .Text("PAID").FontSize(9).Bold().FontColor(Ink800);
                });
            });
        }

        //Light band directly under the masthead: invoice number, order, date.
        void MetaStrip(ColumnDescriptor column, ReceiptData d)
        {
            column.Item().Background(Ink50).PaddingHorizontal(30).PaddingVertical(14).Row(row =>
            {
                MetaCell(row.RelativeItem(38), "INVOICE NUMBER", d.InvoiceNumber);
                MetaCell(row.RelativeItem(32), "ORDER NUMBER", NullToDash(d.OrderNumber));
                MetaCell(row.RelativeItem(30), "INVOICE DATE", $"{d.InvoiceDate:dd-MMM-yyyy}");
            });
        }

        //Two panels side by side: who it is for, and what was booked.
        void DetailPanels(ColumnDescriptor column, ReceiptData d)
        {
            column.Item().PaddingHorizontal(30).PaddingTop(18).PaddingBottom(20).Row(row =>
            {
                row.RelativeItem().Border(1).BorderColor(Ink100).Padding(12).Column(billed =>
                {
                    billed.Item().Element(c => PanelTitle(c, "BILLED TO"));
                    billed.Item().Text(d.CustomerFullName).FontSize(10.5f).Bold();

                    // BRD 3.7 - the receipt shows the billing address captured
                    // from the primary passenger.
                    if (d.Passengers.Count > 0)
                    {
                        var address = FormatAddress(d.Passengers[0]);
                        if (address is not null)
                        {
                            billed.Item().PaddingTop(3).Text(address)
                                .FontSize(9).FontColor(Ink700).LineHeight(1.3f);
                        }
                    }
                });

                row.ConstantItem(14);

                row.RelativeItem().Border(1).BorderColor(Ink100).Padding(12).Column(trip =>
                {
                    trip.Item().Element(c => PanelTitle(c, "TOUR DETAILS"));
                    trip.Item().Text(d.TourTitle).FontSize(10.5f).Bold().LineHeight(1.3f);
                    trip.Item().PaddingTop(3)
                        .Text($"Departure  {d.DepartureDate:dd-MMM-yyyy}     Return  {d.ReturnDate:dd-MMM-yyyy}")
                        .FontSize(9).FontColor(Ink700);
                    trip.Item().PaddingTop(3).Text($"Passengers  {d.NumberOfPassengers}")
                        .FontSize(9).FontColor(Ink700);
                });
            });
        }

        //Passenger list with an indigo header row and zebra striping.
        void PassengerTable(ColumnDescriptor column, ReceiptData d)
        {
            column.Item().PaddingHorizontal(30).PaddingBottom(20).Table(table =>
            {
                table.ColumnsDefinition(cols =>
                {
                    cols.RelativeColumn(42);
                    cols.RelativeColumn(24);
                    cols.RelativeColumn(34);
                });

                table.Header(header =>
                {
                    HeaderCell(header.Cell(), "Name");
                    HeaderCell(header.Cell(), "Gender");
                    HeaderCell(header.Cell(), "ID Proof");
                });

                var shaded = false;
                foreach (var p in d.Passengers)
                {
                    var background = shaded ? Ink50 : White;
                    BodyCell(table.Cell(), NullToDash(p.FullName), background);
                    BodyCell(table.Cell(), NullToDash(p.Gender), background);
                    BodyCell(table.Cell(), NullToDash(p.IdProofNumber), background);
                    shaded = !shaded;
                }
            });
        }

        //Charges, right-aligned and narrower than full width so the eye lands
        //on the total rather than sweeping the page.
        void ChargesTable(ColumnDescriptor column, ReceiptData d)
        {
            column.Item().PaddingHorizontal(30).Row(row =>
            {
                row.RelativeItem(42);
                row.RelativeItem(58).Table(table =>
                {
                    table.ColumnsDefinition(cols =>
                    {
                        cols.RelativeColumn(55);
                        cols.RelativeColumn(45);
                    });

                    ChargeRow(table, "Sub-total", Currency(d.SubTotal));
                    ChargeRow(table, "Tax", Currency(d.TaxAmount));
                    ChargeRow(table, "Discount", Currency(d.DiscountAmount));

                    table.Cell().Background(Amber50).BorderTop(1.2f).BorderColor(Amber500)
                        .Padding(9).Text("Total paid").FontSize(11).Bold();
                    table.Cell().Background(Amber50).BorderTop(1.2f).BorderColor(Amber500)
                        .Padding(9).AlignRight().Text(Currency(d.TotalAmount)).FontSize(11).Bold();
                });
            });
        }

        //Hairline rule, then the closing note.
        void Footer(ColumnDescriptor column)
        {
            column.Item().PaddingHorizontal(30).PaddingTop(34).BorderTop(1).BorderColor(Ink100)
                .PaddingTop(10).Column(footer =>
                {
                    footer.Item().AlignCenter().Text("Thank you for booking with TourIndia Travels.")
                        .FontSize(9.5f).Bold().FontColor(Ink700);
                    footer.Item().PaddingTop(3).AlignCenter()
                        .Text("This is a system-generated receipt and does not require a signature.")
                        .FontSize(8).FontColor(Ink500);
                });
        }

        // -------------------------------------------------------------------
        // Building blocks
        // -------------------------------------------------------------------

        static void SectionHeading(IContainer container, string label) =>
            container.PaddingBottom(7).Text(label.ToUpperInvariant())
                .FontSize(9.5f).Bold().FontColor(Ink500);

        static void MetaCell(IContainer container, string label, string value) =>
            container.Column(cell =>
            {
                cell.Item().Text(label).FontSize(7.5f).Bold().FontColor(Ink500);
                cell.Item().PaddingTop(2).Text(value).FontSize(11).Bold();
            });

        static void PanelTitle(IContainer container, string label) =>
            container.PaddingBottom(5).Text(label).FontSize(7.5f).Bold().FontColor(Ink500);

        static void HeaderCell(IContainer container, string label) =>
            container.Background(Ink700).Padding(8).Text(label).FontSize(9).Bold().FontColor(White);

        static void BodyCell(IContainer container, string value, string background) =>
            container.Background(background).BorderBottom(0.7f).BorderColor(Ink100)
                .Padding(7).Text(value).FontSize(9.5f);

        static void ChargeRow(TableDescriptor table, string label, string value)
        {
            table.Cell().BorderBottom(0.7f).BorderColor(Ink100).Padding(7)
                .Text(label).FontSize(9.5f).FontColor(Ink700);
            table.Cell().BorderBottom(0.7f).BorderColor(Ink100).Padding(7).AlignRight()
                .Text(value).FontSize(9.5f);
        }
    }

    /// <summary>
    /// "INR" rather than the rupee sign on purpose: the Java backend renders
    /// with Helvetica, whose WinAnsi encoding has no glyph for U+20B9, and the
    /// two receipts are meant to be identical.
    /// </summary>
    /// <remarks>
    /// InvariantCulture, not the ambient one: N2 groups by the host's culture
    /// otherwise, so an en-IN machine prints 1,84,500.00 while en-US prints
    /// 184,500.00 for the same booking - and the Java backend, which pins
    /// Locale.ROOT, would no longer match.
    /// </remarks>
    private static string Currency(decimal amount) =>
        $"INR {amount.ToString("N2", System.Globalization.CultureInfo.InvariantCulture)}";

    private static string NullToDash(string? value) => string.IsNullOrWhiteSpace(value) ? "-" : value;

    /// <summary>Joins whichever address parts are present; null if none are.</summary>
    private static string? FormatAddress(ReceiptPassengerLine p)
    {
        var parts = new[] { p.AddressLine1, p.AddressLine2, p.City, p.State, p.Country, p.Pincode }
            .Where(part => !string.IsNullOrWhiteSpace(part));
        var joined = string.Join(", ", parts);
        return string.IsNullOrWhiteSpace(joined) ? null : joined;
    }
}
