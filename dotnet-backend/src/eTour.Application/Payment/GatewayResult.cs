using eTour.Domain.Enums;

namespace eTour.Application.Gateways;

/// <summary>Outcome of a gateway charge attempt. Callers branch on PaymentStatus, never a provider-specific code.</summary>
public class GatewayResult
{
    private GatewayResult(PaymentStatus status, string? transactionRef, string? failureReason)
    {
        Status = status;
        TransactionRef = transactionRef;
        FailureReason = failureReason;
    }

    public PaymentStatus Status { get; }
    public string? TransactionRef { get; }
    public string? FailureReason { get; }
    public bool IsSuccessful => Status == PaymentStatus.SUCCESS;

    public static GatewayResult Success(string transactionRef) => new(PaymentStatus.SUCCESS, transactionRef, null);

    /// <summary>Provider accepted the request but settlement is not final yet (webhook to follow).</summary>
    public static GatewayResult Pending(string transactionRef) => new(PaymentStatus.PENDING, transactionRef, null);

    public static GatewayResult Failed(string? transactionRef, string reason) => new(PaymentStatus.FAILED, transactionRef, reason);
}
