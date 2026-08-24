namespace eTour.Application.Services;

/// <summary>
/// Hands an outbound message to the notification microservice. Implemented in Infrastructure so the
/// Application layer never depends on HttpClient.
/// </summary>
public interface INotificationClient
{
    /// <param name="idempotencyKey">
    /// Stable per logical message. The notification service collapses a repeat enqueue with the same
    /// key onto the original, which is what guarantees one e-mail per booking even if this is called
    /// more than once.
    /// </param>
    /// <returns>true if the service accepted the message.</returns>
    Task<bool> EnqueueAsync(string recipient, string template,
        IReadOnlyDictionary<string, string> variables, string idempotencyKey,
        IReadOnlyList<NotificationAttachment>? attachments = null, CancellationToken ct = default);
}

/// <summary>
/// An attachment on the wire. Base64 because the notification API is JSON, and the property names
/// match the service's <c>AttachmentInput</c> field-for-field.
/// </summary>
public record NotificationAttachment(string Filename, string ContentType, string ContentBase64);
