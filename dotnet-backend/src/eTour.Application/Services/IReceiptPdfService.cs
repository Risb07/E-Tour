using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IReceiptPdfService
{
    byte[] Generate(ReceiptData data);
}
