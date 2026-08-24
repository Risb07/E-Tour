# eTour — Pre-Production Code Audit

Spring Boot + React + MySQL + JWT + Vite + Tailwind
Scope: full backend, full frontend, end-to-end flow, business rules, security, performance.

**Verdict: NOT production ready.** One Critical revenue defect and five High-severity issues must be
fixed first. Everything else is Medium/Low and can be scheduled.

| Severity | Count |
|---|---|
| Critical | 1 |
| High | 5 |
| Medium | 9 |
| Low | 4 |

---

# CRITICAL

## C-1 — GST is displayed and invoiced but never charged

**Severity:** Critical
**Location:** `service/impl/PaymentServiceImpl.java` → `charge(...)`, cross-checked against
`getPaymentSummary(...)` and `service/impl/InvoiceServiceImpl.java` → `generate(...)`

**Problem**
Three places disagree about what the customer owes.

`getPaymentSummary` builds the figure the UI shows:

```java
BigDecimal subTotal  = booking.getTotalAmount();      // pre-tax
BigDecimal taxable   = subTotal.subtract(discount);
BigDecimal tax       = taxable.multiply(gstRate);
dto.setGrandTotal(taxable.add(tax));                  // <-- shown as "Grand total"
```

`PaymentPage.jsx` renders `Pay {formatCurrency(summary.grandTotal)}`.

But `charge(...)` bills the **pre-tax subtotal**:

```java
GatewayResult result = paymentGateway.charge(new PaymentGateway.GatewayChargeRequest(
        booking.getBookingId(),
        booking.getTotalAmount(),      // <-- subtotal, no GST
        ...));
payment.setAmount(booking.getTotalAmount());   // <-- subtotal
```

…while `InvoiceServiceImpl.generate` records `totalAmount = subTotal + tax`.

**Impact**
Every single booking under-charges by the GST rate (5% by default). `payment.amount` never equals
`invoice.total_amount`, so the books do not reconcile and the PDF receipt states a total that was
never collected. This is both a direct revenue loss and a tax-compliance exposure.

**Fix** — compute the payable amount once and use it for the gateway, the payment row and the invoice.

In `PaymentServiceImpl`, add a helper and use it in `charge(...)`:

```java
/** The amount actually payable: pre-tax subtotal plus GST. Must match getPaymentSummary(). */
private BigDecimal payableAmount(Booking booking) {
    BigDecimal subTotal = booking.getTotalAmount() == null ? BigDecimal.ZERO : booking.getTotalAmount();
    BigDecimal tax = subTotal.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);
    return subTotal.add(tax);
}
```

```java
private PaymentResponse charge(Booking booking, String paymentMethod, CardDetails card) {

    BigDecimal payable = payableAmount(booking);          // ADD

    GatewayResult result = paymentGateway.charge(new PaymentGateway.GatewayChargeRequest(
            booking.getBookingId(),
            payable,                                      // CHANGED
            paymentMethod,
            booking.getCustomer().getEmail(),
            card));
    ...
    payment.setAmount(payable);                           // CHANGED
```

`InvoiceServiceImpl.generate` already derives `total = subTotal + tax` from the same `gstRate`
property, so it needs no change once the payment matches it.

> Verify both classes read the same `app.invoice.gst-rate` property — they do today
> (`@Value("${app.invoice.gst-rate:0.05}")` in each).

---

# HIGH

## H-1 — Registration is not transactional; a failure orphans the user account

**Severity:** High
**Location:** `service/impl/UserServiceImpl.java` → `register(...)`

**Problem**
The method performs three writes with no transaction boundary: save `User`, then save `Customer`,
then issue a JWT. With `ddl-auto=update` and default autocommit, the `User` row commits immediately.

**Impact**
If the `Customer` insert fails (constraint, connection blip), the user account exists and can log in,
but `CurrentUserProvider.currentCustomer()` throws `ResourceNotFoundException` forever. That account
can never book, add to cart, review or wishlist, and re-registering is blocked by the duplicate-email
check. Unrecoverable without manual DB surgery.

**Fix**

```java
import org.springframework.transaction.annotation.Transactional;

@Override
@Transactional                                  // ADD
public RegisterResponse register(RegisterRequest request) {
```

