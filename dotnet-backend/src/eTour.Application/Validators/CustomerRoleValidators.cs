using eTour.Application.Dtos;
using FluentValidation;

namespace eTour.Application.Validators;

public class CustomerRequestValidator : AbstractValidator<CustomerRequest>
{
    public CustomerRequestValidator()
    {
        RuleFor(x => x.FullName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.Email).NotEmpty().EmailAddress().MaximumLength(150);
        RuleFor(x => x.Phone).Matches(@"^\d{10}$").When(x => !string.IsNullOrEmpty(x.Phone))
            .WithMessage("Phone must be exactly 10 digits");
    }
}

public class RoleDtoValidator : AbstractValidator<RoleDto>
{
    public RoleDtoValidator()
    {
        RuleFor(x => x.RoleName).NotEmpty().MaximumLength(50);
    }
}
