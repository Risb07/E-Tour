using eTour.Application.Dtos;

namespace eTour.Application.Services;

/// <summary>
/// Requirement #3 (Microsoft.Extensions.AI): admin-only tour-description assistant. Drafts
/// marketing copy from bullet points via IChatClient - never writes the tour itself, the admin
/// reviews the suggestion and saves it through the normal PUT /api/tours/{id} if they like it.
/// </summary>
public interface ITourAiAssistantService
{
    /// <summary>Throws AiNotConfiguredException if no AI provider API key is configured.</summary>
    Task<string> SuggestDescriptionAsync(TourDescriptionAssistRequest request, CancellationToken ct = default);
}
