using System.Text;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Infrastructure.Receipts;
using FluentAssertions;
using Microsoft.Extensions.Logging.Abstractions;
using Moq;
using NUnit.Framework;

namespace eTour.Tests.Services;

/// <summary>
/// The receipt e-mail now goes to the notification microservice instead of an SMTP server.
///
/// <para>The thing worth pinning is the idempotency key. It is what guarantees a customer gets
/// exactly ONE e-mail per booking: if this ever stops being derived from the invoice number, a
/// retried enqueue silently becomes a second e-mail, and nothing else in the system would notice.</para>
/// </summary>
[TestFixture]
public class ReceiptEmailServiceTests
{
    private Mock<INotificationClient> _client = null!;
    private ReceiptEmailService _service = null!;

    [SetUp]
    public void SetUp()
    {
        _client = new Mock<INotificationClient>();
        _client.Setup(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        _service = new ReceiptEmailService(_client.Object, NullLogger<ReceiptEmailService>.Instance);
    }

    private static ReceiptData Receipt(string email = "traveller@example.com") => new()
    {
        InvoiceNumber = "INV-1001",
        OrderNumber = "ORD-1001",
        CustomerFullName = "Grace Hopper",
        CustomerEmail = email,
        TourTitle = "The Rajasthan Edit",
        TotalAmount = 77800m
    };

    private static byte[] Pdf() => Encoding.UTF8.GetBytes("%PDF-1.4 fake");

    [Test]
    public async Task SendReceiptEmail_KeysOnTheInvoiceNumber_SoARetryCannotDoubleSend()
    {
        await _service.SendReceiptEmailAsync(Receipt(), Pdf());

        _client.Verify(c => c.EnqueueAsync(
            "traveller@example.com",
            "BOOKING_RECEIPT",
            It.IsAny<IReadOnlyDictionary<string, string>>(),
            "receipt-INV-1001",
            It.IsAny<IReadOnlyList<NotificationAttachment>>(),
            It.IsAny<CancellationToken>()), Times.Once);
    }

    [Test]
    public async Task SendReceiptEmail_CallingTwiceUsesTheSameKey_WhichTheServiceCollapses()
    {
        // The backend does not itself de-duplicate; it relies on the key being stable. This asserts
        // the half the backend is responsible for.
        await _service.SendReceiptEmailAsync(Receipt(), Pdf());
        await _service.SendReceiptEmailAsync(Receipt(), Pdf());

        _client.Verify(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
            It.IsAny<IReadOnlyDictionary<string, string>>(), "receipt-INV-1001",
            It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Test]
    public async Task SendReceiptEmail_AttachesThePdfAsBase64()
    {
        IReadOnlyList<NotificationAttachment>? captured = null;
        _client.Setup(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()))
            .Callback<string, string, IReadOnlyDictionary<string, string>, string, IReadOnlyList<NotificationAttachment>?, CancellationToken>(
                (_, _, _, _, a, _) => captured = a)
            .ReturnsAsync(true);

        await _service.SendReceiptEmailAsync(Receipt(), Pdf());

        captured.Should().HaveCount(1);
        captured![0].Filename.Should().Be("receipt-INV-1001.pdf");
        captured[0].ContentType.Should().Be("application/pdf");
        Convert.FromBase64String(captured[0].ContentBase64).Should().Equal(Pdf());
    }

    [Test]
    public async Task SendReceiptEmail_PassesTheVariablesTheTemplateNeeds()
    {
        IReadOnlyDictionary<string, string>? captured = null;
        _client.Setup(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()))
            .Callback<string, string, IReadOnlyDictionary<string, string>, string, IReadOnlyList<NotificationAttachment>?, CancellationToken>(
                (_, _, v, _, _, _) => captured = v)
            .ReturnsAsync(true);

        await _service.SendReceiptEmailAsync(Receipt(), Pdf());

        // Every placeholder in the BOOKING_RECEIPT template; a missing one renders blank in a
        // customer-facing e-mail rather than failing loudly, so it has to be checked here.
        captured.Should().Contain(new KeyValuePair<string, string>("customerName", "Grace Hopper"));
        captured.Should().Contain(new KeyValuePair<string, string>("tourTitle", "The Rajasthan Edit"));
        captured.Should().Contain(new KeyValuePair<string, string>("orderNumber", "ORD-1001"));
        captured.Should().Contain(new KeyValuePair<string, string>("invoiceNumber", "INV-1001"));
    }

    [Test]
    public async Task SendReceiptEmail_WithNoCustomerEmail_QueuesNothing()
    {
        await _service.SendReceiptEmailAsync(Receipt(email: ""), Pdf());

        _client.VerifyNoOtherCalls();
    }

    [Test]
    public async Task SendReceiptEmail_WhenTheServiceIsDown_DoesNotThrow()
    {
        // A payment has already been taken by this point. Throwing here would fail the request for a
        // customer whose money has moved.
        _client.Setup(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(false);

        var act = async () => await _service.SendReceiptEmailAsync(Receipt(), Pdf());

        await act.Should().NotThrowAsync();
    }

    [Test]
    public async Task SendReceiptEmail_WithNoPdf_StillQueuesTheConfirmation()
    {
        // A receipt that failed to render must not cost the customer their confirmation e-mail -
        // they can download the PDF from their dashboard either way.
        IReadOnlyList<NotificationAttachment>? captured = null;
        _client.Setup(c => c.EnqueueAsync(It.IsAny<string>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
                It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()))
            .Callback<string, string, IReadOnlyDictionary<string, string>, string, IReadOnlyList<NotificationAttachment>?, CancellationToken>(
                (_, _, _, _, a, _) => captured = a)
            .ReturnsAsync(true);

        await _service.SendReceiptEmailAsync(Receipt(), []);

        captured.Should().BeEmpty();
        _client.Verify(c => c.EnqueueAsync(It.IsAny<string>(), "BOOKING_RECEIPT",
            It.IsAny<IReadOnlyDictionary<string, string>>(), It.IsAny<string>(),
            It.IsAny<IReadOnlyList<NotificationAttachment>>(), It.IsAny<CancellationToken>()), Times.Once);
    }
}
