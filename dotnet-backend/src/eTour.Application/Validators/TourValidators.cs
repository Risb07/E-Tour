using eTour.Application.Dtos;
using eTour.Domain.Enums;
using FluentValidation;

namespace eTour.Application.Validators;

public class TourRequestValidator : AbstractValidator<TourRequest>
{
    public TourRequestValidator()
    {
        RuleFor(x => x.Title).NotEmpty().MaximumLength(200);
        RuleFor(x => x.DurationDays).GreaterThanOrEqualTo(1).WithMessage("Duration must be at least 1 day");
        RuleFor(x => x.BasePrice).GreaterThan(0).WithMessage("Base price must be greater than zero");
        RuleFor(x => x.TourCode).NotEmpty().IsEnumName(typeof(TourCode), caseSensitive: true);
        RuleFor(x => x.Status).NotEmpty().IsEnumName(typeof(TourStatus), caseSensitive: true);
    }
}

public class TourCostDtoValidator : AbstractValidator<TourCostDto>
{
    public TourCostDtoValidator()
    {
        RuleFor(x => x.ValidFrom).NotEmpty();
        RuleFor(x => x.ValidTo).NotEmpty().GreaterThanOrEqualTo(x => x.ValidFrom)
            .WithMessage("Valid-to date must be on or after valid-from");
    }
}

public class TourScheduleDtoValidator : AbstractValidator<TourScheduleDto>
{
    public TourScheduleDtoValidator()
    {
        RuleFor(x => x.DepartureDate).NotEmpty();
        RuleFor(x => x.ReturnDate).NotEmpty().GreaterThanOrEqualTo(x => x.DepartureDate)
            .WithMessage("Return date must be on or after the departure date");
        RuleFor(x => x.AvailableSeats).GreaterThanOrEqualTo(0);
        RuleFor(x => x.Price).GreaterThan(0);
    }
}

public class TourAddonDtoValidator : AbstractValidator<TourAddonDto>
{
    public TourAddonDtoValidator()
    {
        RuleFor(x => x.AddonName).NotEmpty().MaximumLength(150);
        RuleFor(x => x.Price).GreaterThan(0);
        RuleFor(x => x.PriceType).NotEmpty().IsEnumName(typeof(PriceType), caseSensitive: true);
    }
}

public class TourContentDtoValidator : AbstractValidator<TourContentDto>
{
    public TourContentDtoValidator()
    {
        RuleFor(x => x.ContentType).NotEmpty().IsEnumName(typeof(TourContentType), caseSensitive: true);
    }
}

public class TourMediaDtoValidator : AbstractValidator<TourMediaDto>
{
    public TourMediaDtoValidator()
    {
        RuleFor(x => x.FilePath).NotEmpty();
        RuleFor(x => x.MediaType).NotEmpty().IsEnumName(typeof(MediaType), caseSensitive: true);
        RuleFor(x => x.TabContext).NotEmpty().IsEnumName(typeof(TabContext), caseSensitive: true);
    }
}

public class ItineraryDtoValidator : AbstractValidator<ItineraryDto>
{
    public ItineraryDtoValidator()
    {
        RuleFor(x => x.DayNumber).GreaterThanOrEqualTo(1);
        RuleFor(x => x.Title).NotEmpty().MaximumLength(200);
    }
}

public class JourneyDetailDtoValidator : AbstractValidator<JourneyDetailDto>
{
    public JourneyDetailDtoValidator()
    {
        RuleFor(x => x.ModeOfTravel).NotEmpty().IsEnumName(typeof(TravelMode), caseSensitive: true);
        RuleFor(x => x.FromLocationId).GreaterThan(0);
        RuleFor(x => x.ToLocationId).GreaterThan(0);
    }
}

public class StayMealDtoValidator : AbstractValidator<StayMealDto>
{
    public StayMealDtoValidator()
    {
        RuleFor(x => x.DayNumber).GreaterThanOrEqualTo(1);
    }
}

public class RoomChargeDtoValidator : AbstractValidator<RoomChargeDto>
{
    public RoomChargeDtoValidator()
    {
        RuleFor(x => x.Charge).GreaterThanOrEqualTo(0);
        RuleFor(x => x.Occupancy).NotEmpty().IsEnumName(typeof(Occupancy), caseSensitive: true);
    }
}
