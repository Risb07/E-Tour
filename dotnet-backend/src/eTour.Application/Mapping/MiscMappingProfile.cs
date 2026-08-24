using AutoMapper;
using eTour.Application.Dtos;
using eTour.Domain.Entities;

namespace eTour.Application.Mapping;

public class MiscMappingProfile : Profile
{
    public MiscMappingProfile()
    {
        CreateMap<Customer, CustomerResponse>();
        CreateMap<CustomerRequest, Customer>()
            .ForMember(d => d.CustomerId, opt => opt.Ignore())
            .ForMember(d => d.User, opt => opt.Ignore());

        CreateMap<Role, RoleDto>().ReverseMap()
            .ForMember(d => d.Users, opt => opt.Ignore());

        CreateMap<ContactEnquiry, ContactEnquiryResponse>();
        CreateMap<ContactEnquiryRequest, ContactEnquiry>()
            .ForMember(d => d.EnquiryId, opt => opt.Ignore())
            .ForMember(d => d.Status, opt => opt.Ignore())
            .ForMember(d => d.CreatedAt, opt => opt.Ignore())
            .ForMember(d => d.UpdatedAt, opt => opt.Ignore());

        CreateMap<NewsletterSubscriber, NewsletterResponse>();
        CreateMap<NewsletterRequest, NewsletterSubscriber>()
            .ForMember(d => d.SubscriberId, opt => opt.Ignore())
            .ForMember(d => d.Active, opt => opt.Ignore())
            .ForMember(d => d.SubscribedAt, opt => opt.Ignore())
            .ForMember(d => d.UnsubscribedAt, opt => opt.Ignore());

        CreateMap<WishlistItem, WishlistItemResponse>()
            .ForMember(d => d.TourTitle, opt => opt.MapFrom(s => s.Tour.Title))
            .ForMember(d => d.BasePrice, opt => opt.MapFrom(s => s.Tour.BasePrice))
            .ForMember(d => d.TourCode, opt => opt.MapFrom(s => s.Tour.TourCode))
            .ForMember(d => d.DurationDays, opt => opt.MapFrom(s => s.Tour.DurationDays));

        CreateMap<Review, ReviewResponse>()
            .ForMember(d => d.CustomerName, opt => opt.MapFrom(s => s.Customer.FullName));

        CreateMap<Passenger, PassengerDto>();

        CreateMap<ExcelUploadBatch, ExcelUploadResult>()
            .ForMember(d => d.Errors, opt => opt.Ignore());
    }
}