## H-2 — Concurrent payment requests can double-charge a booking

**Severity:** High
**Location:** `service/impl/PaymentServiceImpl.java` → `requirePayableBooking(...)`

**Problem**
The booking is read with a plain `findById` and the "already paid" guard is a status read:

```java
Booking booking = bookingRepository.findById(bookingId)...
if (booking.getBookingStatus() == BookingStatus.CONFIRMED) { throw ... }
```

Two requests (double-click, retry, duplicate tab) both read `PENDING`, both pass the guard, and both
call the gateway.

**Impact**
Customer charged twice. Two `Payment` rows and **two `Invoice` rows** for one booking — which then
breaks `InvoiceRepository.findByBooking_BookingId` (returns `Optional`) with
`NonUniqueResultException`, so the receipt endpoint and `getByBooking` start throwing 500s for that
booking permanently.

**Fix** — take a pessimistic lock, mirroring what `TourScheduleRepository.findByIdForUpdate` already
does for seat inventory.

`repository/BookingRepository.java`:

```java
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select b from Booking b where b.bookingId = :id")
Optional<Booking> findByIdForUpdate(@Param("id") Long id);
```

`PaymentServiceImpl.requirePayableBooking`:

```java
Booking booking = bookingRepository.findByIdForUpdate(bookingId)     // CHANGED
        .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
```

Both `recordPayment` and `payWithCard` are already `@Transactional`, so the lock is held for the
whole charge.

## H-3 — SMTP debug logging is enabled in committed configuration

**Severity:** High
**Location:** `src/main/resources/application.properties` (last four lines)

**Problem**

```properties
logging.level.org.springframework.mail=DEBUG
logging.level.org.springframework.mail.javamail=DEBUG
logging.level.org.eclipse.angus.mail=DEBUG
logging.level.org.eclipse.angus.smtp=DEBUG
```

Angus/JavaMail debug output writes the raw SMTP conversation to the log, including the
`AUTH PLAIN`/`AUTH LOGIN` exchange.

**Impact**
The `MAIL_PASSWORD` (base64-encoded, trivially reversible) is written to application logs on every
receipt email. Anyone with log access — including a shipped log aggregator — obtains the mail
account credentials. Also leaks every customer's email address at DEBUG level.

**Fix** — delete the four lines, or gate them behind a dev profile:

```properties
# (removed - SMTP debug logs the AUTH exchange, which contains the mail password)
```

## H-4 — Deactivated accounts keep full access until their token expires

**Severity:** High
**Location:** `security/JwtAuthenticationFilter.java` → `doFilterInternal(...)`

**Problem**
`CustomUserDetails.isEnabled()` correctly returns `user.getStatus()`, but the filter constructs the
`Authentication` by hand and never consults it:

```java
if (jwtService.isTokenValid(token, userDetails)) {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(...);
```

`isEnabled()` is only checked by `DaoAuthenticationProvider`, which is on the login path only.

**Impact**
Setting `users.status = false` to suspend an account (fraud, abuse, offboarding) has **no effect**
for up to 24 hours (`jwt.expiration-ms` default). The suspended user keeps booking and paying.

**Fix**

```java
if (jwtService.isTokenValid(token, userDetails) && userDetails.isEnabled()) {   // CHANGED
```

`User.status` is `nullable = false` with a `true` default, so no null guard is needed.

## H-5 — Admin booking list is an N+1 with no pagination

**Severity:** High
**Location:** `service/impl/BookingServiceImpl.java` → `getAllBookings()`

**Problem**

```java
return bookingRepository.findAll().stream()
        .map(b -> toResponse(b,
                bookingAddonRepository.findByBooking_BookingId(b.getBookingId()),
                passengerRepository.findByBooking_BookingId(b.getBookingId()), null))
        .toList();
```

`1 + 2N` queries, plus lazy `customer` and `schedule.tour` hydration per row inside `toResponse`
(masked by `open-in-view=true`), and the entire booking table is loaded into memory.

**Impact**
At 10,000 bookings this is ~40,000 queries in one request. The admin bookings page will time out and
can exhaust the connection pool, taking the whole application down — not just the admin screen.

