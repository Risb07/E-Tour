using AutoMapper;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;

namespace eTour.Application.Mapping;

public class CatalogMappingProfile : Profile
{
    public CatalogMappingProfile()
    {
        CreateMap<Category, CategorySummaryDto>();
        // ActiveTourCount is expressed as a subquery rather than Ignore()d so ProjectTo folds it
        // into the same SQL statement as the rest of the read, instead of one count round-trip
        // per category. It only resolves correctly through ProjectTo - the read paths in
        // CategoryService use it for exactly that reason.
        CreateMap<Category, CategoryResponse>()
            .ForMember(d => d.ActiveTourCount,
                opt => opt.MapFrom(s => s.Tours.Count(t => t.Status == TourStatus.ACTIVE)));
        CreateMap<CategoryRequest, Category>()
            .ForMember(d => d.CategoryId, opt => opt.Ignore())
            .ForMember(d => d.ParentCategory, opt => opt.Ignore())
            .ForMember(d => d.ChildCategories, opt => opt.Ignore())
            .ForMember(d => d.Tours, opt => opt.Ignore());
        // CategoryResponse also doubles as the update payload for the generic service.
        CreateMap<CategoryResponse, Category>()
            .ForMember(d => d.ParentCategory, opt => opt.Ignore())
            .ForMember(d => d.ChildCategories, opt => opt.Ignore())
            .ForMember(d => d.Tours, opt => opt.Ignore());
        CreateMap<CategorySummaryDto, Category>()
            .ForMember(d => d.ParentCategory, opt => opt.Ignore())
            .ForMember(d => d.ChildCategories, opt => opt.Ignore())
            .ForMember(d => d.Tours, opt => opt.Ignore());

        CreateMap<Location, LocationDto>().ReverseMap();
        CreateMap<Content, ContentDto>().ReverseMap();
        CreateMap<AdBanner, AdBannerDto>().ReverseMap();
        CreateMap<CrawlingText, CrawlingTextDto>().ReverseMap();

        CreateMap<NavMenuItem, NavMenuItemDto>()
            .ForMember(d => d.Children, opt => opt.MapFrom(s => s.Children));
        CreateMap<NavMenuItemDto, NavMenuItem>()
            .ForMember(d => d.ParentItem, opt => opt.Ignore())
            .ForMember(d => d.Children, opt => opt.Ignore());
    }
}
