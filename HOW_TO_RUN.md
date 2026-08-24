# eTour — How to run it

## What was actually wrong

Your build was **fine** — I checked `Backend/target/classes` and all 213 classes are compiled, including every file added in earlier sessions. There were no compile errors.

The app wouldn't **start**, and that was my fault. In the security pass I removed the defaults from three properties:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

With no default and no environment variable set, Spring can't resolve the placeholder and aborts at startup with:

```
Could not resolve placeholder 'DB_USERNAME' in value "${DB_USERNAME}"
```

I was protecting against a real problem (your DB password was committed to git) but I overcorrected: I made the app impossible to run without configuration it didn't tell you about. Fixed now.

---

## Run it

### 1. MySQL
Make sure MySQL is running. You don't need to create the schema by hand — the
connection URL now includes `createDatabaseIfNotExist=true`.

### 2. Set your DB password
`application.properties` now defaults to `root` with an **empty** password. If
yours differs, do one of:

**Option A — edit the default** in `Backend/src/main/resources/application.properties`:
```properties
spring.datasource.password=${DB_PASSWORD:YOUR_PASSWORD_HERE}
```

**Option B — set an env var** (keeps the password out of git — preferred):
```powershell
# PowerShell
$env:DB_PASSWORD="YOUR_PASSWORD_HERE"
```
```bash
# macOS / Linux
export DB_PASSWORD=YOUR_PASSWORD_HERE
```

### 3. Start the backend
```bash
cd Backend
mvn spring-boot:run
```
It should now start on **http://localhost:8080**. On first run the seeder creates roles, a demo admin, and sample tours.

### 4. Start the frontend
```bash
cd etour-frontend
npm install
npm run dev
```
Opens on **http://localhost:5173**.

### 5. Log in
Seeded admin account (from `DemoDataSeederService`):

| Email | Password |
|---|---|
| `admin.seed@etour.com` | `Admin@123` |

Or register a new customer account through the UI.

---

## Expected startup warning

```
=================================================================
jwt.secret is not set - generated a TEMPORARY random signing key.
The application will run, but every restart logs all users out.
```

This is **normal** with no `JWT_SECRET` set. The app works; you'll just have
to log in again after each restart. To stop the warning and keep sessions
across restarts:

```bash
openssl rand -base64 48        # copy the output
export JWT_SECRET=<paste>      # or set it in your IDE run config
```

I chose a random key over a hardcoded fallback deliberately — a default
signing key sitting in the source would let anyone mint an admin token.

---

## Still an important security note

Your old MySQL password (`Saraf@123`) and the previous JWT fallback key are
**still in your git history**, even though they're no longer in the working
tree. If this repo is pushed anywhere shared, change the MySQL password and
generate a new JWT secret.

---

## Other fixes in this pass

| Issue | Fix |
|---|---|
| App couldn't start without env vars | Working local defaults restored for DB + JWT, still overridable |
| No `jwt.secret` → hard crash | Falls back to a per-process random key with a loud warning |
| `GET /api/reviews/customer/{id}` returned **404** for a customer with no reviews | Now returns `[]` — an empty list isn't an error |
| Lazy-loading 500s on entity endpoints | `spring.jpa.open-in-view=true` set explicitly |
| Errors returned a bare status with no message | `server.error.include-message=always` |
| Schema had to be created manually | `createDatabaseIfNotExist=true` in the JDBC URL |
| Stray `;;` in `TourController` imports | Cleaned |

## Checks run across the whole backend

- 146 endpoints across 29 controllers — **no duplicate/ambiguous mappings** (a hard Spring startup failure)
- **Every repository call** resolves to a declared method or a JpaRepository built-in
- **Every derived query** property path resolves to a real entity field
- **Every `com.etour.*` import** resolves to a real file
- Every service interface has exactly one `@Service` implementation — no missing or ambiguous beans
- **Every frontend API path exists on the backend** (all 40+ checked)
- All `Optional.get()` calls are guarded by `isPresent()`

## Known non-blocking behaviour

Logging in as **admin** and hitting customer-only endpoints (`/api/bookings/me`,
`/api/cart`, `/api/wishlist`) returns *"No customer profile for this account"*.
That's correct — admin accounts intentionally have no `Customer` row. The
frontend already routes admins to the admin UI, so you'll only see this by
calling those endpoints directly.
