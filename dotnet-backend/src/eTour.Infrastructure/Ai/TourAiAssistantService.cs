using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Exceptions;
using Microsoft.Extensions.AI;

namespace eTour.Infrastructure.Ai;

public class TourAiAssistantService : ITourAiAssistantService
{
    private readonly IChatClient? _chatClient;

    // Optional by design: if Ai:ApiKey isn't configured, IChatClient is never registered and
    // this stays null - the feature degrades to a clear 503 instead of a startup crash.
    public TourAiAssistantService(IChatClient? chatClient = null)
    {
        _chatClient = chatClient;
    }

    public async Task<string> SuggestDescriptionAsync(TourDescriptionAssistRequest request, CancellationToken ct = default)
    {
        if (_chatClient is null)
        {
            throw new AiNotConfiguredException(
                "The AI tour-description assistant is not configured. Set Ai:ApiKey (or the AI_API_KEY environment variable) to enable it.");
        }

        var bullets = request.Bullets.Count > 0
            ? string.Join("\n", request.Bullets.Select(b => $"- {b}"))
            : "(no highlights provided - use your best judgement based on the title)";
        var tone = string.IsNullOrWhiteSpace(request.Tone) ? "appealing and informative" : request.Tone;

        var prompt =
            $"""
             Write a concise marketing description (2-4 sentences) for a travel tour, in a {tone} tone.
             Tour title: {request.TourTitle}
             Highlights:
             {bullets}

             Return only the description text, no headings or markdown.
             """;

        var response = await _chatClient.GetResponseAsync(prompt, cancellationToken: ct);
        return response.Text.Trim();
    }
}
