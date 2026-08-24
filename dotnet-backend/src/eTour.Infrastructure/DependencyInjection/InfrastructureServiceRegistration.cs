using eTour.Application.Common;
using eTour.Application.Services;
using eTour.Infrastructure.Files;
using eTour.Infrastructure.Persistence;
using eTour.Infrastructure.Receipts;
using eTour.Infrastructure.Seeding;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace eTour.Infrastructure.DependencyInjection;

public static class InfrastructureServiceRegistration
{
    /// <summary>Infra-specific registrations (JavaMicroserviceClient, AI, DemoDataSeeder) are added here as each is built out.</summary>
    public static IServiceCollection AddInfrastructure(IServiceCollection services, IConfiguration configuration)
    {
        services.AddScoped<IBookingLockRepository, BookingLockRepository>();
        services.AddScoped<ITourScheduleLockRepository, TourScheduleLockRepository>();
        services.AddScoped<IMultipathLinkRepository, MultipathLinkRepository>();
        services.AddScoped<IFileStorageService, FileStorageService>();
        services.AddScoped<IExcelUploadService, ExcelUploadService>();
        services.AddSingleton<IExcelTemplateService, ExcelTemplateService>();
        services.AddSingleton<IReceiptPdfService, ReceiptPdfService>();
        services.AddScoped<IReceiptEmailService, ReceiptEmailService>();
        services.AddScoped<DemoDataSeeder>();
        return services;
    }
}
