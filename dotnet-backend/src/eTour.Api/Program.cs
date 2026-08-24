using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using eTour.Api.Common;
using eTour.Api.Middleware;
using eTour.Api.Validation;
using eTour.Application.Common;
using FluentValidation;
using eTour.Infrastructure.Persistence;
using eTour.Infrastructure.Security;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authentication;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.AI;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using SharpGrip.FluentValidation.AutoValidation.Mvc.Extensions;
using Serilog;

Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .WriteTo.Console()
    .WriteTo.File("logs/etour-api-.log", rollingInterval: RollingInterval.Day, retainedFileCountLimit: 14)
    .Enrich.FromLogContext()
    .CreateBootstrapLogger();

try
{
    var builder = WebApplication.CreateBuilder(args);

    builder.Host.UseSerilog((context, services, configuration) => configuration
        .ReadFrom.Configuration(context.Configuration)
        .ReadFrom.Services(services)
        .Enrich.FromLogContext()
        .WriteTo.Console()
        .WriteTo.File("logs/etour-api-.log", rollingInterval: RollingInterval.Day, retainedFileCountLimit: 14));

    // --- MVC / validation ---------------------------------------------------
    //builder.Services.AddControllers();
    builder.Services
    .AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(
            new System.Text.Json.Serialization.JsonStringEnumConverter());
    });
    builder.Services.AddFluentValidationAutoValidation(config =>
    {
        // Requirement #4/#6: validation failures are normalized to the same
        // ApiErrorResponse envelope as every other error, via this factory.
        config.OverrideDefaultResultFactoryWith<ValidationResultFactory>();
    });
    builder.Services.AddValidatorsFromAssembly(typeof(eTour.Application.AssemblyMarker).Assembly);

    builder.Services.AddEndpointsApiExplorer();
    builder.Services.AddSwaggerGen(c =>
    {
        c.SwaggerDoc("v1", new OpenApiInfo { Title = "eTour API", Version = "v1" });
        c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
        {
            Description = "JWT Authorization header using the Bearer scheme. Example: \"Bearer {token}\"",
            Name = "Authorization",
            In = ParameterLocation.Header,
            Type = SecuritySchemeType.ApiKey,
            Scheme = "Bearer"
        });
        c.AddSecurityRequirement(new OpenApiSecurityRequirement
        {
            {
                new OpenApiSecurityScheme { Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = "Bearer" } },
                Array.Empty<string>()
            }
        });
    });

    // --- Persistence ---------------------------------------------------------
    // A fixed ServerVersion (rather than ServerVersion.AutoDetect) avoids a live DB connection
    // during app startup and EF Core design-time tooling (migrations) - override via
    // ConnectionStrings:MySqlServerVersion if the target server is a materially different version.
    // Skipped entirely under the "Testing" environment so eTour.Tests' WebApplicationFactory can
    // register its own InMemory provider without ending up with two providers in one container
    // (EF Core does not allow that).
    if (!builder.Environment.IsEnvironment("Testing"))
    {
        var connectionString = builder.Configuration.GetConnectionString("Default");
        var mySqlVersion = builder.Configuration["ConnectionStrings:MySqlServerVersion"] ?? "8.0.35";
        builder.Services.AddDbContext<EtourDbContext>(options =>
            options.UseMySql(connectionString, new MySqlServerVersion(new Version(mySqlVersion))));
    }

    builder.Services.AddScoped(typeof(IGenericRepository<,>), typeof(GenericRepository<,>));
    builder.Services.AddScoped(typeof(IGenericService<,,>), typeof(eTour.Application.Common.GenericService<,,>));

    // --- AutoMapper / cross-cutting services ----------------------------------
    builder.Services.AddAutoMapper(cfg => { }, typeof(eTour.Application.AssemblyMarker).Assembly);
    builder.Services.AddHttpContextAccessor();
    builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();
    builder.Services.AddScoped<IJwtTokenService, JwtTokenService>();
    builder.Services.AddScoped<ExceptionHandlingMiddleware>();

    // --- JWT auth --------------------------------------------------------------
    // Built once, here, and registered as this exact INSTANCE (not by type) so every consumer -
    // both TokenValidationParameters below and JwtTokenService (resolved later via DI) - shares
    // the identical key. Registering by type instead (AddSingleton<JwtSigningKeyProvider>()) was
    // tried first and was a real bug: DI would lazily construct its own separate instance the
    // first time something injected it, and since an unset Jwt:Secret makes the constructor pick
    // a random key, that second instance's key never matched the one already baked into
    // TokenValidationParameters - every login would issue a token that could never authenticate.
    using var keyLoggerFactory = LoggerFactory.Create(b => b.AddConsole());
    var jwtSigningKeyProvider = new JwtSigningKeyProvider(builder.Configuration, keyLoggerFactory.CreateLogger<JwtSigningKeyProvider>());
    builder.Services.AddSingleton(jwtSigningKeyProvider);

    builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
        .AddJwtBearer(options =>
        {
            // Without this, the handler silently rewrites inbound "sub" (and other short JWT
            // claim names) to long legacy .NET claim URIs (JwtSecurityTokenHandler's
            // DefaultInboundClaimTypeMap) before OnTokenValidated ever sees them - so
            // FindFirstValue(JwtRegisteredClaimNames.Sub) below would find nothing, every token
            // would fail "Token has no subject", and every request would 401 regardless of how
            // valid the token's signature was.
            options.MapInboundClaims = false;
            options.TokenValidationParameters = new TokenValidationParameters
            {
                ValidateIssuer = false,
                ValidateAudience = false,
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(jwtSigningKeyProvider.Key),
                ValidateLifetime = true,
                ClockSkew = TimeSpan.FromSeconds(30),
                NameClaimType = JwtRegisteredClaimNames.Sub
            };

            // Role is never baked into the token (matching Java) - it is looked up
            // fresh from the DB on every request, so a role change or a disabled
            // account takes effect on the very next request without reissuing a token.
            options.Events = new JwtBearerEvents
            {
                OnTokenValidated = async context =>
                {
                    var email = context.Principal?.FindFirstValue(JwtRegisteredClaimNames.Sub);
                    if (string.IsNullOrEmpty(email))
                    {
                        context.Fail("Token has no subject");
                        return;
                    }

                    var db = context.HttpContext.RequestServices.GetRequiredService<EtourDbContext>();
                    var user = await db.Users.Include(u => u.Role).FirstOrDefaultAsync(u => u.Email == email);
                    if (user is null || !user.Status)
                    {
                        context.Fail("User not found or disabled");
                        return;
                    }

                    var identity = (ClaimsIdentity)context.Principal!.Identity!;
                    identity.AddClaim(new Claim(ClaimTypes.Email, user.Email));
                    identity.AddClaim(new Claim(ClaimTypes.Role, user.Role.RoleName));
                },
                OnChallenge = context =>
                {
                    context.HandleResponse();
                    return ApiErrorResponseWriter.WriteAsync(context.Response, StatusCodes.Status401Unauthorized,
                        "Authentication is required to access this resource");
                },
                OnForbidden = context => ApiErrorResponseWriter.WriteAsync(context.Response,
                    StatusCodes.Status403Forbidden, "You do not have permission to perform this action")
            };
        });

    // --- OAuth social sign-in (additive) ---------------------------------------
    // Registered as EXTRA schemes only. AddAuthentication() with no argument does not touch the
    // default authenticate/challenge scheme set above, so JWT bearer remains the default for the
    // entire API and no existing endpoint changes behaviour. The whole block is skipped unless a
    // client id AND secret are configured - GoogleOptions.Validate() throws on a blank client id,
    // which would otherwise turn "OAuth not set up" into a startup crash for everyone.
    // The Testing guard is not redundant with IsGoogleConfigured: WebApplicationFactory applies
    // its configuration overrides when the host is BUILT, which is after these top-level
    // statements have already read builder.Configuration - so a test cannot blank these keys out
    // from here, and a developer with real credentials in appsettings.json would otherwise have
    // the Google handler registered inside every integration test. (The controller reads the
    // BUILT configuration at request time, where the override does apply, so it still correctly
    // reports "not configured".) Same pattern, and same reason, as the DbContext skip above.
    if (!builder.Environment.IsEnvironment("Testing")
        && eTour.Api.Auth.ExternalAuthDefaults.IsGoogleConfigured(builder.Configuration))
    {
        // Logged at startup because the single most common OAuth failure - Google's
        // "Error 400: redirect_uri_mismatch" - happens entirely on Google's side, before any
        // request reaches this app, so there is nothing in the request log to diagnose it from.
        // Printing the exact path here makes it a copy-paste comparison against the console.
        Log.Information(
            "Google sign-in enabled. Register this EXACT Authorized redirect URI on the Google OAuth client: {Scheme}://{Host}{CallbackPath}",
            "http(s)", "<your-host:port>", eTour.Api.Auth.ExternalAuthDefaults.GoogleCallbackPath(builder.Configuration));

        builder.Services.AddAuthentication()
            .AddCookie(eTour.Api.Auth.ExternalAuthDefaults.ExternalScheme, options =>
            {
                // Exists only to survive the hop back from Google; the callback deletes it as soon
                // as it has read the principal, so it is short-lived by design.
                options.Cookie.Name = "eTour.External";
                options.Cookie.HttpOnly = true;
                options.Cookie.IsEssential = true;
                options.Cookie.SameSite = SameSiteMode.Lax;
                options.ExpireTimeSpan = TimeSpan.FromMinutes(5);
            })
            .AddGoogle(eTour.Api.Auth.ExternalAuthDefaults.GoogleScheme, options =>
            {
                options.ClientId = builder.Configuration[eTour.Api.Auth.ExternalAuthDefaults.GoogleClientIdKey]!;
                options.ClientSecret = builder.Configuration[eTour.Api.Auth.ExternalAuthDefaults.GoogleClientSecretKey]!;
                options.SignInScheme = eTour.Api.Auth.ExternalAuthDefaults.ExternalScheme;

                // Google matches this byte-for-byte against the client's registered Authorized
                // redirect URIs; any difference in scheme, host, port or path is rejected as
                // redirect_uri_mismatch. Overridable so an already-registered URI can be matched.
                options.CallbackPath = eTour.Api.Auth.ExternalAuthDefaults.GoogleCallbackPath(builder.Configuration);

                // Nothing needs the Google access/refresh token after sign-in - we only ever read
                // the identity claims - so not persisting them keeps them out of the cookie.
                options.SaveTokens = false;

                // The handler maps sub/email/given_name/family_name by default but not `picture`,
                // and the Java backend stores the avatar on the user row - so map it explicitly
                // rather than losing it. Claim name matches the raw Google key.
                options.ClaimActions.MapJsonKey(eTour.Api.Auth.ExternalAuthDefaults.PictureClaim, "picture");

                // Lax rather than the None default: the return leg from Google is a top-level GET
                // navigation, which Lax permits, whereas SameSite=None additionally requires
                // Secure and so silently breaks the flow over plain http on localhost in dev.
                options.CorrelationCookie.SameSite = SameSiteMode.Lax;
            });
    }

    // Default-deny, matching Java's anyRequest().authenticated(): every endpoint requires
    // authentication unless explicitly marked [AllowAnonymous]. Prevents a controller action
    // that's missing an [Authorize]/[AllowAnonymous] attribute from silently becoming public.
    builder.Services.AddAuthorization(options =>
    {
        options.FallbackPolicy = new Microsoft.AspNetCore.Authorization.AuthorizationPolicyBuilder()
            .RequireAuthenticatedUser()
            .Build();
    });

    // --- CORS --------------------------------------------------------------
    var allowedOrigins = (builder.Configuration["Cors:AllowedOrigins"] ?? "http://localhost:5173,http://localhost:3000")
        .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);
    builder.Services.AddCors(options =>
    {
        options.AddDefaultPolicy(policy => policy
            .WithOrigins(allowedOrigins)
            .AllowAnyMethod()
            .AllowAnyHeader()
            .AllowCredentials());
    });

    // --- Application/Infrastructure service registration ----------------------
    eTour.Infrastructure.DependencyInjection.InfrastructureServiceRegistration.AddInfrastructure(builder.Services, builder.Configuration);
    eTour.Application.DependencyInjection.ApplicationServiceRegistration.AddApplication(builder.Services);

    // --- Requirement #11: Java microservice integration (HttpClient) ----------
    builder.Services.AddHttpClient<eTour.Application.Services.IJavaMicroserviceClient, eTour.Infrastructure.Interop.JavaMicroserviceClient>(client =>
    {
        client.BaseAddress = new Uri(builder.Configuration["JavaMicroservice:BaseUrl"] ?? "http://localhost:8080");
        client.Timeout = TimeSpan.FromSeconds(10);
    });

    // --- Notification microservice --------------------------------------------
    // Receipt e-mail is queued here rather than sent from this process. A short
    // timeout on purpose: this call sits on the payment request's path, and a
    // slow notification service must not make paying feel slow. Failing fast is
    // safe because the enqueue is best-effort and the receipt stays downloadable.
    builder.Services.AddHttpClient<eTour.Application.Services.INotificationClient, eTour.Infrastructure.Interop.NotificationClient>(client =>
    {
        client.BaseAddress = new Uri(builder.Configuration["Notification:BaseUrl"] ?? "http://notification-service:8085");
        client.Timeout = TimeSpan.FromSeconds(10);
    });

    // --- Requirement #3: Microsoft.Extensions.AI - only registered when an API key is configured;
    // otherwise ITourAiAssistantService's IChatClient stays null and the feature degrades to a 503.
    var aiApiKey = builder.Configuration["Ai:ApiKey"];
    if (!string.IsNullOrWhiteSpace(aiApiKey))
    {
        var aiModel = builder.Configuration["Ai:Model"] ?? "gpt-4o-mini";
        builder.Services.AddSingleton<Microsoft.Extensions.AI.IChatClient>(
            _ => new OpenAI.Chat.ChatClient(aiModel, aiApiKey).AsIChatClient());
    }
    builder.Services.AddScoped<eTour.Application.Services.ITourAiAssistantService, eTour.Infrastructure.Ai.TourAiAssistantService>();

    var app = builder.Build();

    if (app.Environment.IsDevelopment())
    {
        app.UseSwagger();
        app.UseSwaggerUI();
    }

    app.UseMiddleware<ExceptionHandlingMiddleware>();

    app.UseSerilogRequestLogging();

    app.UseHttpsRedirection();

    // Serves uploaded tour media/Excel files under /uploads, matching Java's WebConfig mapping.
    var uploadDir = Path.Combine(builder.Environment.ContentRootPath, builder.Configuration["Upload:Directory"] ?? "uploads");
    Directory.CreateDirectory(uploadDir);
    app.UseStaticFiles(new StaticFileOptions
    {
        FileProvider = new Microsoft.Extensions.FileProviders.PhysicalFileProvider(uploadDir),
        RequestPath = "/uploads"
    });

    app.UseCors();

    app.UseAuthentication();
    app.UseAuthorization();

    app.MapControllers();

    if (builder.Configuration.GetValue<bool>("Seed:Enabled"))
    {
        using var scope = app.Services.CreateScope();
        var seeder = scope.ServiceProvider.GetRequiredService<eTour.Infrastructure.Seeding.DemoDataSeeder>();
        await seeder.SeedAllAsync();
    }

    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "eTour API terminated unexpectedly during startup");
}
finally
{
    Log.CloseAndFlush();
}

namespace eTour.Api
{
    /// <summary>Exposed for WebApplicationFactory-based integration tests.</summary>
    public partial class Program
    {
    }
}
