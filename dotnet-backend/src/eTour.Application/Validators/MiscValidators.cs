using eTour.Application.Dtos;
using FluentValidation;

namespace eTour.Application.Validators;

public class ReviewRequestValidator : AbstractValidator<ReviewRequest>
{
    public ReviewRequestValidator()
    {
        RuleFor(x => x.Rating).InclusiveBetween(1, 5);
        RuleFor(x => x.Comment).MaximumLength(2000);
    }
}

public class ContactEnquiryRequestValidator : AbstractValidator<ContactEnquiryRequest>
{
    public ContactEnquiryRequestValidator()
    {
        RuleFor(x => x.Name).NotEmpty().MaximumLength(150);
        RuleFor(x => x.Email).NotEmpty().EmailAddress().MaximumLength(150);
        RuleFor(x => x.Message).NotEmpty();
    }
}

public class NewsletterRequestValidator : AbstractValidator<NewsletterRequest>
{
    public NewsletterRequestValidator()
    {
        RuleFor(x => x.Email).NotEmpty().EmailAddress().MaximumLength(190);
    }
}

public class TourTagRuleDtoValidator : AbstractValidator<TourTagRuleDto>
{
    public TourTagRuleDtoValidator()
    {
        RuleFor(x => x.Name).NotEmpty().MaximumLength(150);
        RuleFor(x => x.TargetCategoryId).GreaterThan(0);
    }
}
