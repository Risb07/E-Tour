using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class UserService : IUserService
{
    private const string DefaultSelfRegisterRole = "CUSTOMER";

    private readonly IGenericRepository<User, long> _userRepository;
    private readonly IGenericRepository<Role, long> _roleRepository;
    private readonly IGenericRepository<Customer, long> _customerRepository;
    private readonly IJwtTokenService _jwtTokenService;

    public UserService(IGenericRepository<User, long> userRepository, IGenericRepository<Role, long> roleRepository,
        IGenericRepository<Customer, long> customerRepository, IJwtTokenService jwtTokenService)
    {
        _userRepository = userRepository;
        _roleRepository = roleRepository;
        _customerRepository = customerRepository;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken ct = default)
    {
        if (_userRepository.Query().Any(u => u.Email == request.Email))
        {
            throw new ResourceConflictException("An account with this email already exists.");
        }

        // Public self-registration is ALWAYS the CUSTOMER role - never trust a role from the request body.
        var role = _roleRepository.Query().FirstOrDefault(r => r.RoleName == DefaultSelfRegisterRole)
            ?? throw new InvalidOperationException($"Default role '{DefaultSelfRegisterRole}' is not seeded in the database");

        var user = new User
        {
            FirstName = request.FirstName,
            LastName = request.LastName,
            Email = request.Email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            Phone = request.Phone,
            PreferredLanguage = "en",
            Status = true,
            RoleId = role.RoleId
        };

        var savedUser = await _userRepository.AddAsync(user, ct);

        // Every CUSTOMER-role user needs a Customer profile row for bookings/cart/reviews/passengers to attach to.
        var customer = new Customer
        {
            UserId = savedUser.UserId,
            FullName = $"{request.FirstName} {request.LastName ?? ""}".Trim(),
            Email = savedUser.Email,
            Phone = savedUser.Phone
        };
        await _customerRepository.AddAsync(customer, ct);

        // Issue the JWT here so the user is logged in immediately after registration.
        var token = _jwtTokenService.GenerateToken(savedUser.Email);

        return new RegisterResponse
        {
            UserId = savedUser.UserId,
            FirstName = savedUser.FirstName,
            LastName = savedUser.LastName,
            Email = savedUser.Email,
            Role = role.RoleName,
            Token = token
        };
    }
}
