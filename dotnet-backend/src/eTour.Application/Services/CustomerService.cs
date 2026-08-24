using AutoMapper;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class CustomerService : ICustomerService
{
    private readonly IGenericRepository<Customer, long> _repository;
    private readonly IGenericRepository<User, long> _userRepository;
    private readonly ICurrentUserService _currentUser;
    private readonly IMapper _mapper;

    public CustomerService(IGenericRepository<Customer, long> repository, IGenericRepository<User, long> userRepository,
        ICurrentUserService currentUser, IMapper mapper)
    {
        _repository = repository;
        _userRepository = userRepository;
        _currentUser = currentUser;
        _mapper = mapper;
    }

    public async Task<IReadOnlyList<CustomerResponse>> GetAllAsync(CancellationToken ct = default) =>
        _mapper.Map<List<CustomerResponse>>(await _repository.GetAllAsync(ct));

    public async Task<CustomerResponse> CreateAsync(CustomerRequest request, CancellationToken ct = default)
    {
        _ = await _userRepository.GetByIdAsync(request.UserId, ct)
            ?? throw new ResourceNotFoundException($"User not found with id: {request.UserId}");

        if (_repository.Query().Any(c => c.UserId == request.UserId))
        {
            throw new ResourceConflictException("User already has a customer profile");
        }

        var entity = _mapper.Map<Customer>(request);
        var saved = await _repository.AddAsync(entity, ct);
        return _mapper.Map<CustomerResponse>(saved);
    }

    public async Task<CustomerResponse> GetByIdAsync(long id, CancellationToken ct = default)
    {
        var customer = await RequireAsync(id, ct);
        await RequireAdminOrOwnerAsync(customer, ct);
        return _mapper.Map<CustomerResponse>(customer);
    }

    public async Task<CustomerResponse> UpdateAsync(long id, CustomerRequest request, CancellationToken ct = default)
    {
        var customer = await RequireAsync(id, ct);
        await RequireAdminOrOwnerAsync(customer, ct);

        customer.FullName = request.FullName;
        customer.Email = request.Email;
        customer.Phone = request.Phone;
        await _repository.UpdateAsync(customer, ct);
        return _mapper.Map<CustomerResponse>(customer);
    }

    public async Task DeleteAsync(long id, CancellationToken ct = default)
    {
        var customer = await RequireAsync(id, ct);
        await _repository.DeleteAsync(customer, ct);
    }

    public async Task<CustomerResponse> GetCurrentAsync(CancellationToken ct = default) =>
        _mapper.Map<CustomerResponse>(await _currentUser.CurrentCustomerAsync(ct));

    public async Task<CustomerResponse> UpdateCurrentAsync(UpdateProfileRequest request, CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        customer.FullName = request.FullName;
        customer.Phone = request.Phone;
        await _repository.UpdateAsync(customer, ct);
        return _mapper.Map<CustomerResponse>(customer);
    }

    private async Task<Customer> RequireAsync(long id, CancellationToken ct) =>
        await _repository.GetByIdAsync(id, ct) ?? throw new ResourceNotFoundException("Customer Not Found");

    private async Task RequireAdminOrOwnerAsync(Customer customer, CancellationToken ct)
    {
        if (_currentUser.IsAdmin)
        {
            return;
        }

        var current = await _currentUser.CurrentCustomerAsync(ct);
        if (current.CustomerId != customer.CustomerId)
        {
            // 404 on purpose - don't reveal the existence of other profiles.
            throw new ResourceNotFoundException("Customer Not Found");
        }
    }
}