**Fix** — batch the two child lookups into one query each, keyed by booking id.

`PassengerRepository` / `BookingAddonRepository`:

```java
List<Passenger> findByBooking_BookingIdIn(List<Long> bookingIds);
```

```java
List<BookingAddon> findByBooking_BookingIdIn(List<Long> bookingIds);
```

`getAllBookings()`:

```java
List<Booking> bookings = bookingRepository.findAll();
List<Long> ids = bookings.stream().map(Booking::getBookingId).toList();

Map<Long, List<Passenger>> paxByBooking = passengerRepository.findByBooking_BookingIdIn(ids)
        .stream().collect(Collectors.groupingBy(p -> p.getBooking().getBookingId()));
Map<Long, List<BookingAddon>> addonsByBooking = bookingAddonRepository.findByBooking_BookingIdIn(ids)
        .stream().collect(Collectors.groupingBy(a -> a.getBooking().getBookingId()));

return bookings.stream()
        .map(b -> toResponse(b,
                addonsByBooking.getOrDefault(b.getBookingId(), List.of()),
                paxByBooking.getOrDefault(b.getBookingId(), List.of()), null))
        .toList();
```

Pagination is the fuller fix (see M-1) but this alone removes the collapse risk.

---

# MEDIUM

## M-1 — Unbounded `findAll()` on customer-facing and admin collections

**Severity:** Medium
**Location:** `TourServiceImpl.getAllTours()`, `ReviewServiceImpl.getAllReviews()`,
`CustomerServiceImpl` (`findAll`), `MultipathGeneratorServiceImpl` (line 77),
`BookingServiceImpl.getAllBookings()`

**Problem** No `Pageable` anywhere except `NewsletterServiceImpl`. `GET /api/tours` returns every
tour, serialising the `categories` many-to-many per row.

**Impact** Response size and query time grow linearly with the catalogue; memory pressure and slow
first paint on the listing page.

**Fix** Add paged variants without removing the existing methods, so no current caller breaks:

```java
// TourController
@GetMapping(params = "page")
public ResponseEntity<Page<Tour>> getToursPaged(Pageable pageable) {
    return ResponseEntity.ok(tourRepository.findAll(pageable));
}
```

Then move the frontend listing pages onto the paged endpoint.

## M-2 — A paid, confirmed booking can be cancelled with no refund path

**Severity:** Medium
**Location:** `service/impl/BookingServiceImpl.java` → `cancelBooking(...)`

**Problem** The only guard is "already cancelled". A `CONFIRMED` booking — one with a successful
`Payment` and a generated `Invoice` — can be cancelled by the customer. Seats are returned to
inventory; the payment and invoice are left untouched.

**Impact** Money is retained against a cancelled booking with no refund record, and the invoice now
refers to a cancelled booking. Accounting and customer-support inconsistency.

**Fix** — minimal: refuse, and route paid cancellations through support until a refund flow exists.

```java
if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
    throw new IllegalOperationException(
            "This booking is already paid. Contact support to request a cancellation and refund.");
}
```

## M-3 — Tour create/update bind the JPA entity directly with no validation

**Severity:** Medium
**Location:** `controller/TourController.java` → `createTour(...)`, `updateTour(...)`

**Problem**

```java
public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
```

No `@Valid`, and the entity — including `tourId`, `categories` and the cascading `tourCosts`
collection — is bound straight from JSON.

**Impact** Admin-only, so not a privilege issue, but a malformed body can overwrite an unintended
row via a supplied `tourId`, or cascade-insert `tourCosts`. Bean constraints only fire at flush,
surfacing as a late `ConstraintViolationException` mid-transaction.

**Fix** — minimal, without introducing a DTO:

```java
public ResponseEntity<Tour> createTour(@Valid @RequestBody Tour tour) {
    tour.setTourId(null);           // never trust a client-supplied id on create
    ...
```

```java
public ResponseEntity<Tour> updateTour(@PathVariable Long id, @Valid @RequestBody Tour tour) {
```

## M-4 — `/uploads/**` is public and accepts any file type

