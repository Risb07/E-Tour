namespace eTour.Domain.Entities;

public class Category
{
    public long CategoryId { get; set; }
    public string CategoryName { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public bool Status { get; set; } = true;
    /// <summary>Free-text code, conventionally DOM|ADV|INT per the source app.</summary>
    public string? CategoryCode { get; set; }
    /// <summary>"Y"/"N" flag, kept as a string to match the legacy schema column.</summary>
    public string? IsFeatured { get; set; }

    public long? ParentCategoryId { get; set; }
    public Category? ParentCategory { get; set; }
    public ICollection<Category> ChildCategories { get; set; } = new List<Category>();

    public ICollection<Tour> Tours { get; set; } = new List<Tour>();
}
