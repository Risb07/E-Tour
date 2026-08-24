# eTour Backend

Spring Boot 4 / Java 17 / MySQL 8 backend for the eTour Management System, built against
`VITA - BRD - eTour v1.3` and the Group 11 DB design.

## 1. Setup

### Prerequisites
- Java 17
- Maven
- MySQL 8 running locally (or update `spring.datasource.url`)

### Required environment variables
```
DB_USERNAME=root
DB_PASSWORD=<your mysql password>
JWT_SECRET=<a long random base64 string - e.g. `openssl rand -base64 48`>
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```
Set these in your shell, IDE run config, or a `.env`-style mechanism your OS supports — do **not**
put real secrets back into `application.properties`.

### Seed data (required before first run)
The `roles` table needs at least these two rows before anyone can register or log in:
```sql
INSERT INTO roles (role_name, description) VALUES ('ADMIN', 'System administrator');
INSERT INTO roles (role_name, description) VALUES ('CUSTOMER', 'Customer / traveller');
```
There is no seeded admin user by design (self-registration only ever creates CUSTOMER accounts,
which is a deliberate security fix — see section 4). Promote a user to ADMIN directly in the
database for your first admin account:
```sql
UPDATE users SET role_id = (SELECT role_id FROM roles WHERE role_name = 'ADMIN') WHERE email = 'you@example.com';
```

### Run
```
cd Backend
mvn spring-boot:run
```
API base URL: `http://localhost:8080`

## 2. React integration notes
- CORS is open to `CORS_ALLOWED_ORIGINS` (comma-separated) with credentials enabled.
- JWT is returned from `POST /api/auth/login` as `{ token, message }`. Send it back as
  `Authorization: Bearer <token>` on every subsequent request.
- Uploaded files (tour media, brochures) are served statically from `/uploads/**`.
- All list/detail responses are DTOs for the newer modules (Booking, Cart, Payment, Invoice,
  Passenger, tour sub-resources). A few older modules still return JPA entities directly — see
  "Known simplifications" below before you build UI against them, since their shape may still
  change.

## 3. Endpoint map (by BRD module)

| Module | Base path | Notes |
|---|---|---|
| Auth | `/api/auth/login`, `/api/users/register` | Public |
| Category | `/api/categories` | Public GET, admin write |
| Tour | `/api/tours` | Public GET, admin write |
| Tour sub-resources | `/api/tours/{id}/journey`, `/stay-meals`, `/content`, `/media`, `/addons` | Public GET, admin write |
| Tour Schedule | `/api/tour-schedules` | |
| Tour Cost | — | entity/repo exist, controller not yet built |
| Search | `/api/tours/search` | filters + pagination + sorting |
| **Sector / Sub-Sector / Product** | `/api/sectors`, `/api/sub-sectors`, `/api/products` | Public GET, admin write; BRD 3.2/3.3/3.4 drill-down |
| **Showcase / Ad banners** | `/api/ad-banners` | Public GET, admin write; BRD 3.1 |
| **Crawling ticker** | `/api/crawling-text` | Public GET, admin write; BRD 3.2 |
| **Nav menu** | `/api/nav-menu` | Public GET, admin write; BRD 2.1 |
| **Content / multilingual** | `/api/content` | Public GET, admin write; BRD 2.1 |
| Cart | `/api/cart`, `/api/cart/{id}/checkout` | Requires login |
| Booking | `/api/bookings`, `/api/bookings/me` | Ownership enforced server-side |
| Passenger | `/api/passengers` | Ownership enforced via booking |
| Payment | `/api/payments` | **Simulated gateway** — see below |
| Invoice | `/api/invoices/me`, `/api/invoices/booking/{id}` | Auto-generated on payment |
| Review | `/api/reviews` | |
| Excel Upload | `/api/excel-upload` (multipart, admin-only) | See column format below |
| Admin Dashboard | `/api/admin/dashboard` | |
| Locations/Content (CMS) | `/api/locations`, `/api/content` | `/api/content` now controller-exposed |

### Sector / Sub-Sector / Product drill-down (BRD 3.2 → 3.4)
The Home page sector icons and the Home → Sector → Sub-Sector → Product navigation map to:
- `GET /api/sectors` (active sectors, ordered), `GET /api/sectors/{id}` (with sub-sectors),
  `GET /api/sectors/{id}/sub-sectors`, `GET /api/sectors/{id}/products`
- `GET /api/sub-sectors?sectorId=`, `GET /api/sub-sectors/{id}` (with products),
  `GET /api/sub-sectors/{id}/products`
- `GET /api/products?subSectorId=`, `GET /api/products/{id}`

