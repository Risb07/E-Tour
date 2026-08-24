using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class ContactService : IContactService
{
    private readonly IGenericRepository<ContactEnquiry, long> _repository;
    private readonly IMapper _mapper;

    public ContactService(IGenericRepository<ContactEnquiry, long> repository, IMapper mapper)
    {
        _repository = repository;
        _mapper = mapper;
    }

    public async Task<ContactEnquiryResponse> SubmitAsync(ContactEnquiryRequest request, CancellationToken ct = default)
    {
        var entity = _mapper.Map<ContactEnquiry>(request);
        entity.Status = EnquiryStatus.NEW;
        entity.CreatedAt = DateTime.UtcNow;
        entity.UpdatedAt = DateTime.UtcNow;
        var saved = await _repository.AddAsync(entity, ct);
        return _mapper.Map<ContactEnquiryResponse>(saved);
    }

    public async Task<IReadOnlyList<ContactEnquiryResponse>> GetAllAsync(CancellationToken ct = default)
    {
        var entities = await _repository.GetAllAsync(ct);
        return _mapper.Map<IReadOnlyList<ContactEnquiryResponse>>(entities);
    }

    public async Task<ContactEnquiryResponse> GetByIdAsync(long id, CancellationToken ct = default) =>
        _mapper.Map<ContactEnquiryResponse>(await RequireAsync(id, ct));

    public async Task<ContactEnquiryResponse> UpdateStatusAsync(long id, string status, CancellationToken ct = default)
    {
        var entity = await RequireAsync(id, ct);
        if (!Enum.TryParse<EnquiryStatus>(status, true, out var parsed))
        {
            throw new ArgumentException($"Invalid enquiry status '{status}'");
        }
        entity.Status = parsed;
        entity.UpdatedAt = DateTime.UtcNow;
        await _repository.UpdateAsync(entity, ct);
        return _mapper.Map<ContactEnquiryResponse>(entity);
    }

    public async Task<long> CountAsync(CancellationToken ct = default) => (await _repository.GetAllAsync(ct)).Count;

    public async Task DeleteAsync(long id, CancellationToken ct = default)
    {
        var entity = await RequireAsync(id, ct);
        await _repository.DeleteAsync(entity, ct);
    }

    private async Task<ContactEnquiry> RequireAsync(long id, CancellationToken ct) =>
        await _repository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException($"Enquiry {id} was not found");
}
