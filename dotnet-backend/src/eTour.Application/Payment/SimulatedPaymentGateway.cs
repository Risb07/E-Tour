using Microsoft.Extensions.Logging;

namespace eTour.Application.Gateways;

/// <summary>
/// Default gateway: always succeeds, no money moves. Active whenever Payment:Provider is unset
/// or "simulated" (see InfrastructureServiceRegistration), so a real provider can replace it
/// without touching PaymentService.
/// </summary>
public class SimulatedPaymentGateway : IPaymentGateway
{
    // Test cards so the failure paths can actually be exercised without a real provider.
    private const string DeclineCardPrefix = "4000000000000002";
    private const string InsufficientFundsPrefix = "4000000000009995";

    private readonly ILogger<SimulatedPaymentGateway> _logger;

    public SimulatedPaymentGateway(ILogger<SimulatedPaymentGateway> logger)
    {
        _logger = logger;
    }

    public string ProviderName => "SIMULATED";

    public GatewayResult Charge(GatewayChargeRequest request)
    {
        if (request.Amount <= 0)
        {
            return GatewayResult.Failed(null, "Amount must be greater than zero");
        }

        var card = request.Card;
        if (card is not null)
        {
            // Re-check server-side - the browser already validated, but a client can be bypassed entirely.
            if (!card.PassesLuhn())
            {
                return GatewayResult.Failed(null, "The card number is not valid");
            }
            if (card.IsExpired(DateOnly.FromDateTime(DateTime.UtcNow)))
            {
                return GatewayResult.Failed(null, "The card has expired");
            }
            if (!card.HasValidCvv())
            {
                return GatewayResult.Failed(null, "The security code is not valid");
            }
            if (card.Number!.StartsWith(DeclineCardPrefix))
            {
                return GatewayResult.Failed($"TXN-{Guid.NewGuid()}", "The card was declined by the issuer");
            }
            if (card.Number!.StartsWith(InsufficientFundsPrefix))
            {
                return GatewayResult.Failed($"TXN-{Guid.NewGuid()}", "Insufficient funds");
            }
        }

        _logger.LogInformation("[SIMULATED] Charging {Amount} for booking {BookingId} via {PaymentMethod} {CardSummary}",
            request.Amount, request.BookingId, request.PaymentMethod, card?.MaskedSummary ?? "");

        return GatewayResult.Success($"TXN-{Guid.NewGuid()}");
    }
}
