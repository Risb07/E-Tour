namespace eTour.Domain.Enums;

public enum EnquiryStatus
{
    NEW,
    IN_PROGRESS,
    RESOLVED
}

public enum UploadBatchStatus
{
    UPLOADED,
    PROCESSING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED
}

/// <summary>Which field a multipath tag rule matches against.</summary>
public enum RuleMatchField
{
    TOUR_CODE,
    TITLE,
    BASE_PRICE,
    DURATION_DAYS,
    ALL
}

/// <summary>How a multipath tag rule compares its match value.</summary>
public enum RuleOperator
{
    EQUALS,
    CONTAINS,
    GREATER_THAN,
    LESS_THAN,
    BETWEEN,
    ANY
}

/// <summary>Geographic granularity, distinct from the Location entity (which is an actual place record).</summary>
public enum LocationType
{
    CONTINENT,
    COUNTRY,
    STATE,
    CITY,
    AREA,
    LANDMARK
}
