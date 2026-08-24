using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.Extensions.Logging;

namespace eTour.Infrastructure.Receipts;

/// <summary>
/// BRD 3.7 - "receipt will be displayed and e-mailed to user".
///
/// <para>This no longer sends mail. It hands the rendered receipt to the notification
/// microservice, which owns the queue, the single retry and the delivery log. The
/// customer-visible result is unchanged: same subject, same wording, same attached PDF - the
/// template on the service side is a verbatim copy of the text this class used to build.</para>
///
/// <para><b>Why exactly one e-mail per booking.</b> Three things have to hold:</para>
/// <list type="number">
///   <item><description><b>One payment per booking.</b> PaymentService loads the booking under a
///   lock and refuses a second payment once it is CONFIRMED, so this is reached once per
///   booking.</description></item>
///   <item><description><b>Never before the payment is persisted.</b> The call site sits after the
///   payment, booking and invoice have been saved - GenericRepository commits on every write and
///   PaymentService opens no wrapping transaction, so by the time this runs the money movement is
///   already durable.</description></item>
///   <item><description><b>Never twice if this runs twice.</b> The idempotency key is derived from
///   the invoice number, so the notification service collapses a repeat enqueue onto the original
///   message instead of queueing another.</description></item>
/// </list>
///
/// <para>Failures are logged, never thrown: a queue being unreachable is not a reason to undo a
/// payment that already succeeded, and the receipt stays available from
/// <c>GET /api/invoices/booking/{id}/receipt</c>.</para>
/// </summary>
public class ReceiptEmailService : IReceiptEmailService
{
    /// <summary>Seeded by the notification service; wording matches the old inline e-mail.</summary>
    private const string Template = "BOOKING_RECEIPT";

    private readonly INotificationClient _notificationClient;
    private readonly ILogger<ReceiptEmailService> _logger;

    public ReceiptEmailService(INotificationClient notificationClient, ILogger<ReceiptEmailService> logger)
    {
        _notificationClient = notificationClient;
        _logger = logger;
    }

    public async Task SendReceiptEmailAsync(ReceiptData data, byte[] pdf, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(data.CustomerEmail))
        {
            _logger.LogWarning("Skipping receipt e-mail for invoice {InvoiceNumber} - customer has no e-mail on file",
                data.InvoiceNumber);
            return;
        }

        var variables = new Dictionary<string, string>
        {
            ["customerName"] = data.CustomerFullName ?? "",
            ["tourTitle"] = data.TourTitle ?? "",
            ["orderNumber"] = data.OrderNumber ?? "",
            ["invoiceNumber"] = data.InvoiceNumber ?? ""
        };

        // An empty list rather than a missing attachment: a receipt we could not
        // render must not stop the confirmation e-mail, since the customer can
        // still download it on demand.
        IReadOnlyList<NotificationAttachment> attachments = pdf is { Length: > 0 }
            ? [new NotificationAttachment($"receipt-{data.InvoiceNumber}.pdf", "application/pdf", Convert.ToBase64String(pdf))]
            : [];

        await _notificationClient.EnqueueAsync(
            data.CustomerEmail,
            Template,
            variables,
            $"receipt-{data.InvoiceNumber}",
            attachments,
            ct);
    }
}
