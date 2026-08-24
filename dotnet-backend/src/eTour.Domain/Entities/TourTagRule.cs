using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class TourTagRule
{
    public long RuleId { get; set; }
    public string Name { get; set; } = null!;
    public RuleMatchField MatchField { get; set; }
    public RuleOperator MatchOperator { get; set; }
    public string? MatchValue { get; set; }
    public string? MatchValueTo { get; set; }
    public int Priority { get; set; }
    public bool Active { get; set; } = true;

    public long TargetCategoryId { get; set; }
    public Category TargetCategory { get; set; } = null!;

    public long? TargetSubSectorId { get; set; }
    public SubSector? TargetSubSector { get; set; }
}
