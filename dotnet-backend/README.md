# eTour .NET Backend

A functional port of the Java Spring Boot backend (`../Backend`) to ASP.NET Core (.NET 10), built
to share the same MySQL database and serve the same frontend (`../etour-frontend`). `../Backend` is
untouched reference/legacy code — this is a completely separate project.

The port itself required **no** frontend changes: it matches the Java backend's JSON contract
exactly (see [Wire-contract checks](#wire-contract-checks)). The only edits ever made to
`../etour-frontend` were the "Continue with Google" button and its callback page, added
deliberately as a new feature — see [OAuth social sign-in](#oauth-social-sign-in-google).

## Architecture

Clean layering, `Api → Application/Infrastructure → Domain`:

```
dotnet-backend/
  eTour.sln
  src/
    eTour.Domain/          Entities (POCOs), enums, domain exceptions - no external dependencies
    eTour.Application/     DTOs, IGenericRepository/IGenericService, feature services, AutoMapper
                            profiles, FluentValidation validators, ICurrentUserService
    eTour.Infrastructure/  EF Core DbContext + Fluent config, JWT signing, file storage, MailKit
                            email, QuestPDF receipts, ClosedXML Excel, Java microservice HttpClient,
                            Microsoft.Extensions.AI wiring, demo data seeder
    eTour.Api/              Controllers, exception middleware, Program.cs, appsettings
  tests/
    eTour.Tests/            NUnit + Moq + FluentAssertions + WebApplicationFactory
```

### How the 11 requirements map to the code

| # | Requirement | Where |
|---|---|---|
| 1 | Structured logging | Serilog (console + rolling file), `ILogger<T>` throughout services/controllers, `UseSerilogRequestLogging()` |
| 2 | JWT auth | `Microsoft.AspNetCore.Authentication.JwtBearer`, `eTour.Infrastructure/Security/*`, role re-checked from the DB on every request (see below) |
| 3 | Microsoft.Extensions.AI | Admin-only tour-description assistant - `ITourAiAssistantService` / `POST /api/ai/tour-description`, degrades to 503 if `Ai:ApiKey` isn't set |
| 4 | Global exception middleware | `eTour.Api/Middleware/ExceptionHandlingMiddleware.cs` - one JSON envelope for every error, including validation failures |
| 5 | NUnit tests | `tests/eTour.Tests` - 111 tests: pricing engine, booking/payment/auth business logic, generic CRUD, AutoMapper, wire-contract checks, and full-pipeline controller tests |
| 6 | Validation | FluentValidation on every request DTO (server-side); see [Client-side validation](#client-side-validation-requirement-6) below |
| 7 | Generic CRUD interface | `eTour.Application/Common/IGenericRepository.cs`, `IGenericService.cs` |
| 8 | Generic CRUD implementation | `eTour.Infrastructure/Persistence/GenericRepository.cs`, `eTour.Application/Common/GenericService.cs` |
| 9 | "EmployeeService" | `TourService` - this domain's central entity. Composes `IGenericService<Tour,...>`/the generic repository for plain CRUD, adds tour-specific logic (category linking, search/detail delegation) on top |
| 10 | AutoMapper | `eTour.Application/Mapping/*` - entity ↔ DTO profiles, `ProjectTo` used wherever a DTO needs data from a related entity |
| 11 | Java microservice integration | `JavaMicroserviceClient` (`eTour.Infrastructure/Interop`) + `ProxyController` (`GET /api/proxy/java-status`), wired via `AddHttpClient` in `Program.cs` exactly per the brief's reference pattern |

## Prerequisites

- .NET 10 SDK
- MySQL (the same instance/schema `../Backend` uses, or a fresh one - see below)
- `dotnet-ef` for migrations: `dotnet tool install --global dotnet-ef`

## Configuration

All settings live in `src/eTour.Api/appsettings.json`, overridable via environment variables
(standard ASP.NET Core convention, e.g. `ConnectionStrings__Default`, `Jwt__Secret`) or
`appsettings.Development.json`.

| Setting | Purpose | Default |
|---|---|---|
| `ConnectionStrings:Default` | MySQL connection string | `Server=localhost;Port=3306;Database=etour;User=root;Password=;...` |
| `Jwt:Secret` | HS256 signing key (base64, ≥32 bytes) | unset → random per-process key (dev-only; logs a warning; invalidates sessions on restart) |
| `Jwt:ExpirationMs` | Token lifetime | `86400000` (24h) |
| `Cors:AllowedOrigins` | Comma-separated origins | `http://localhost:5173,http://localhost:3000` |
| `Upload:Directory` | Local disk folder for uploaded files, served at `/uploads` | `uploads` |
| `Mail:Host` / `Port` / `Username` / `Password` / `From` | SMTP for receipt emails (optional - skipped silently if `Username` is blank) | unset |
| `Invoice:GstRate` | Tax rate applied to bookings | `0.05` |
| `Seed:Enabled` | Run the idempotent demo data seeder on startup | `true` |
| `JavaMicroservice:BaseUrl` | Base URL for the Java backend interop demo | `http://localhost:8080` |
| `Ai:ApiKey` / `Ai:Model` | OpenAI-compatible key/model for the AI tour-description assistant | unset → feature returns 503 |
| `OAuth:Google:ClientId` / `ClientSecret` | Google OAuth client credentials | unset → routes return 503, app starts normally |
| `OAuth:Google:CallbackPath` | Path Google redirects back to. Must match an Authorized redirect URI on the OAuth client exactly | `/signin-google` |
| `OAuth:SuccessRedirectUrl` | Where the callback sends the browser after issuing a JWT | `http://localhost:5173/oauth/callback` |
| `OAuth:FailureRedirectUrl` | Where the callback sends the browser on cancel/failure | `http://localhost:5173/login` |

## Database

The .NET backend maps to the **same table/column names** Hibernate already uses in `../Backend`
(captured directly from the JPA entity annotations - e.g. `tour`, `roles`, `TOURCOST`, `users`,
snake_case columns), so it can point at the same MySQL database as the Java backend.

**Pointing at the existing database** (already created by the Java app, or from `../db/*.sql`):
just set `ConnectionStrings:Default` and run the app - do **not** run migrations against it. The
.NET app maps onto the existing tables; it doesn't own that schema's migration history.

**Starting from a fresh database**: create an empty `etour` schema, then:

```bash
cd dotnet-backend
dotnet ef database update --project src/eTour.Infrastructure --startup-project src/eTour.Api
```

BCrypt password hashes are compatible in both directions - a user created by the Java backend can
log in through the .NET backend and vice versa.

## Running

```bash
cd dotnet-backend
dotnet restore
dotnet build
dotnet run --project src/eTour.Api
```

By default this listens on the same port the Java backend uses (**8080**), so `etour-frontend`
works against either backend with **zero frontend changes** - just don't run both at once on the
same port. To run them side by side instead, point one at a different port:

```bash
# .NET backend on a different port, Java backend stays on 8080
dotnet run --project src/eTour.Api --urls http://localhost:8081
```

then set `VITE_API_BASE_URL=http://localhost:8081` in `etour-frontend/.env.local` for that session.

On startup (with `Seed:Enabled=true`, the default) the same idempotent demo data the Java backend
seeds gets created if missing: ADMIN/CUSTOMER roles, a demo admin login, a schedule/itinerary/
add-on backfill for every tour, and the "Europe" multipath demo category.

| Email | Password | Role |
|---|---|---|
| `admin.seed@etour.com` | `Admin@123` | ADMIN |

Swagger UI is available at `/swagger` in the Development environment.

## Running the frontend against it

```bash
cd etour-frontend
npm install
npm run dev
```

Opens on `http://localhost:5173`. `etour-frontend/.env.example` defaults `VITE_API_BASE_URL` to
`http://localhost:8080` - matching either backend's default port, so no frontend edit is needed
unless you moved the .NET backend to a different port as above.

## Tests

```bash
dotnet test tests/eTour.Tests/eTour.Tests.csproj
```

111 tests covering:
- `TourPricingCalculator` (the BRD 3.7 DOB-driven passenger pricing engine) - the heaviest
  coverage, since it's the most safety-critical logic in the app.
- `BookingService` / `PaymentService` business rules: seat availability, party-composition
  validation, DOB-vs-declared-band mismatches, ownership (404-not-403) checks, payment
  idempotency, card validation (Luhn/expiry/CVV), GST calculation consistency.
- `AuthService`, `MultipathGeneratorService`, the generic repository/service pair, `CardDetails`,
  and the `Occupancy`/`PassengerType` domain logic.
- Full-pipeline controller tests via `WebApplicationFactory` (real DI container, real middleware,
  an in-memory EF provider swapped in for MySQL) - including a register → login → authenticated
  request round trip.

Two genuine bugs were caught and fixed by this suite while porting: a generic-update path where
AutoMapper could zero out an entity's primary key, and a JWT signing-key/claim-mapping issue that
would have made every issued token fail to authenticate. Both are called out in code comments
where fixed.

### Wire-contract checks

`WireContractTests` and `TourCategoryContractTests` assert the *JSON property names* of the
payloads `../etour-frontend` consumes. They exist because this is the one class of porting bug
nothing else catches: rename `lineTotal` to `total` and the endpoint still returns 200 with a
well-formed body, every behavioural test still passes, and the only symptom is a blank number on
a page. Several such mismatches did ship this way (an empty cost breakdown on the booking review
screen, a tour's categories silently cleared on save, sub-category pages rendering empty).

To re-audit the whole surface rather than the pinned subset:

```bash
node tools/contract-diff.js
```

It diffs every Java DTO/entity's field names against the .NET equivalent and prints what each
side is missing. The header comment in that file explains how to read the output and lists the
handful of differences that are known-benign (fields Java declares but no code ever reads).

## OAuth social sign-in (Google)

Added on top of the existing email/password login — nothing about that path, the JWT format, or
any `[Authorize]` endpoint changed. The OAuth callback finishes by calling the **same**
`IJwtTokenService` the password login uses, so a token obtained via Google is an ordinary eTour
JWT and nothing downstream can tell the difference.

**Routes** (both `[AllowAnonymous]`):

| Route | Purpose |
|---|---|
| `GET /api/auth/oauth/google` | Starts sign-in — the frontend's "Continue with Google" button links here |
| `GET /api/auth/oauth/google/callback` | Issues the JWT and redirects back to the frontend |
| `GET /signin-google` | Google's redirect target, handled by the framework — register this exact URL in the Google console |
| `GET /api/auth/me` | Profile behind the caller's token (`[Authorize]`). The OAuth redirect can only carry the token, so this is how the client learns who signed in |

**Setup:** create an OAuth client (type *Web application*) in the Google Cloud console, add
`http://localhost:8080/signin-google` as an Authorized redirect URI, then supply the credentials
— preferably as environment variables rather than in `appsettings.json`:

```bash
OAuth__Google__ClientId=your-id.apps.googleusercontent.com OAuth__Google__ClientSecret=your-secret dotnet run --project src/eTour.Api
```

### "Error 400: redirect_uri_mismatch"

Google rejects the request before it ever reaches this app, so there is nothing in the server log
to diagnose it from — which is why the exact path is printed at startup instead:

```
Google sign-in enabled. Register this EXACT Authorized redirect URI on the Google OAuth client: http(s)://<your-host:port>/signin-google
```

The registered URI must match **byte for byte** — scheme, host, port and path, with no trailing
slash. For local development that is `http://localhost:8080/signin-google`. Common causes:

- Registering the frontend URL (`http://localhost:5173/...`) or the app's own callback action
  (`/api/auth/oauth/google/callback`) instead of `/signin-google`. Only `/signin-google` is what
  the handler asks Google for; the other route is an internal hop Google never sees.
- `127.0.0.1` vs `localhost`, or `https` vs `http` — Google treats these as different URIs.
- Registering it as an *Authorized JavaScript origin* rather than an *Authorized redirect URI*.
- Changes can take a few minutes to propagate on Google's side.

If you would rather match a URI that is already registered, set `OAuth:Google:CallbackPath` to it
instead of changing the console.

**Behaviour without credentials:** the whole handler registration is skipped, so the app starts
and runs exactly as before and the two routes return a 503 in the standard error envelope. This
is deliberate — `GoogleOptions.Validate()` throws on a blank client id, so registering
unconditionally would turn "OAuth not set up" into a startup crash for every deployment.

**How the token comes back:** the callback redirects to `OAuth:SuccessRedirectUrl` with the JWT in
the URL **fragment** (`#token=…`), which a page reads via `location.hash`. A fragment is never sent
to a server, so unlike a `?token=` query string it stays out of access logs, out of intermediate
proxies, and out of the `Referer` header of the next request. If `SuccessRedirectUrl` is blank the
callback instead returns the same JSON body as `/api/auth/login`, so the flow is usable and
testable before any UI exists. There is deliberately **no** caller-supplied `returnUrl`: the final
redirect carries the token, so honouring an arbitrary one would be an open redirect that leaks it.

**Account linking** is by the provider's verified email:

- Email already registered → signs into that account, keeping its existing role (an admin who uses
  Google stays an admin; linking never changes a role).
- Unknown email → provisions a `User` + `Customer` pair exactly as self-registration does, always
  with the `CUSTOMER` role — a role is never taken from the provider.
- Disabled account → rejected, same as password login.
- The provisioned row stores a BCrypt hash of random bytes, so the account cannot be logged into
  via `/api/auth/login` until the user sets a real password.

### Frontend side

This is the one feature where `../etour-frontend` **was** modified (on request — everything else in
this port leaves it untouched). Added there:

| File | Change |
|---|---|
| `components/domain/GoogleSignInButton.jsx` | New — the button, an `<a>` because it must be a full-page navigation, with the mark inlined as SVG so no third-party request is added to the login page |
| `pages/OAuthCallbackPage.jsx` | New — reads `location.hash`, exchanges the token for a session, redirects by role |
| `pages/LoginPage.jsx` | Button + an "or" divider; surfaces the backend's `?error=` as a toast and strips it from the URL |
| `pages/RegisterPage.jsx` | Same button, labelled "Sign up with Google" — the backend creates the account on first sign-in, so there is no separate signup route |
| `context/AuthContext.jsx` | New `loginWithToken(token)` alongside the existing `login`/`register`, which are unchanged |
| `services/authService.js`, `constants/apiEndpoints.js`, `constants/routes.js`, `routes/AppRoutes.jsx` | New endpoint/route wiring |

The callback page clears the fragment via `history.replaceState` as soon as it has read the token,
so it does not linger in the address bar or browser history, and clears the part-written session if
anything fails — a failed sign-in can never leave a token behind with no user attached.

### Trying it without Google credentials

The whole frontend path can be exercised without a Google client, since the callback only cares
about the token: get one from the password login and visit the callback URL directly.

```bash
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"admin.seed@etour.com","password":"Admin@123"}'
```

Then open `http://localhost:5173/oauth/callback#token=<the token>`.

Linking by **email** rather than by the stored provider id is deliberate, and stays that way now
that `../Backend` has added federated columns to the shared `users` table: linking by email is what
lets someone who registered with a password later click "Continue with Google" and land in the same
account. The trade-off is that it trusts the provider's email verification, which is safe for Google
and must be re-checked before adding any provider that does not verify emails.

The three columns `../Backend` owns — `auth_provider`, `google_sub`, `avatar_url`, all nullable —
are mapped here and written with the same rules as its `GoogleUserProvisioningService`:

| Case | `auth_provider` | `google_sub` | `avatar_url` |
|---|---|---|---|
| New account from Google | `GOOGLE` | provider's `sub` | provider's `picture` |
| Existing password account, first Google sign-in | stays `LOCAL` | recorded | updated |

Stamping an existing row `GOOGLE` would lose the fact that it also has a usable password, so the
marker is only written when the row has none. Because both backends write the same table, this
means an account's recorded provenance no longer depends on which backend created it.

## Client-side validation (requirement #6)

Per the project constraint, `../etour-frontend` is not modified. Server-side validation
(FluentValidation, one validator per request DTO in `eTour.Application/Validators/`) is the
enforced source of truth; the table below documents which existing frontend validation each rule
corresponds to, for anyone who does want to keep the two in sync by hand.

| Server rule | Frontend equivalent to check |
|---|---|
| Register/login email format, password length | `etour-frontend/src/pages` auth forms |
| Customer/passenger phone = 10 digits, nationality = 2-letter code | booking/passenger forms |
| Tour title/duration/price/code/status | admin tour edit form |
| Category code ∈ {DOM, ADV, INT}, `isFeatured` ∈ {Y, N} | admin category form |
| Card number (Luhn, 12-19 digits), expiry, CVV (3/4 digits by brand) | payment form |
| Booking passenger count vs adult/child composition | booking passenger step |

## Notes on intentional deviations from the Java version

- **Consistent error envelope**: the Java backend returns a bare `{field: message}` map for bean-
  validation errors but a `{timestamp, status, message, path}` envelope for everything else. The
  .NET port normalizes both to the same envelope (`ExceptionHandlingMiddleware` +
  `ValidationResultFactory`).
- **DTOs instead of raw entities**: several Java controllers (`Category`, `Tour`, `Review`, ...)
  serialize JPA entities directly, relying on Jackson annotations to avoid cycles. The .NET port
  uses an explicit response DTO everywhere, mapped via AutoMapper.
- **`Seed:Enabled` is an explicit switch** (default `true`, matching the Java app's always-on
  behavior) rather than unconditional, so it can be turned off for a shared/staging database.