These sit on top of the pre-existing `sector` / `sub_sector` / `tour_product` tables that the DB
design shipped with but no controller exposed. Writes are admin-only; reads are public.

### Excel bulk upload format
Header row + data from row 2, columns in this order:
`title | description | durationDays | basePrice | tourCode (ADV/INT/DEV/DOM) | categoryName | status`

Response includes `totalRows`, `successRows`, `failedRows`, and a per-row `errors` list
(duplicate titles, missing required fields, unknown category, etc. — the whole file isn't
rejected for one bad row).

## 4. Fixes made to the inherited codebase
This project started as an existing team codebase. Before adding new modules, the following
were fixed (see conversation history for full detail if you have it, otherwise this is the
summary):
- `SecurityConfig` had a `permitAll("/api/**")` rule that made every other rule dead code —
  fixed to most-specific-first ordering.
- JWT secret and DB password were hardcoded/committed in plaintext — moved to env vars.
  **The old DB password was already committed to git history and should be rotated.**
- `POST /api/users/register` accepted a client-supplied `roleId` (self-service admin signup) and
  returned the full `User` entity including the password hash — fixed to hardcode CUSTOMER and
  return a DTO.
- `Customer` had no link to `User` despite the DB design specifying one — added, with
  auto-provisioning on registration.
- `Booking` used raw `Long` FK columns instead of real relationships, and trusted a
  client-supplied `customerId` (IDOR) — rebuilt with real `@ManyToOne` relations and
  server-derived ownership from the JWT principal.
- Dead commented-out code, stray `System.out.println`/`printStackTrace()`, and exception
  messages leaking to clients in the 500 handler were cleaned up.

### Round 2 fixes (schema realignment + error semantics)
A follow-up API audit found the app 500'd on several endpoints that touched legacy DB tables
or fell through the generic exception handler. All are fixed in the current tree:

- **`TourCode` was missing `DOM`** (DB enum already had it) — every read touching a DOM tour
  (`GET /api/tours`, search, dashboard, DOM tour by id) crashed. Added to the Java enum.
- **Default JWT secret was too short for HS256** (216 bits) — login always crashed with
  `WeakKeyException`. `JwtService` now derives a valid >=256-bit key from the configured value
  (base64-tolerant, SHA-256 fallback), and the default is a proper 256-bit base64 secret.
  Override via the `JWT_SECRET` env var.
- **Legacy DB schema didn't match the entities** — the `booking`, `passenger`, `invoice` and
  `tour` tables carried unmapped NOT NULL columns (and `booking` was missing `booking_date`),
  so insert flows (booking, passengers, invoices, `POST /api/tours`, Excel upload) crashed.
  An **idempotent migration** in `db/migration/V2__align_schema.sql` drops the legacy columns,
  adds `booking_date`, and gives `tour.reviews` a default. Apply it once with:
  ```sql
  source db/migration/V2__align_schema.sql;   -- inside a mysql client on the etour DB
  ```
- **Error semantics**: `GlobalExceptionHandler` now maps `BadCredentialsException` -> 401,
  `AccessDeniedException`/`AuthorizationDeniedException` -> 403, `EntityNotFoundException` ->
  404, `DataIntegrityViolationException` -> 409, and unmapped routes -> 404 (previously all 500).
- **Categories were not actually public**: the GET endpoints carried a `@PreAuthorize` that
  overrode the `permitAll` in `SecurityConfig` for anonymous browsing — removed.
- **IDOR on `GET/PUT /api/customer/{id}`**: any authenticated user could read/update any
  customer's PII. Ownership is now enforced server-side (own profile or admin; others get 404).
  Admin `POST /api/customer` now takes a `CustomerRequest` DTO (userId + profile fields) and
  returns 404/409 instead of 500 for bad/duplicate links.
- **`DELETE /api/tours/{id}` / schedules 500'd** on FK conflicts. Child content tables now
  use `ON DELETE CASCADE` (see migration); deleting a tour/schedule that still has bookings or
  carts returns **409** instead of 500.

### Round 3 fixes (BRD gap closure)
An audit against `BRD v1.3` section 3 found several modules had DB tables but **no API**, and one
explicitly-documented search gap. All addressed:

- **Showcase / ad banners (3.1)**: new `AdBanner` entity + `/api/ad-banners` — banners are fully
  DB-driven (title, image_url, link_url, position, status), no hard-coded media.
- **Home / Sector / Sub-Sector / Product (3.2–3.4)**: new `Sector`, `SubSector`, `TourProduct`
  entities + `/api/sectors`, `/api/sub-sectors`, `/api/products` covering the full drill-down.
  The old `sector`/`sub_sector`/`tour_product` tables (which had rows but no Java mapping) are
  now wired up.
