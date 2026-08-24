using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IMultipathRuleService
{
    Task<IReadOnlyList<TourTagRuleDto>> GetAllAsync(CancellationToken ct = default);
    Task<TourTagRuleDto> CreateAsync(TourTagRuleDto dto, CancellationToken ct = default);
    Task<TourTagRuleDto> UpdateAsync(long ruleId, TourTagRuleDto dto, CancellationToken ct = default);
    Task DeleteAsync(long ruleId, CancellationToken ct = default);
}

public interface IMultipathGeneratorService
{
    /// <summary>Dry run - returns exactly what Apply() would change, writing nothing.</summary>
    Task<MultipathPreviewResponse> PreviewAsync(CancellationToken ct = default);
    /// <summary>Applies every active rule. Safe to re-run - existing links are skipped.</summary>
    Task<MultipathPreviewResponse> ApplyAsync(CancellationToken ct = default);
}
