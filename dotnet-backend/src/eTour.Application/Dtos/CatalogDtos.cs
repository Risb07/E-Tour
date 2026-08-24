namespace eTour.Application.Dtos;

public class CategoryRequest
{
    public string CategoryName { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public bool Status { get; set; } = true;
    public string? CategoryCode { get; set; }
    public string? IsFeatured { get; set; }
    public long? ParentCategoryId { get; set; }
}

/// <summary>
/// A category without its own parent link - the shape Java produces wherever a Category is
/// nested inside another payload (its @JsonIgnoreProperties strips "parentCategory" there to
/// stop the recursion). Used for both a category's parent and a tour's category list.
/// </summary>
public class CategorySummaryDto
{
    public long CategoryId { get; set; }
    public string CategoryName { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public bool Status { get; set; }
    public string? CategoryCode { get; set; }
    public string? IsFeatured { get; set; }
    public long? ParentCategoryId { get; set; }
}

public class CategoryResponse
{
    public long CategoryId { get; set; }
    public string CategoryName { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public bool Status { get; set; }
    public string? CategoryCode { get; set; }
    public string? IsFeatured { get; set; }
    public long? ParentCategoryId { get; set; }

    /// <summary>
    /// The parent as a nested object, matching Java's serialized Category entity. The site builds
    /// its whole category tree off this (top-level = no parentCategory, sub-categories = those
    /// whose parentCategory.categoryId matches), so a bare ParentCategoryId scalar would flatten
    /// every category into a top-level one and leave every sub-category page empty.
    /// </summary>
    public CategorySummaryDto? ParentCategory { get; set; }

    public int ActiveTourCount { get; set; }
}

public class LocationDto
{
    public long LocationId { get; set; }
    public string LocationName { get; set; } = null!;
    public string? StateProvince { get; set; }
    public string? Country { get; set; }
    public string? CountryCode { get; set; }
    public bool Status { get; set; } = true;
}

public class ContentDto
{
    public long ContentId { get; set; }
    public string ContentKey { get; set; } = null!;
    public string? PageName { get; set; }
    public string LanguageCode { get; set; } = "en";
    public string? ContentValue { get; set; }
    public string? MediaUrl { get; set; }
    public string? LinkUrl { get; set; }
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
}

public class AdBannerDto
{
    public long AdId { get; set; }
    public string Title { get; set; } = null!;
    public string? ImageUrl { get; set; }
    public string? LinkUrl { get; set; }
    public string? Position { get; set; }
    public bool Status { get; set; } = true;
}

public class CrawlingTextDto
{
    public long CrawlingTextId { get; set; }
    public string Text { get; set; } = null!;
    public int SortOrder { get; set; }
    public bool IsActive { get; set; } = true;
}

public class NavMenuItemDto
{
    public long NavMenuItemId { get; set; }
    public string Label { get; set; } = null!;
    public string? Link { get; set; }
    public int SortOrder { get; set; }
    public bool Active { get; set; } = true;
    public long? ParentItemId { get; set; }
    public List<NavMenuItemDto> Children { get; set; } = [];
}
