using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IAdminDashboardService
{
    Task<DashboardStatsResponse> GetStatsAsync(CancellationToken ct = default);
}
