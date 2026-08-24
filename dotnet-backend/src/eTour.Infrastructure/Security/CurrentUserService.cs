using System.Security.Claims;
using eTour.Application.Common;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;

namespace eTour.Infrastructure.Security;

public class CurrentUserService : ICurrentUserService
{
    private readonly IHttpContextAccessor _httpContextAccessor;
    private readonly EtourDbContext _context;

    public CurrentUserService(IHttpContextAccessor httpContextAccessor, EtourDbContext context)
    {
        _httpContextAccessor = httpContextAccessor;
        _context = context;
    }

    private ClaimsPrincipal? Principal => _httpContextAccessor.HttpContext?.User;

    public bool IsAuthenticated => Principal?.Identity?.IsAuthenticated ?? false;

    public string? Email => Principal?.FindFirstValue(ClaimTypes.Email) ?? Principal?.Identity?.Name;

    public bool IsAdmin => Principal?.IsInRole("ADMIN") ?? false;

    public async Task<User> CurrentUserAsync(CancellationToken ct = default)
    {
        if (Email is null)
        {
            throw new UnauthorizedAccessException("No authenticated user in the current request");
        }

        return await _context.Users.Include(u => u.Role).FirstOrDefaultAsync(u => u.Email == Email, ct)
            ?? throw new ResourceNotFoundException("Authenticated user not found");
    }

    public async Task<Customer> CurrentCustomerAsync(CancellationToken ct = default)
    {
        var user = await CurrentUserAsync(ct);
        return await _context.Customers.FirstOrDefaultAsync(c => c.UserId == user.UserId, ct)
            ?? throw new ResourceNotFoundException("The authenticated user has no customer profile");
    }
}
