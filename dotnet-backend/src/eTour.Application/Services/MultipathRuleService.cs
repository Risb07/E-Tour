using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class MultipathRuleService : IMultipathRuleService
{
    private readonly IGenericRepository<TourTagRule, long> _ruleRepository;
    private readonly IGenericRepository<Category, long> _categoryRepository;
    private readonly IGenericRepository<SubSector, long> _subSectorRepository;

    public MultipathRuleService(IGenericRepository<TourTagRule, long> ruleRepository, IGenericRepository<Category, long> categoryRepository,
        IGenericRepository<SubSector, long> subSectorRepository)
    {
        _ruleRepository = ruleRepository;
        _categoryRepository = categoryRepository;
        _subSectorRepository = subSectorRepository;
    }

    public Task<IReadOnlyList<TourTagRuleDto>> GetAllAsync(CancellationToken ct = default)
    {
        var rules = _ruleRepository.Query().OrderBy(r => r.Priority).ThenBy(r => r.RuleId).ToList();
        return Task.FromResult<IReadOnlyList<TourTagRuleDto>>(rules.Select(ToDto).ToList());
    }

    public async Task<TourTagRuleDto> CreateAsync(TourTagRuleDto dto, CancellationToken ct = default)
    {
        var rule = new TourTagRule();
        await ApplyAsync(dto, rule, ct);
        var saved = await _ruleRepository.AddAsync(rule, ct);
        return ToDto(saved);
    }

    public async Task<TourTagRuleDto> UpdateAsync(long ruleId, TourTagRuleDto dto, CancellationToken ct = default)
    {
        var rule = await _ruleRepository.GetByIdAsync(ruleId, ct) ?? throw new ResourceNotFoundException($"Rule not found with id: {ruleId}");
        await ApplyAsync(dto, rule, ct);
        await _ruleRepository.UpdateAsync(rule, ct);
        return ToDto(rule);
    }

    public async Task DeleteAsync(long ruleId, CancellationToken ct = default)
    {
        var rule = await _ruleRepository.GetByIdAsync(ruleId, ct) ?? throw new ResourceNotFoundException($"Rule not found with id: {ruleId}");
        // Deleting a rule does NOT remove links it already created.
        await _ruleRepository.DeleteAsync(rule, ct);
    }

    private async Task ApplyAsync(TourTagRuleDto dto, TourTagRule rule, CancellationToken ct)
    {
        var category = await _categoryRepository.GetByIdAsync(dto.TargetCategoryId, ct)
            ?? throw new ResourceNotFoundException($"Category not found with id: {dto.TargetCategoryId}");

        SubSector? subSector = null;
        if (dto.TargetSubSectorId is { } subSectorId)
        {
            subSector = await _subSectorRepository.GetByIdAsync(subSectorId, ct)
                ?? throw new ResourceNotFoundException($"Sub-sector not found with id: {subSectorId}");
        }

        rule.Name = dto.Name;
        rule.MatchField = Enum.Parse<RuleMatchField>(dto.MatchField, true);
        rule.MatchOperator = Enum.Parse<RuleOperator>(dto.MatchOperator, true);
        rule.MatchValue = dto.MatchValue;
        rule.MatchValueTo = dto.MatchValueTo;
        rule.TargetCategoryId = category.CategoryId;
        rule.TargetSubSectorId = subSector?.SubSectorId;
        rule.Priority = dto.Priority;
        rule.Active = dto.Active ?? true;
    }

    private TourTagRuleDto ToDto(TourTagRule rule)
    {
        var categoryName = _categoryRepository.Query().Where(c => c.CategoryId == rule.TargetCategoryId).Select(c => c.CategoryName).FirstOrDefault();
        var subSectorName = rule.TargetSubSectorId is { } id
            ? _subSectorRepository.Query().Where(s => s.SubSectorId == id).Select(s => s.Name).FirstOrDefault()
            : null;

        return new TourTagRuleDto
        {
            RuleId = rule.RuleId,
            Name = rule.Name,
            MatchField = rule.MatchField.ToString(),
            MatchOperator = rule.MatchOperator.ToString(),
            MatchValue = rule.MatchValue,
            MatchValueTo = rule.MatchValueTo,
            TargetCategoryId = rule.TargetCategoryId,
            TargetCategoryName = categoryName,
            TargetSubSectorId = rule.TargetSubSectorId,
            TargetSubSectorName = subSectorName,
            Priority = rule.Priority,
            Active = rule.Active
        };
    }
}
