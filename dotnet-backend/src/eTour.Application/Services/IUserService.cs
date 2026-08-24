using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IUserService
{
    Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken ct = default);
}
