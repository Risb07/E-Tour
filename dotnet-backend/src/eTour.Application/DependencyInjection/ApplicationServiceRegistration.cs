using eTour.Application.Gateways;
using eTour.Application.Pricing;
using eTour.Application.Services;
using Microsoft.Extensions.DependencyInjection;

namespace eTour.Application.DependencyInjection;

public static class ApplicationServiceRegistration
{
    /// <summary>Feature service registrations - added here as each service is built out.</summary>
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IExternalAuthService, ExternalAuthService>();
        services.AddScoped<IUserService, UserService>();
        services.AddScoped<ICustomerService, CustomerService>();
        services.AddScoped<ICategoryService, CategoryService>();
        services.AddScoped<IContactService, ContactService>();
        services.AddScoped<INewsletterService, NewsletterService>();
        services.AddScoped<IRoomChargeService, RoomChargeService>();
        services.AddScoped<ITourCostService, TourCostService>();
        services.AddScoped<ITourScheduleService, TourScheduleService>();
        services.AddScoped<ISectorService, SectorService>();
        services.AddScoped<ISubSectorService, SubSectorService>();
        services.AddScoped<ITourProductService, TourProductService>();
        services.AddScoped<INavMenuService, NavMenuService>();
        services.AddScoped<IWishlistService, WishlistService>();
        services.AddScoped<ITourService, TourService>();
        services.AddScoped<ITourDetailService, TourDetailService>();
        services.AddScoped<ITourSearchService, TourSearchService>();

        services.AddSingleton<TourPricingCalculator>();
        services.AddSingleton<IPaymentGateway, SimulatedPaymentGateway>();
        services.AddScoped<ICartService, CartService>();
        services.AddScoped<IBookingService, BookingService>();
        services.AddScoped<IPaymentService, PaymentService>();
        services.AddScoped<IInvoiceService, InvoiceService>();
        services.AddScoped<IMultipathRuleService, MultipathRuleService>();
        services.AddScoped<IMultipathGeneratorService, MultipathGeneratorService>();
        services.AddScoped<IAdminDashboardService, AdminDashboardService>();
        services.AddScoped<IPassengerService, PassengerService>();
        services.AddScoped<IReviewService, ReviewService>();

        return services;
    }
}