- **Crawling ticker (3.2)**: new `CrawlingText` entity + `/api/crawling-text` (multi-item,
  ordered, DB-managed).
- **Nav menu + content (2.1)**: new `NavMenuItem` entity + `/api/nav-menu` (parent/child for
  mouse-over menus), and `/api/content` is now controller-exposed with `language_code`
  filtering for the multilingual requirement.
- **Search on Period (3.6)**: `/api/tours/search` now accepts `startDate` / `endDate` and lists
  tours that have at least one schedule departing on/after `startDate` and returning on/before
  `endDate`, implemented as a `TourSchedule` subquery (no schema change).
- **Unique order number (3.7)**: `booking.order_number` added (migration
  `db/migration/V3__booking_order_number.sql`), generated server-side on successful payment
  (e.g. `ORD-4-40BE56F3`) and returned in `BookingResponse`. The invoice number already
  existed as the receipt identifier.
- **Error-handling gaps**: previously several failure modes fell through to the generic 500.
  `GlobalExceptionHandler` now maps `ConstraintViolationException` (persist-time bean
  validation, e.g. missing required field on save) -> 400 with a field map,
  `HttpMessageNotReadableException` (malformed JSON body) -> 400, and
  `MethodArgumentTypeMismatchException` (bad date/enum/number in path/query params) -> 400.
- **Partial-write bug on registration**: `POST /api/users/register` without a `phone` created a
  `users` row then failed on the `customer` insert (orphan account). `phone` is now
  `@NotBlank` + 10-digit on `RegisterRequest`, so the request is rejected with a 400 field map
  before anything is written.
- **Maven build warning**: `pom.xml` declared `maven-compiler-plugin` twice — merged into one
  plugin declaration (`<parameters>true</parameters>` combined with the Lombok
  annotation-processor executions).

## 5. Known simplifications / honest scope notes
Be aware of these before treating this as a finished production backend:

- **Payment is simulated.** `POST /api/payments` immediately marks the payment SUCCESS and
  confirms the booking — there's no real gateway integration (Razorpay/Stripe/etc). The code is
  structured so swapping in a real gateway means changing `PaymentServiceImpl` and adding a
  webhook endpoint, not restructuring the module.
- **Cart requires login.** The BRD's staging-cart concept allows an anonymous `session_token`
  before login; this implementation only supports the logged-in path.
- **Category/Tour/TourSchedule/TourCost/Review/Customer controllers still return JPA entities
  directly** rather than DTOs, unlike the newer modules (Booking/Cart/Payment/Invoice/Passenger/
  tour sub-resources). They're functional and guarded against lazy-loading crashes with
  `@JsonIgnoreProperties`, but retrofitting them to DTOs is the natural next step for API
  stability and to fully close the "never expose entities" rule from the architecture brief.
- **Tour Cost has no controller yet** — entity, repository, and validation exist; CRUD endpoints
  don't.
- **Search supports Period filtering** via `startDate`/`endDate` (see Round 3). The result list
  returns `Tour` entities; per-tour schedule dates are fetched with
  `GET /api/tour-schedules/tour/{tourId}`.
- **GST/tax rate on invoices is a hardcoded 5%** constant, not a configurable value — the DB
  design doesn't include a tax-rate table.
- **Receipt is not e-mailed and no PDF is generated.** BRD 3.7 wants a PDF receipt e-mailed to
  the user on payment; the backend stores the unique order number + invoice and exposes them via
  API, but no mail transport or PDF generator is wired in (no mail/PDF dependency). This is the
  main remaining Book-Tour gap.
- **Admin dashboard stats load full lists into memory** to compute counts rather than using
  aggregate SQL queries — fine at prototype scale, worth revisiting with real data volume.
- **Automated tests** now cover passenger age banding, the tour-detail tab services and the
  exception-to-HTTP-status mapping — 36 JUnit 5 tests, run with `mvn test` against an in-memory
  H2 database (no MySQL needed). Controller, repository and booking/payment service layers are
  still untested. See [TESTING.md](TESTING.md) for the full breakdown and the remaining gaps.

## 6. Suggested next steps, in order
1. `mvn clean install`, fix whatever the compiler finds.
2. Seed roles, register a test user, promote to ADMIN, confirm login works end-to-end.
3. Walk the purchase flow manually: browse tours → add to cart → checkout → pay → view invoice.
4. Retrofit Category/Tour/TourSchedule/TourCost/Review to DTOs (pattern is established in
   `BookingResponse`/`CartResponse` — copy that shape).
5. Add the TourCost controller.
6. Wire a real payment gateway + PDF receipt + e-mail on payment (closes the last BRD 3.7 gap).
