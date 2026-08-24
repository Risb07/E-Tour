namespace eTour.Domain.Entities;

public class Location
{
    public long LocationId { get; set; }
    public string LocationName { get; set; } = null!;
    public string? StateProvince { get; set; }
    public string? Country { get; set; }
    /// <summary>ISO-2 country code.</summary>
    public string? CountryCode { get; set; }
    public bool Status { get; set; } = true;
}