**Severity:** Medium
**Location:** `config/WebConfig.java`, `config/SecurityConfig.java` (`.requestMatchers("/uploads/**").permitAll()`),
`util/FileStorageService.java` → `store(...)`

**Problem** `FileStorageService` sanitises the filename correctly (`Path.getFileName()` + UUID
prefix — no traversal) but applies **no extension or content-type allowlist**, and the directory is
served as static content with no authentication.

**Impact** An uploaded `.html` or `.svg` is served from the API origin and executes as same-origin
script — stored XSS against any session on that host. Today only the admin Excel endpoint writes
here, which bounds the risk, but any future customer-facing upload turns this Critical. Separately,
uploaded Excel batch files are publicly downloadable to anyone who learns the URL.

**Fix** — allowlist on write:

```java
private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of("jpg", "jpeg", "png", "webp", "gif", "xlsx", "xls", "csv", "pdf");

// inside store(), after computing `original`:
String ext = original.contains(".")
        ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
        : "";
if (!ALLOWED_EXTENSIONS.contains(ext)) {
    throw new IllegalOperationException("File type '" + ext + "' is not allowed");
}
```

And stop the browser sniffing/rendering what is served:

```java
// WebConfig.addResourceHandlers
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/")
        .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
```

plus `X-Content-Type-Options: nosniff` via a `HttpSecurity#headers` entry if not already present.

## M-5 — Login endpoint performs no request validation

**Severity:** Medium
**Location:** `controller/AuthController.java` → `login(...)`; `dto/LoginRequest.java`

**Problem** `@RequestBody LoginRequest` has no `@Valid`, and `LoginRequest` declares no constraints
at all.

**Impact** A null or blank email reaches `authenticationManager.authenticate(...)` and surfaces as a
generic failure rather than a clean 400. Minor, but it is the only unvalidated entry point on the
auth path.

**Fix**

```java
// LoginRequest
@NotBlank(message = "Email is required")
private String email;

@NotBlank(message = "Password is required")
private String password;
```

```java
// AuthController
public LoginResponse login(@Valid @RequestBody LoginRequest request) {
```

## M-6 — Receipt email is sent inside the payment transaction

**Severity:** Medium
**Location:** `service/impl/PaymentServiceImpl.java` → `charge(...)`;
`service/impl/ReceiptEmailService.java` → `sendReceiptEmail(...)`

**Problem** `sendReceiptEmail` renders a PDF and performs a blocking SMTP round trip while the
`@Transactional` payment transaction — and its pooled DB connection, and now the H-2 row lock — is
still open.

**Impact** A slow or unreachable mail server holds a database connection for the SMTP timeout on
every payment. Under load this exhausts the pool. The exception handling is correct (failures are
caught and logged, never rolled back) — the issue is purely the connection being pinned.

**Fix** — send after the transaction commits:

```java
// ReceiptEmailService
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@Async
@TransactionalEventListener
public void on(ReceiptReadyEvent event) { sendReceiptEmail(event.booking(), event.invoice()); }
```

Minimal alternative if you would rather not introduce events: annotate `sendReceiptEmail` with
`@Async` and add `@EnableAsync` to the application class. The method already swallows its own
exceptions, so nothing else changes.

## M-7 — No explicit indexes on frequently-filtered columns

**Severity:** Medium
**Location:** `entity/*.java` — no `@Index` / `indexes = {...}` anywhere in the codebase

**Problem** InnoDB auto-creates indexes for foreign keys, so `passenger.booking_id`,
`invoice.booking_id`, `booking.customer_id` etc. are covered. These are not:

- `tour.title` — scanned by `existsByTitleIgnoreCase` once per row of every Excel import
- `tourcost (tour_id, status, valid_from, valid_to)` — the composite predicate in
  `findFirstByTour_TourIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByCostIdDesc`,
  called on every quote, booking and payment summary
- `booking.order_number` — added in V3 with no index or unique constraint

**Impact** Full scans on the hot pricing path; Excel import degrades to O(rows × tours).

**Fix** — a `V5` migration alongside the existing ones:

```sql
CREATE INDEX idx_tour_title            ON tour (title);
CREATE INDEX idx_tourcost_lookup       ON TOURCOST (tour_id, status, valid_from, valid_to);
CREATE UNIQUE INDEX uk_booking_order_no ON booking (order_number);
```

