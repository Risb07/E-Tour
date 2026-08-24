namespace eTour.Application.Gateways;

/// <summary>
/// Boundary between "our booking/payment business logic" and "whoever actually moves the money".
/// PaymentService owns the domain rules (ownership, status transitions, invoice + receipt); this
/// interface owns only the charge itself. Swap in a real provider by adding a new implementation
/// and changing Payment:Provider - no business logic should need to change.
/// </summary>
public interface IPaymentGateway
{
    /// <summary>Provider key for logging/persistence, e.g. "SIMULATED", "RAZORPAY".</summary>
    string ProviderName { get; }

    /// <summary>
    /// Attempts to take payment. Implementations must not throw for an ordinary decline - return
    /// a failed GatewayResult instead. Exceptions are for genuinely exceptional cases only.
    /// </summary>
    GatewayResult Charge(GatewayChargeRequest request);
}

/// <summary>Immutable request handed to the gateway.</summary>
/// <param name="Card">Present for card payments, null for other methods. Never persisted.</param>
public record GatewayChargeRequest(long BookingId, decimal Amount, string PaymentMethod, string CustomerEmail, CardDetails? Card = null);
