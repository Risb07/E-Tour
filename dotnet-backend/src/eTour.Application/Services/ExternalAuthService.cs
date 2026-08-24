using System.Security.Cryptography;
using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using Microsoft.Extensions.Logging;

namespace eTour.Application.Services;

/// <summary>
/// Account linking for OAuth sign-in. Additive by design: it ends by calling the SAME
/// <see cref="IJwtTokenService"/> the password login uses, so an OAuth user carries an ordinary
/// eTour JWT and every existing [Authorize] endpoint, role lookup and ICurrentUserService path
/// works unchanged - nothing downstream can tell how the token was obtained.
///
/// Accounts are linked on the provider's verified email, NOT on the stored provider id. The
/// `google_sub` column is recorded for provenance and support, but is deliberately not the lookup
/// key: linking by email is what lets someone who registered with a password later click "Continue
/// with Google" and land in the same account. The trade-off is that it trusts the provider's email
/// verification - fine for Google, and it must stay that way for any provider added later (see the
/// guard in the controller).
///
/// The three federated columns are owned by the Java backend's `users` table, which this backend
/// shares. They are written here with the same rules as its GoogleUserProvisioningService, so an
/// account's recorded provenance does not depend on which backend was running when it was created.
/// </summary>
public class ExternalAuthService : IExternalAuthService
{
    private const string DefaultExternalRole = "CUSTOMER";

    /// <summary>Stand-in for the phone number no OAuth provider gives us. Ten digits so it satisfies
    /// the Java side's <c>^[0-9]{10}$</c> constraint on Customer.phone; all zeroes so it is obviously
    /// not a real number.</summary>
    private const string PlaceholderPhone = "0000000000";

    private readonly IGenericRepository<User, long> _userRepository;
    private readonly IGenericRepository<Role, long> _roleRepository;
    private readonly IGenericRepository<Customer, long> _customerRepository;
    private readonly IJwtTokenService _jwtTokenService;
    private readonly ILogger<ExternalAuthService> _logger;

    public ExternalAuthService(IGenericRepository<User, long> userRepository, IGenericRepository<Role, long> roleRepository,
        IGenericRepository<Customer, long> customerRepository, IJwtTokenService jwtTokenService,
        ILogger<ExternalAuthService> logger)
    {
        _userRepository = userRepository;
        _roleRepository = roleRepository;
        _customerRepository = customerRepository;
        _jwtTokenService = jwtTokenService;
        _logger = logger;
    }

    public async Task<LoginResponse> SignInAsync(ExternalIdentity identity, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(identity.Email))
        {
            // Not InvalidCredentialsException: that type deliberately has no message overload so it
            // can never leak whether an account exists, and its fixed "Invalid email or password"
            // would misdescribe what actually went wrong here (the provider's response was unusable).
            throw new ArgumentException($"{identity.Provider} did not return an email address for this account.");
        }

        // Compared lower-cased on both sides: MySQL's default collation is case-insensitive but
        // the provider may return a different casing than the row was registered with, and an
        // exact match would then provision a duplicate account for the same person.
        var email = identity.Email.Trim();
        var normalized = email.ToLowerInvariant();

        // The whole entity, not a projection: the link step below writes back to it.
        var existing = _userRepository.Query().FirstOrDefault(u => u.Email.ToLower() == normalized);

        if (existing is not null)
        {
            // Same rule as the password login: a disabled account cannot sign in by any route.
            if (!existing.Status)
            {
                _logger.LogInformation("Blocked {Provider} sign-in for disabled user {UserId}", identity.Provider, existing.UserId);
                throw new InvalidCredentialsException();
            }

            var roleName = _roleRepository.Query().Where(r => r.RoleId == existing.RoleId)
                .Select(r => r.RoleName).FirstOrDefault() ?? DefaultExternalRole;

            await LinkExistingAsync(existing, identity, ct);

            _logger.LogInformation("User {UserId} signed in via {Provider}", existing.UserId, identity.Provider);

            return new LoginResponse
            {
                Token = _jwtTokenService.GenerateToken(existing.Email),
                UserId = existing.UserId,
                FirstName = existing.FirstName,
                LastName = existing.LastName,
                Email = existing.Email,
                Role = roleName
            };
        }