## M-8 — `spring.jpa.open-in-view=true`

**Severity:** Medium
**Location:** `src/main/resources/application.properties`

**Problem** Documented in the file as a fix for lazy-initialisation 500s. It works, but it keeps the
Hibernate session open for the entire request including view rendering.

**Impact** Holds a connection longer than necessary and hides N+1 problems (it is precisely what
lets H-5 run without erroring). Changing it is a behavioural change across many endpoints, so this
is a **recommendation, not a required fix** — leave it until the serialisation paths use DTOs or
fetch joins.

## M-9 — `CurrentUserProvider.isAdmin()` can throw NPE

**Severity:** Medium
**Location:** `security/CurrentUserProvider.java` → `isAdmin()`

**Problem** `currentEmail()` null-checks the `Authentication`; `isAdmin()` does not:

```java
return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()...
```

`InvoiceServiceImpl.getByBooking` and `getReceiptPdf` call `isAdmin()` first.

**Impact** An unauthenticated call reaching a service that calls `isAdmin()` yields a 500 instead of
a 401. Currently unreachable (all callers sit behind authenticated routes), so this is defensive.

**Fix**

```java
public boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
}
```

---

# LOW

## L-1 — Debug endpoints shipped

**Severity:** Low
**Location:** `controller/TestController.java` (`GET /api/test`), `controller/TourController.java`
(`GET /api/tours/test`)

**Problem** Both return hardcoded strings. `/api/test` falls through to
`anyRequest().authenticated()` and confirms token validity to any caller.

**Impact** Dead code; minor surface-area and fingerprinting.

**Fix** Delete `TestController.java` and the `test()` method in `TourController`.

## L-2 — Receipt PDF fetch bypasses the shared HTTP client

**Severity:** Low
**Location:** `src/services/invoiceService.js` → `fetchReceiptPdfBlob(...)`

**Problem** Bypassing `httpClient` is **correct** here — it only parses JSON. But the raw `fetch`
inherits none of its safety: no `AbortController` timeout, and a 401 does not clear the session.

**Impact** A hung request never resolves and the download spinner never clears; an expired token
shows "Could not load the receipt PDF" instead of redirecting to login.

**Fix**

