using AutoMapper;
using eTour.Application.Dtos;
using eTour.Domain.Entities;

namespace eTour.Application.Mapping;

public class TourMappingProfile : Profile
{
    public TourMappingProfile()
    {
        CreateMap<Tour, TourResponse>();
        CreateMap<TourRequest, Tour>()
            .ForMember(d => d.TourId, opt => opt.Ignore())
            .ForMember(d => d.Categories, opt => opt.Ignore())
            .ForMember(d => d.TourCosts, opt => opt.Ignore())
            .ForMember(d => d.Schedules, opt => opt.Ignore())
            .ForMember(d => d.Itineraries, opt => opt.Ignore())
            .ForMember(d => d.Addons, opt => opt.Ignore())
            .ForMember(d => d.Media, opt => opt.Ignore())
            .ForMember(d => d.Contents, opt => opt.Ignore())
            .ForMember(d => d.JourneyDetails, opt => opt.Ignore())
            .ForMember(d => d.StayMeals, opt => opt.Ignore())
            .ForMember(d => d.RoomCharges, opt => opt.Ignore())
            .ForMember(d => d.Reviews, opt => opt.Ignore());
        // TourResponse also doubles as the update payload for the generic service - category
        // linking itself is handled explicitly in TourService, not by AutoMapper.
        CreateMap<TourResponse, Tour>()
            .ForMember(d => d.Categories, opt => opt.Ignore())
            .ForMember(d => d.TourCosts, opt => opt.Ignore())
            .ForMember(d => d.Schedules, opt => opt.Ignore())
            .ForMember(d => d.Itineraries, opt => opt.Ignore())
            .ForMember(d => d.Addons, opt => opt.Ignore())
            .ForMember(d => d.Media, opt => opt.Ignore())
            .ForMember(d => d.Contents, opt => opt.Ignore())
            .ForMember(d => d.JourneyDetails, opt => opt.Ignore())
            .ForMember(d => d.StayMeals, opt => opt.Ignore())
            .ForMember(d => d.RoomCharges, opt => opt.Ignore())
            .ForMember(d => d.Reviews, opt => opt.Ignore());

        CreateMap<TourCost, TourCostDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());

        CreateMap<TourSchedule, TourScheduleDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore())
            .ForMember(d => d.Bookings, opt => opt.Ignore())
            .ForMember(d => d.Carts, opt => opt.Ignore());

        CreateMap<TourAddon, TourAddonDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());

        CreateMap<TourContent, TourContentDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());

        CreateMap<TourMedia, TourMediaDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());

        CreateMap<Itinerary, ItineraryDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());

        // The *Name members resolve through a navigation, so these two maps only populate fully
        // via ProjectTo (which turns them into SQL joins) - see TourDetailService's read methods.
        CreateMap<JourneyDetail, JourneyDetailDto>()
            .ForMember(d => d.FromLocationName, opt => opt.MapFrom(s => s.FromLocation.LocationName))
            .ForMember(d => d.ToLocationName, opt => opt.MapFrom(s => s.ToLocation.LocationName))
            .ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore())
            .ForMember(d => d.FromLocation, opt => opt.Ignore())
            .ForMember(d => d.ToLocation, opt => opt.Ignore());

        CreateMap<StayMeal, StayMealDto>()
            // Null-forgiving because LocationEntity is genuinely optional (a stay row can be
            // hotel-only). This expression is never dereferenced in C#: ProjectTo translates it to
            // a LEFT JOIN that yields null, and AutoMapper's in-memory path null-checks the chain.
            .ForMember(d => d.LocationName, opt => opt.MapFrom(s => s.LocationEntity!.LocationName))
            .ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore())
            .ForMember(d => d.LocationEntity, opt => opt.Ignore());

        CreateMap<RoomCharge, RoomChargeDto>().ReverseMap()
            .ForMember(d => d.Tour, opt => opt.Ignore());
    }
}
