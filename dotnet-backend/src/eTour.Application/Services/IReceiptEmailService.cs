using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IReceiptEmailService
{
    /// <summary>Best-effort - never throws. A broken mail server must not undo a successful payment.</summary>
    Task SendReceiptEmailAsync(ReceiptData data, byte[] pdf, CancellationToken ct = default);
}
