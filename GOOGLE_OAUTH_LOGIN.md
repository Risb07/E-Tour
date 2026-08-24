# Google OAuth 2.0 Sign-In

Adds "Continue with Google" alongside the existing email/password login.

**Nothing existing changed.** The password login, the JWT format, `SecurityConfig.java`
and every authorization rule are untouched. Google sign-in is a second way to obtain
the *same* eTour JWT, and it stays completely switched off until you configure
credentials.

---

## 1. How it works

The **authorization code flow, driven by the backend**. The client secret never
reaches the browser.

```
Browser                 eTour backend                  Google
   |                         |                            |
   |  click "Continue with Google"                        |
   |------------------------>|                            |
   |     GET /oauth2/authorization/google                  |
   |                         |---- 302 to consent screen ->|
   |                         |                            |
   |<-------------------- user signs in & consents --------|
   |                         |                            |
   |     GET /login/oauth2/code/google?code=...&state=...  |
   |------------------------>|                            |
   |                         |-- exchange code for tokens->|
   |                         |<- ID token + userinfo ------|
   |                         |                            |
   |                    [verify ID token]                  |
   |                    [link or create eTour user]        |
   |                    [mint the same JWT as /api/auth/login]
   |                         |                            |
   |<-- 302 /oauth2/callback?token=...&email=...           |
   |                         |                            |
   |  SPA stores the token exactly like a password login   |
```

From the moment the SPA has the token, the two sign-in routes are indistinguishable:
same `sessionStorage` keys, same `Authorization: Bearer` header, same
`JwtAuthenticationFilter`, same roles.

---

## 2. Setup

### Create Google credentials

1. [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services** → **Credentials**
2. **Create Credentials** → **OAuth client ID** → application type **Web application**
3. Under **Authorised redirect URIs** add exactly:

   ```
   http://localhost:8080/login/oauth2/code/google
   ```

   For production use your real backend host over HTTPS. This URI points at the
   **backend**, not the React app — a common mistake is to enter the frontend URL here.
4. Copy the **Client ID** and **Client secret**.

### Start the backend with the credentials

Windows (PowerShell):

```powershell
$env:GOOGLE_CLIENT_ID="....apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="...."
mvn spring-boot:run
```

macOS / Linux:

```bash
export GOOGLE_CLIENT_ID=....apps.googleusercontent.com
export GOOGLE_CLIENT_SECRET=....
mvn spring-boot:run
```

That is the whole setup. The frontend needs no configuration — it asks the backend
at `GET /api/auth/providers` whether Google is available and shows or hides the
button accordingly.

### Configuration reference

| Property | Env var | Default | Purpose |
|---|---|---|---|
| `app.oauth2.google.client-id` | `GOOGLE_CLIENT_ID` | *(empty)* | Empty = feature off |
| `app.oauth2.google.client-secret` | `GOOGLE_CLIENT_SECRET` | *(empty)* | Empty = feature off |
| `app.oauth2.google.redirect-uri` | `GOOGLE_REDIRECT_URI` | `{baseUrl}/login/oauth2/code/google` | Must match the Console entry |
| `app.oauth2.frontend-redirect-uri` | `OAUTH2_FRONTEND_REDIRECT_URI` | `http://localhost:5173/oauth2/callback` | Where the SPA receives the JWT |

For production, set `OAUTH2_FRONTEND_REDIRECT_URI` to your deployed frontend and make
sure that origin is also in `CORS_ALLOWED_ORIGINS`.

---

## 3. Account behaviour

| Situation | Result |
|---|---|
| Email already has an eTour account | Linked and logged in. Bookings, cart and wishlist are preserved. The existing password keeps working. |
| New email | A `CUSTOMER` user and its `Customer` profile row are created, exactly as self-registration does. |
| Account disabled (`status = false`) | Refused, same as password login. |
| Google reports `email_verified: false` | Refused. |

Linking is by verified email. Google confirms ownership of the address before we
trust it, which is why the unverified case is rejected rather than linked.

Two notes on Google-created accounts:

- **Password** — they get a random, discarded 32-byte secret as their hash, so
  `/api/auth/login` can never succeed for them. There is no plaintext that produces
  that hash. If such a user wants a password, add a normal reset flow.
- **Phone** — Google does not supply one, and `Customer.phone` requires 10 digits, so
  new rows start at `0000000000`. The customer replaces it on their profile or during
  booking.

---

## 4. Why a second security filter chain

`OAuth2SecurityConfig` registers its own `SecurityFilterChain` at `@Order(1)` matching
only `/oauth2/**` and `/login/oauth2/**`, instead of adding `.oauth2Login()` to the
existing chain. Three reasons:

1. **`SecurityConfig.java` is not edited at all** — every rule, the JWT filter and the
   CORS setup stay byte-for-byte as they were.
2. **`/api/**` stays stateless.** The authorization code flow has to keep the `state`
   parameter and PKCE verifier in an HTTP session between the redirect out and the
   callback. Confining that to two URLs preserves `SessionCreationPolicy.STATELESS`
   everywhere else.
3. **API error behaviour is unchanged.** `.oauth2Login()` on the main chain would
   replace its authentication entry point, so unauthenticated `/api/**` calls would
   start returning a 302 to Google instead of the status code the React client
   already handles.

---

## 5. Files

**New — backend** (`com.etour.security.oauth2`)

| File | Role |
|---|---|
| `OAuth2SecurityConfig` | The `/oauth2/**` filter chain |
| `GoogleOAuth2ClientConfig` | Builds the Google `ClientRegistration` |
| `GoogleOAuthEnabledCondition` | Keeps everything off when credentials are blank |
| `GoogleUserProvisioningService` | Link-or-create rules (the only class that writes to the DB) |
| `GoogleOidcUserService` | The path that actually runs (`openid` scope ⇒ OIDC) |
| `GoogleOAuth2UserService` | Non-OIDC fallback, same rules |
| `EtourOidcUser` / `EtourOAuth2User` / `EtourOAuthPrincipal` | Carry the resolved user through the flow |
| `OAuth2AuthenticationSuccessHandler` | Mints the JWT, redirects to the SPA |
| `OAuth2AuthenticationFailureHandler` | Redirects to the SPA with a readable message |
| `controller/AuthProvidersController` | `GET /api/auth/providers` |

**New — frontend**

- `pages/OAuth2CallbackPage.jsx`
- `components/common/GoogleSignInButton.jsx`

**Modified** — additive only

- `pom.xml` — one dependency
- `application.properties` — one config block
- `entity/User.java` — three nullable columns (`auth_provider`, `google_sub`, `avatar_url`)
- `AuthContext.jsx` — new `loginWithToken` action
- `authService.js`, `apiEndpoints.js`, `routes.js`, `AppRoutes.jsx` — new entries
- `LoginPage.jsx`, `RegisterPage.jsx` — the button

The three new `users` columns are nullable with a `LOCAL` default, so existing rows
stay valid and `ddl-auto=update` adds them without a migration.

---

## 6. Troubleshooting

| Symptom | Cause |
|---|---|
| Button never appears | Credentials not set. Check `GET /api/auth/providers` returns `{"google": true}`. |
| `redirect_uri_mismatch` from Google | The Console URI must be the **backend** callback, character for character, including scheme and port. |
| Lands on the SPA with `?error=...` | Expected failure path — the message is shown as a toast. |
| Everyone logged out after a restart | Unrelated to OAuth: `JWT_SECRET` is unset, so `JwtService` generates a random key per process. |
