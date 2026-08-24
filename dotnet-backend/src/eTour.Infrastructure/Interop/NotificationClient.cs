using System.Net.Http.Json;
using eTour.Application.Common;
using eTour.Application.Services;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace eTour.Infrastructure.Interop;

/// <summary>
/// Talks to the eTour notification microservice.
///
/// <para>This backend no longer sends mail itself - it hands the message to the notification
/// service, which owns the queue, the templates and the single retry. Mirrors
/// <c>Backend/src/main/java/com/etour/integration/NotificationClient.java</c> so both backends
/// behave identically; see <c>notification-service/README.md</c>.</para>
///
/// <para><b>How it authenticates:</b> the notification service accepts any token signed with the
/// shared <c>JWT_SECRET</c>, so this mints one for itself through the same
/// <see cref="IJwtTokenService"/> that issues customer tokens. No new credential to configure and
/// no shared API key to leak. The subject is a reserved internal address that can never match a
/// real account, which gives a useful least-privilege result: the service's admin endpoints
/// re-check the ADMIN role against a backend's database, and a subject with no user row fails that
/// check - so this token can <em>enqueue</em> and nothing else.</para>
/// </summary>
public class NotificationClient : INotificationClient
{
    /// <summary>Not a real, or registrable, mailbox - see the class summary.</summary>
    private const string ServiceSubject = "system@etour.internal";

    private readonly HttpClient _httpClient;
    private readonly IJwtTokenService _jwtTokenService;
    private readonly ILogger<NotificationClient> _logger;
    private readonly bool _enabled;

    public NotificationClient(HttpClient httpClient, IJwtTokenService jwtTokenService,
        IConfiguration configuration, ILogger<NotificationClient> logger)
    {
        _httpClient = httpClient;
        _jwtTokenService = jwtTokenService;
        _logger = logger;
        _enabled = configuration.GetValue<bool?>("Notification:Enabled") ?? true;

        if (!_enabled)
        {
            _logger.LogWarning("Notification service integration is DISABLED - no e-mail will be queued.");
        }
    }

    /// <summary>
    /// Queues a message. Best-effort by contract: this is called after a payment has already been
    /// persisted, so a failure here is logged and swallowed rather than thrown.
    ///
    /// <para>There is deliberately <b>no fallback to direct SMTP</b>. If the enqueue fails after the
    /// service actually accepted it - a lost response, a timeout on a request that landed - a
    /// fallback send would put a second copy of the same e-mail in the customer's inbox. The receipt
    /// stays available from <c>GET /api/invoices/booking/{id}/receipt</c>, and the admin
    /// Notifications screen shows anything that did not go out.</para>
    /// </summary>
    public async Task<bool> EnqueueAsync(string recipient, string template,
        IReadOnlyDictionary<string, string> variables, string idempotencyKey,
        IReadOnlyList<NotificationAttachment>? attachments = null, CancellationToken ct = default)
    {
        if (!_enabled)
        {
            return false;
        }

        try
        {
            var body = new
            {
                recipient,
                template,
                variables,
                idempotencyKey,
                attachments = attachments ?? []
            };

            using var request = new HttpRequestMessage(HttpMethod.Post, "/svc/notifications")
            {
                Content = JsonContent.Create(body)
            };
            request.Headers.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", _jwtTokenService.GenerateToken(ServiceSubject));

            var response = await _httpClient.SendAsync(request, ct);
            response.EnsureSuccessStatusCode();

            _logger.LogInformation("Queued '{Template}' notification to {Recipient} (idempotency key {Key})",
                template, recipient, idempotencyKey);
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Could not queue '{Template}' notification to {Recipient} (key {Key})",
                template, recipient, idempotencyKey);
            return false;
        }
    }
}