```js
export async function fetchReceiptPdfBlob(bookingId) {
  const session = loadSession();
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.INVOICES.RECEIPT(bookingId)}`, {
      headers: session?.token ? { Authorization: `Bearer ${session.token}` } : {},
      signal: controller.signal,
    });
    if (response.status === 401) {
      clearSession();
      throw new Error("Session expired. Please log in again.");
    }
    if (!response.ok) throw new Error("Could not load the receipt PDF.");
    return response.blob();
  } finally {
    clearTimeout(timeoutId);
  }
}
```

(add `clearSession` to the existing `../utils/storage` import)

## L-3 — Invoice generation is not idempotent

**Severity:** Low (escalates to High if H-2 is not fixed)
**Location:** `service/impl/InvoiceServiceImpl.java` → `generate(...)`

**Problem** No uniqueness guard on `invoice.booking_id`, but `InvoiceRepository.findByBooking_BookingId`
returns `Optional`, which assumes at most one.

**Impact** Two invoices for one booking make every subsequent read throw `NonUniqueResultException`.
Fixing H-2 removes the only path that produces this, but the constraint is worth adding as a backstop.

**Fix** — in the V5 migration:

```sql
CREATE UNIQUE INDEX uk_invoice_booking ON invoice (booking_id);
```

## L-4 — `ReceiptPdfGenerator` does not close the document on failure

**Severity:** Low
**Location:** `util/ReceiptPdfGenerator.java` → `generate(...)`

**Problem** `doc.close()` is the last statement inside `try`; an exception thrown while adding
content skips it.

**Impact** Negligible in practice — `Document`/`PdfWriter` write to an in-memory
`ByteArrayOutputStream` (which is itself in a try-with-resources) and hold no OS handles. Noted for
completeness only.

**Fix** Optional — move `doc.close()` into a `finally`, guarded by `doc.isOpen()`.

---

# VERIFIED CLEAN — ✅ No issue found

## Security

- **SQL injection** — ✅ No issue found. Every query is a Spring Data derived method or a
  parameterised `@Query`. No string concatenation into JPQL/SQL anywhere in `repository/`.
- **CSRF** — ✅ No issue found. `csrf().disable()` is correct for a stateless JWT API with
  `SessionCreationPolicy.STATELESS` and no cookie-based auth.
- **CORS** — ✅ No issue found. Explicit origin list from `CORS_ALLOWED_ORIGINS`, never `*` with
  `setAllowCredentials(true)`.
- **Password handling** — ✅ No issue found. BCrypt via `PasswordEncoder`; `passwordHash` is
  `@JsonIgnore`d on the entity as defence in depth; never logged; never returned in any DTO.
- **Privilege escalation on registration** — ✅ No issue found. `UserServiceImpl` hardcodes
  `DEFAULT_SELF_REGISTER_ROLE = "CUSTOMER"` and explicitly ignores any role in the request body.
- **IDOR / authorization bypass** — ✅ No issue found. Ownership is resolved from the JWT via
  `CurrentUserProvider`, never from a client-supplied id. `BookingServiceImpl.requireOwnedOrAdmin`,
  `PaymentServiceImpl.requirePayableBooking`, `PassengerServiceImpl.requireOwnedBooking` and
  `InvoiceServiceImpl` all re-check per call, and correctly return **404 rather than 403** so a
  non-owner cannot enumerate valid ids.
- **Cardholder data** — ✅ No issue found. PAN and CVV are used for the gateway call and discarded;
  only `getMaskedSummary()` (brand + last four) is persisted. Luhn, expiry and CVV are validated
  server-side, not trusted from the client.
- **JWT signing** — ✅ No issue found. Rejects short secrets, SHA-256-derives a 256-bit key from a
  plain string, and falls back to a **random per-process** key with a loud warning rather than a
  hardcoded one. Expiry is enforced — `parseClaimsJws` throws `ExpiredJwtException`, caught in the
  filter, which clears the context.
- **Token storage (frontend)** — ✅ No issue found. `sessionStorage`, not `localStorage` — smaller
  XSS blast radius and cleared on tab close. All access is funnelled through `utils/storage.js`.
- **Path traversal on upload** — ✅ No issue found. `Path.of(name).getFileName()` strips directory
  components and a UUID prefix prevents collisions. (Type allowlist is the open item — see M-4.)

## Backend architecture

- **Layered architecture / Controller → Service → Repository** — ✅ No issue found. No controller
  touches a repository directly; no repository logic leaks upward.
- **Exception handling** — ✅ No issue found. `GlobalExceptionHandler` is unusually thorough:
  404/401/403/409/400 are all mapped, `NoResourceFoundException` and `MethodArgumentTypeMismatchException`
  are handled so unmapped routes and bad path params return 4xx instead of 500, and the catch-all
  logs the stack trace but returns a generic message — no internal detail leaks to the client.
- **Seat-inventory concurrency** — ✅ No issue found. `createBooking` uses
  `tourScheduleRepository.findByIdForUpdate` (pessimistic lock) before the availability check, so
  seats cannot be oversold. This is exactly the pattern H-2 asks for on `Booking`.
- **Transaction management** — ✅ No issue found on `BookingServiceImpl` (`createBooking`,
  `finalizePassengers`, `updateStatus`, `cancelBooking`), `PaymentServiceImpl` and
  `ExcelUploadServiceImpl`, all correctly `@Transactional`. The one gap is H-1.
- **Circular dependencies** — ✅ No issue found. `PaymentServiceImpl → InvoiceServiceImpl` and
  `→ ReceiptEmailService` are one-directional; no cycle exists.
- **Package organization** — ✅ No issue found. `controller / service / service.impl / repository /
  entity / dto / enums / security / exception / config / util` is consistent and conventional.
- **Excel import** — ✅ No issue found. Per-row error collection (one bad row doesn't abort the
  batch), duplicate detection against both the DB and earlier rows in the same file, and
  try-with-resources on both the `InputStream` and the POI `Workbook`.
- **Payment gateway abstraction** — ✅ No issue found. `PaymentGateway` keeps provider detail out of
  the service; a declined charge throws before anything is persisted, so no orphan payment or
  invoice is created.

## Business logic

- **Pricing — twin sharing / single occupancy / extra person / child with bed / child without bed** —
  ✅ No issue found. `TourPricingCalculator` maps each occupancy category to the correct `TourCost`
  column with correct null fallbacks, room supplements are additive and only applied to explicit
  selections, and per-passenger prices sum exactly to the booking total.
- **DOB-based passenger validation** — ✅ No issue found. Date of birth is the single classifier on
  both sides; a client-sent `passengerType` is ignored server-side; occupancy contradicting the
  derived band is rejected; the frontend clears an invalid category on DOB change and re-validates
  at save.
- **Booking status lifecycle** — ✅ No issue found. `PENDING → CONFIRMED` on payment,
  `→ CANCELLED` releases seats using the persisted `numberOfPassengers` (not derived from the total,
  which would miscount when add-ons are present). Status changes are admin-gated. (The paid-cancel
  gap is M-2.)
- **Order number generation** — ✅ No issue found. Generated once, only after the gateway confirms
  success. (Uniqueness index recommended in M-7.)
- **Payment status** — ✅ No issue found. Taken from the gateway result, never from the client.
- **Role-based access** — ✅ No issue found. `SecurityConfig` rules and `@PreAuthorize` agree; writes
  are ADMIN-only across every module; the `/api/reviews/me/**` rules correctly precede the generic
  `/api/reviews/**` rule so ordering does not defeat them.

## Frontend

- **API abstraction** — ✅ No issue found. Every path lives in `constants/apiEndpoints.js`; every
  call goes through `services/*.js` → `httpClient`. No hardcoded URLs in components.
- **`httpClient`** — ✅ No issue found. 15s `AbortController` timeout, network vs HTTP error
  distinction, normalised `ApiError { status, message, details }`, 401 clears the session, and it
  correctly declines to set `Content-Type` for `FormData` so the browser supplies the boundary.
- **Protected routes** — ✅ No issue found. `ProtectedRoute` returns `null` while `isInitializing`,
  which correctly avoids the redirect-flash race on refresh, and carries `state.from` so login
  returns the user to where they were headed.
- **Context / reducer** — ✅ No issue found. `AuthContext` and `BookingFlowContext` both memoise
  their value objects and wrap callbacks in `useCallback`; `bookingFlowReducer` is pure.
- **Memory leaks** — ✅ No issue found. `PaymentPage` and `BookingSummaryPage` both use the
  `let cancelled = false` cleanup pattern on their fetch effects; `PaymentPage` also clears its
  staged-feedback `setTimeout` on both success and failure paths.
- **Loading and error states** — ✅ No issue found. Every async page has an explicit
  `loading / succeeded / failed` status with `Loader` and `ErrorState` components.
- **Console logs / secrets** — ✅ No issue found. Zero `console.*` statements in `src/`. No secrets,
  keys or TODO/FIXME markers.
- **Payment page double-pay guard** — ✅ No issue found. Checks both `summary.alreadyPaid` and
  `bookingStatus === "CONFIRMED"` before rendering the form.
- **Full-stack contract** — ✅ No issue found. Every frontend service call was matched against its
  controller: paths, HTTP verbs, request bodies and response shapes align across register, login,
  tour listing, tour details, booking, passengers, quote, payment summary, card payment, invoice and
  receipt. `finalizeBookingPassengers` correctly uses `PATCH`; `payWithCard` correctly expects 201.

---

# Recommended fix order

1. **C-1** — GST charge mismatch *(revenue; one-line change plus a helper)*
2. **H-3** — remove SMTP debug logging *(credential leak; delete four lines)*
3. **H-4** — enforce `isEnabled()` *(one-line change)*
4. **H-1** — `@Transactional` on register *(one annotation)*
5. **H-2** — pessimistic lock on booking before charging
6. **H-5** — batch the admin booking queries
7. Medium items, then Low.

Items 1–4 are four small, low-risk edits and clear the Critical plus three of five Highs.
