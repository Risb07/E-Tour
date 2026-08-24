using eTour.Application.Dtos;
using FluentValidation;

namespace eTour.Application.Validators;

public class CategoryRequestValidator : AbstractValidator<CategoryRequest>
{
    public CategoryRequestValidator()
    {
        RuleFor(x => x.CategoryName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.CategoryCode).Matches("^(DOM|ADV|INT)$").When(x => !string.IsNullOrEmpty(x.CategoryCode))
            .WithMessage("Category code must be one of DOM, ADV, INT");
        RuleFor(x => x.IsFeatured).Matches("^[YN]$").When(x => !string.IsNullOrEmpty(x.IsFeatured))
            .WithMessage("IsFeatured must be 'Y' or 'N'");
    }
}

public class LocationDtoValidator : AbstractValidator<LocationDto>
{
    public LocationDtoValidator()
    {
        RuleFor(x => x.LocationName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.CountryCode).Length(2).When(x => !string.IsNullOrEmpty(x.CountryCode));
    }
}

public class ContentDtoValidator : AbstractValidator<ContentDto>
{
    public ContentDtoValidator()
    {
        RuleFor(x => x.ContentKey).NotEmpty().MaximumLength(100);
    }
}

public class AdBannerDtoValidator : AbstractValidator<AdBannerDto>
{
    public AdBannerDtoValidator()
    {
        RuleFor(x => x.Title).NotEmpty().MaximumLength(200);
    }
}

public class CrawlingTextDtoValidator : AbstractValidator<CrawlingTextDto>
{
    public CrawlingTextDtoValidator()
    {
        RuleFor(x => x.Text).NotEmpty();
    }
}

public class NavMenuItemDtoValidator : AbstractValidator<NavMenuItemDto>
{
    public NavMenuItemDtoValidator()
    {
        RuleFor(x => x.Label).NotEmpty().MaximumLength(100);
    }
}
