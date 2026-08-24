using eTour.Application.Dtos;
using FluentValidation;

namespace eTour.Application.Validators;

public class PaymentRequestValidator : AbstractValidator<PaymentRequest>
{
    public PaymentRequestValidator()
    {
        RuleFor(x => x.BookingId).GreaterThan(0);
        RuleFor(x => x.PaymentMethod).NotEmpty();
    }
}

public class CardPaymentRequestValidator : AbstractValidator<CardPaymentRequest>
{
    public CardPaymentRequestValidator()
    {
        RuleFor(x => x.BookingId).GreaterThan(0);
        RuleFor(x => x.CardNumber).NotEmpty().Matches(@"^\d{12,19}$")
            .WithMessage("Card number must be 12-19 digits");
        RuleFor(x => x.CardHolderName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.ExpiryMonth).InclusiveBetween(1, 12);
        RuleFor(x => x.ExpiryYear).GreaterThanOrEqualTo(DateTime.UtcNow.Year);
        RuleFor(x => x.Cvv).NotEmpty().Matches(@"^\d{3,4}$").WithMessage("CVV must be 3-4 digits");
    }
}
