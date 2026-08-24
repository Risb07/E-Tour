using AutoMapper;
using eTour.Application.Dtos;
using eTour.Domain.Entities;

namespace eTour.Application.Mapping;

public class SectorMappingProfile : Profile
{
    public SectorMappingProfile()
    {
        CreateMap<Sector, SectorDto>().ReverseMap()
            .ForMember(d => d.SubSectors, opt => opt.Ignore());

        CreateMap<SubSector, SubSectorDto>().ReverseMap()
            .ForMember(d => d.Sector, opt => opt.Ignore())
            .ForMember(d => d.Products, opt => opt.Ignore());

        CreateMap<TourProduct, TourProductDto>().ReverseMap()
            .ForMember(d => d.SubSector, opt => opt.Ignore())
            .ForMember(d => d.Tour, opt => opt.Ignore());

        CreateMap<TourTagRule, TourTagRuleDto>().ReverseMap()
            .ForMember(d => d.TargetCategory, opt => opt.Ignore())
            .ForMember(d => d.TargetSubSector, opt => opt.Ignore());
    }
}
