using eTour.Application.Dtos;
using FluentValidation;

namespace eTour.Application.Validators;

public class PassengerInputValidator : AbstractValidator<PassengerInput>
{
    public PassengerInputValidator()
    {
        RuleFor(x => x.FullName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.Dob).LessThan(DateOnly.FromDateTime(DateTime.UtcNow)).When(x => x.Dob is not null)
            .WithMessage("Date of birth must be in the past");
        RuleFor(x => x.Nationality).Length(2).When(x => !string.IsNullOrEmpty(x.Nationality));
        RuleFor(x => x.Gender).Length(1).When(x => !string.IsNullOrEmpty(x.Gender));
        // Matches Java's @NotBlank(message = "ID Proof Number is required") on Passenger.idProofNumber -
        // the column is NOT NULL with no DB default, so without this the request used to reach
        // SaveChangesAsync and fail with a raw MySqlException instead of a clean 400.
        RuleFor(x => x.IdProofNumber).NotEmpty().MaximumLength(50).WithMessage("ID Proof Number is required");
    }
}

public class BookingAddonSelectionValidator : AbstractValidator<BookingAddonSelection>
{
    public BookingAddonSelectionValidator()
    {
        RuleFor(x => x.AddonId).GreaterThan(0);
        RuleFor(x => x.Quantity).GreaterThan(0);
    }
}

public class BookingRequestValidator : AbstractValidator<BookingRequest>
{
    public BookingRequestValidator()
    {
        RuleFor(x => x.ScheduleId).GreaterThan(0);
        RuleFor(x => x.NumberOfPassengers).GreaterThanOrEqualTo(1);
        RuleForEach(x => x.Passengers!).SetValidator(new PassengerInputValidator()).When(x => x.Passengers is not null);
        RuleForEach(x => x.Addons).SetValidator(new BookingAddonSelectionValidator());
    }
}

public class CartAddonSelectionValidator : AbstractValidator<CartAddonSelection>
{
    public CartAddonSelectionValidator()
    {
        RuleFor(x => x.AddonId).GreaterThan(0);
        RuleFor(x => x.Quantity).GreaterThan(0);
    }
}

public class CartRequestValidator : AbstractValidator<CartRequest>
{
    public CartRequestValidator()
    {
        RuleFor(x => x.ScheduleId).GreaterThan(0);
        RuleFor(x => x.AdultCount).GreaterThanOrEqualTo(0);
        RuleFor(x => x.ChildCount).GreaterThanOrEqualTo(0);
        RuleForEach(x => x.Addons).SetValidator(new CartAddonSelectionValidator());
    }
}
