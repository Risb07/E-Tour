namespace eTour.Application.Dtos;

public class TourTagRuleDto
{
    public long RuleId { get; set; }
    public string Name { get; set; } = null!;
    public string MatchField { get; set; } = null!;
    public string MatchOperator { get; set; } = null!;
    public string? MatchValue { get; set; }
    public string? MatchValueTo { get; set; }
    public int Priority { get; set; }
    public bool? Active { get; set; } = true;
    public long TargetCategoryId { get; set; }
    public string? TargetCategoryName { get; set; }
    public long? TargetSubSectorId { get; set; }
    public string? TargetSubSectorName { get; set; }
}

public class PlannedChange
{
    public PlannedChange()
    {
    }

    public PlannedChange(long tourId, string tourTitle, string ruleName, string linkType, string targetName, bool alreadyLinked)
    {
        TourId = tourId;
        TourTitle = tourTitle;
        RuleName = ruleName;
        LinkType = linkType;
        TargetName = targetName;
        AlreadyLinked = alreadyLinked;
    }

    public long TourId { get; set; }
    public string TourTitle { get; set; } = null!;
    public string RuleName { get; set; } = null!;
    /// <summary>"CATEGORY" or "PRODUCT". Named LinkType to match the multipath preview table.</summary>
    public string LinkType { get; set; } = null!;
    public string TargetName { get; set; } = null!;
    public bool AlreadyLinked { get; set; }
}

public class MultipathPreviewResponse
{
    public int RulesEvaluated { get; set; }
    public int ToursEvaluated { get; set; }
    public int NewCategoryLinks { get; set; }
    public int NewProducts { get; set; }
    public List<PlannedChange> Changes { get; set; } = [];
    public List<string> RulesWithNoMatches { get; set; } = [];
}