        return await ProvisionAsync(identity, email, ct);
    }

    /// <summary>
    /// Attaches the provider identity to an account that already exists under this email. The
    /// password hash, role and status are deliberately left alone: linking must never escalate or
    /// downgrade an account, and a customer who already has a password keeps being able to use it.
    /// </summary>
    private async Task LinkExistingAsync(User existing, ExternalIdentity identity, CancellationToken ct)
    {
        var changed = false;

        if (!string.IsNullOrWhiteSpace(identity.Subject) && identity.Subject != existing.GoogleSub)
        {
            existing.GoogleSub = identity.Subject;
            changed = true;
        }
        // An account created with a password stays marked LOCAL - it can now be reached both ways,
        // and overwriting the marker would lose that fact. Only a row with nothing recorded gets
        // stamped.
        if (string.IsNullOrWhiteSpace(existing.AuthProvider))
        {
            existing.AuthProvider = "LOCAL";
            changed = true;
        }
        if (!string.IsNullOrWhiteSpace(identity.AvatarUrl) && identity.AvatarUrl != existing.AvatarUrl)
        {
            existing.AvatarUrl = identity.AvatarUrl;
            changed = true;
        }

        if (changed)
        {
            await _userRepository.UpdateAsync(existing, ct);
        }
    }

    /// <summary>
    /// First sign-in: create the User + Customer pair exactly as UserService.RegisterAsync does,
    /// so an OAuth-created account is indistinguishable from a self-registered one downstream
    /// (bookings, cart, reviews and passengers all hang off the Customer row).
    /// </summary>
    private async Task<LoginResponse> ProvisionAsync(ExternalIdentity identity, string email, CancellationToken ct)
    {
        // Never trust a role from an external provider - external sign-up is always CUSTOMER,
        // mirroring the "public self-registration is ALWAYS CUSTOMER" rule in UserService.
        var role = _roleRepository.Query().FirstOrDefault(r => r.RoleName == DefaultExternalRole)
            ?? throw new InvalidOperationException($"Default role '{DefaultExternalRole}' is not seeded in the database");

        var firstName = string.IsNullOrWhiteSpace(identity.FirstName) ? email.Split('@')[0] : identity.FirstName.Trim();
        var lastName = identity.LastName?.Trim() ?? string.Empty;

        var user = new User
        {
            FirstName = firstName,
            LastName = lastName,
            Email = email,
            // The column is NOT NULL and the account has no password. Hashing random bytes gives a
            // valid BCrypt hash that no input can ever match, so this account simply cannot be
            // logged into through /api/auth/login until the user sets a real password - rather
            // than leaving an empty or well-known hash that might verify against something.
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(Convert.ToBase64String(RandomNumberGenerator.GetBytes(32))),
            PreferredLanguage = "en",
            Status = true,
            RoleId = role.RoleId,
            AuthProvider = identity.Provider.ToUpperInvariant(),
            GoogleSub = identity.Subject,
            AvatarUrl = identity.AvatarUrl
        };

        var savedUser = await _userRepository.AddAsync(user, ct);

        var customer = new Customer
        {
            UserId = savedUser.UserId,
            FullName = $"{firstName} {lastName}".Trim(),
            Email = savedUser.Email,
            // customer.phone is NOT NULL in the shared schema and the provider never supplies one,
            // so copying the (always null) user phone here aborted the insert on a real Google
            // sign-up. Ten zeroes satisfies the column and the Java side's ^[0-9]{10}$ validator,
            // and is obviously not a real number if it surfaces in an admin screen. The customer
            // replaces it on their profile or at booking time. Matches GoogleUserProvisioningService.
            Phone = string.IsNullOrWhiteSpace(savedUser.Phone) ? PlaceholderPhone : savedUser.Phone
        };
        await _customerRepository.AddAsync(customer, ct);

        _logger.LogInformation("Provisioned user {UserId} from first {Provider} sign-in", savedUser.UserId, identity.Provider);

        return new LoginResponse
        {
            Token = _jwtTokenService.GenerateToken(savedUser.Email),
            UserId = savedUser.UserId,
            FirstName = savedUser.FirstName,
            LastName = savedUser.LastName,
            Email = savedUser.Email,
            Role = role.RoleName
        };
    }
}
