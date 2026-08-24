# eTour — Final QA & Bug Audit

Full end-to-end pass over backend, frontend and the complete user journey.
Excluded by instruction (verified applied, not re-reported): registration transaction,
`isEnabled()` on the JWT filter, SMTP debug logging.

**One bug fixed in code** (QA-1 — a regression introduced by the earlier party-composition work).
Everything else is reported with a recommended fix and left untouched, because each is either a
business-policy decision or was already documented in the previous audit.

| | New this pass | Still open from prior audit | Total |
|---|---|---|---|
| Critical | 0 | 1 | 1 |
| High | 1 (fixed) | 2 | 3 |
| Medium | 2 | 8 | 10 |
| Low | 3 | 3 | 6 |

---

# FIXED IN THIS PASS

## QA-1 — Stale booking-flow session renders the wrong number of passenger forms

**Severity:** High
**Status:** ✅ Fixed
**Files:** `etour-frontend/src/context/BookingFlowContext.jsx` → `initBookingFlowState()`

### Root cause

The booking flow persists to `sessionStorage` and rehydrates with a spread:

```js
return saved ? { ...initialBookingFlowState, ...saved } : initialBookingFlowState;
```

A session written **before** the tour page started asking for adults and children separately
contains `numberOfPassengers` but no `adultCount` / `childCount` keys. Spread only overrides keys
that exist, so the saved headcount lands on top of the new **defaults** — leaving
`numberOfPassengers: 3` sitting next to `adultCount: 1, childCount: 0`.

### Impact

For any user mid-booking when this build deploys (sessionStorage survives reloads for the tab):

1. `PassengerDetailsPage` renders `adults + children` = **1** form for a **3**-seat booking.
2. Submitting fails `validateDeclaredComposition` (1 + 0 ≠ 3) → 409.
3. It also fails the passenger-count check (1 ≠ 3) → 409.

The user is hard-stuck — the booking cannot be completed and the stale state persists for the
whole tab session. This is a regression from the party-composition change, not a pre-existing defect.

### Fix applied

```js
function initBookingFlowState() {
  const saved = loadBookingFlow();
  if (!saved) return initialBookingFlowState;

  const state = { ...initialBookingFlowState, ...saved };

  // Treat a saved party with no declared split as all adults - the same
  // fallback the passenger page and the server already apply to bookings
  // that predate the split.
  if (saved.numberOfPassengers != null && saved.adultCount == null) {
    state.adultCount = saved.numberOfPassengers;
    state.childCount = 0;
  }
  return state;
}
```

### Safe & backward compatible?

**Yes.** Only fires when `adultCount` is absent, which is exactly the pre-split shape. Sessions
written by the current build carry the key and pass through untouched — including a legitimately
saved `adultCount: 0`, which is preserved so validation can still reject it rather than being
silently "corrected". Verified across 13 rehydration cases including the no-session and
explicit-zero paths.

---

# NEW FINDINGS — reported, not changed

## QA-2 — An unpaid PENDING booking permanently earns the right to review

**Severity:** Medium
**Files:** `Backend/.../service/impl/ReviewServiceImpl.java` → `requireBookingFor(...)`

**Root cause**

```java
List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.COMPLETED)
```

`PENDING` means created but **not paid**. A booking can be created and cancelled seconds later.

**Impact** Review gaming: create a booking → post a 5-star review → cancel. The booking disappears,
the review stays. Nothing pays for it and no travel occurs. Affects the public rating average shown
on every tour card and the tour details page.

**Recommended fix** — drop `PENDING` from the allowed set:

```java
List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED)
```

**Safe & backward compatible?** Safe, but it is a **policy change**: customers with an unpaid
booking lose the ability to review until they pay. Existing reviews are unaffected. Left unchanged
because "who may review" is your call, not a clear-cut defect.

## QA-3 — Admin schedule edit silently desynchronises seat inventory

**Severity:** Medium
**Files:** `Backend/.../service/impl/TourScheduleServiceImpl.java` → `updateSchedule(...)`

**Root cause**

```java
existing.setAvailableSeats(schedule.getAvailableSeats());
```

The submitted value overwrites the live count unconditionally. `availableSeats` is not a capacity
figure — it is a **running balance** that `createBooking` decrements and `cancelBooking` increments.

**Impact** An admin editing a schedule's price or dates re-submits the whole object, including a
stale `availableSeats` read before recent bookings. Seats already sold are handed back to
inventory → **overselling**, with no error at any layer. The pessimistic lock in `createBooking`
protects against concurrent bookings but not against an admin write.

**Recommended fix** — leave the balance alone unless the admin is deliberately adjusting it:

```java
// Only apply an explicit change; null means "leave inventory as it is".
if (schedule.getAvailableSeats() != null
        && !schedule.getAvailableSeats().equals(existing.getAvailableSeats())) {
    existing.setAvailableSeats(schedule.getAvailableSeats());
}
```

A fuller fix stores `totalSeats` separately from `availableSeats`, but that is a schema change.

**Safe & backward compatible?** The snippet above is safe and additive. Left unchanged because
deliberate capacity adjustment is a legitimate admin action and only you can say whether the edit
form is meant to perform it.

## QA-4 — Duplicate reviews possible under concurrency

**Severity:** Low
**Files:** `Backend/.../service/impl/ReviewServiceImpl.java` → `addMyReview(...)`;
`Backend/.../entity/Review.java`

**Root cause** The "already reviewed" guard is a read-then-write with no database constraint
backing it, and `Review` declares no `uniqueConstraints`. Two concurrent submits both pass the check.

**Impact** Two reviews from one customer for one tour. `findByCustomerCustomerIdAndTourTourId`
returns `Optional`, so the duplicate then makes `editMyReview` throw
`NonUniqueResultException` (500) for that customer on that tour, permanently.

**Recommended fix** — add the constraint as a backstop:

```java
@Table(name = "review", uniqueConstraints = @UniqueConstraint(
        name = "uk_review_customer_tour", columnNames = { "customer_id", "tour_id" }))
```

plus a migration. Requires de-duplicating any existing rows first.

**Safe & backward compatible?** Safe once existing data is clean; the `DataIntegrityViolationException`
handler already converts a violation into a 409.

## QA-5 — Customer email can diverge from login email

**Severity:** Low
**Files:** `Backend/.../service/impl/CustomerServiceImpl.java` → `updateCustomer(...)`

**Root cause** `PUT /api/customer/{id}` copies `email` onto the `Customer` row. `User.email` — the
login identity and the JWT subject — is untouched.

**Impact** `customer.email` is where the **receipt is sent** (`ReceiptEmailService` reads
`booking.getCustomer().getEmail()`) and what `CurrentUserProvider.currentCustomer()` matches on via
`findByUser_Email`. Divergence means receipts go to an address the account no longer logs in with.
Not exploitable — the caller owns both records — but a support-ticket generator.

Note the `/me` endpoint the UI actually uses (`updateCurrentCustomer`) correctly updates only
`fullName` and `phone`, so this is reachable only through the older by-id route.

**Recommended fix** — drop the email copy from `updateCustomer`, matching what `/me` already does:

```java
customer.setFullName(customerDetails.getFullName());
customer.setPhone(customerDetails.getPhone());
// email intentionally not updated - it must stay in step with User.email
```

**Safe & backward compatible?** Yes — narrows what the endpoint mutates; no caller in the frontend
sends a changed email.

## QA-6 — `useAdminCrud` has no unmount guard and swallows the load error

**Severity:** Low
**Files:** `etour-frontend/src/hooks/useAdminCrud.js` → `load()`

**Root cause**

```js
} catch (err) {
  setStatus("failed");
}
```

No cancellation flag, and `err` is caught and discarded.

**Impact** Navigating away from an admin page before its list resolves sets state on an unmounted
component (harmless in React 18, but a leak). More usefully: the actual failure reason — expired
session, 403, server error — is never surfaced; every admin page shows the same generic failed
state. Slower diagnosis in production.

**Recommended fix**

```js
const load = useCallback(async () => {
  let cancelled = false;
  setStatus("loading");
  try {
    const data = await fetchAll();
    if (!cancelled) { setItems(data); setStatus("succeeded"); }
  } catch (err) {
    if (!cancelled) { setStatus("failed"); showToast(err.message || `Couldn't load ${noun.toLowerCase()}s.`, "error"); }
  }
  return () => { cancelled = true; };
}, [fetchAll, noun, showToast]);
```

**Safe & backward compatible?** Yes — additive; every admin page keeps its current behaviour plus a
toast.

---

# STILL OPEN FROM THE PREVIOUS AUDIT

Re-confirmed against current code. Full detail, root cause and diffs are in
`PRE_PRODUCTION_AUDIT.md`; summarised here so this report is complete.

| ID | Sev | Issue | File | Verified still open |
|---|---|---|---|---|
| C-1 | **Critical** | GST shown and invoiced but never charged — gateway and `payment.amount` both get the pre-tax subtotal while the UI and invoice show subtotal + GST | `PaymentServiceImpl.charge` | ✅ line 162, 186 |
| H-2 | High | Concurrent payments can double-charge — booking read with plain `findById`, no lock | `PaymentServiceImpl.requirePayableBooking` | ✅ line 128 |
| H-5 | High | Admin booking list is `1 + 2N` queries, no pagination | `BookingServiceImpl.getAllBookings` | ✅ |
| M-1 | Medium | Unbounded `findAll()` on tours, reviews, customers | `TourServiceImpl`, `ReviewServiceImpl`, `CustomerServiceImpl` | ✅ |
| M-2 | Medium | A paid CONFIRMED booking can be cancelled; seats released, no refund record | `BookingServiceImpl.cancelBooking` | ✅ |
| M-3 | Medium | Tour create/update bind the JPA entity with no `@Valid`, client-supplied `tourId` honoured | `TourController` | ✅ |
| M-4 | Medium | `/uploads/**` public with no extension allowlist | `WebConfig`, `FileStorageService` | ✅ |
| M-5 | Medium | Login endpoint has no `@Valid` and `LoginRequest` no constraints | `AuthController`, `LoginRequest` | ✅ |
| M-6 | Medium | Receipt email sent inside the payment transaction, pinning a DB connection over SMTP | `PaymentServiceImpl.charge` | ✅ |
| M-7 | Medium | No indexes on `tour.title`, the `TOURCOST` composite lookup, `booking.order_number` | entities | ✅ |
| M-9 | Medium | `CurrentUserProvider.isAdmin()` NPEs on a null `Authentication` | `CurrentUserProvider` | ✅ |
| L-1 | Low | Debug endpoints `/api/test`, `/api/tours/test` | `TestController`, `TourController` | ✅ |
| L-2 | Low | Receipt PDF fetch has no timeout and no 401 handling | `invoiceService.js` | ✅ |
| L-3 | Low | Invoice generation not idempotent (no unique index on `invoice.booking_id`) | `InvoiceServiceImpl` | ✅ |

M-8 (`open-in-view=true`) remains a **recommendation only**, not a bug.

---

# VERIFIED CLEAN — ✅ No issue found

## Calculations and business rules

- **Tour cost / occupancy pricing** — ✅ Twin → `basePrice`, Single → `singlePersonCost`,
  Extra person → `extraPersonCost`, Child with/without bed → the matching column, with correct
  null fallbacks. Room supplements are additive and applied only to explicit selections.
  Per-passenger prices sum exactly to the booking total. Re-verified across 15 legacy payload
  shapes with byte-identical totals.
- **Adult / child / infant banding** — ✅ Date of birth is the sole classifier on both sides;
  a client-sent `passengerType` is ignored server-side. Boundaries exact: 12 on the departure
  date is an adult, one day short is a child; 2 is a child, one day short is an infant.
- **Party composition** — ✅ Adults + children must equal the headcount, at least one adult is
  required, each passenger must match the slot they were sold in, and every booking must price an
  adult by DOB. Enforced inline on the form and again in `createBooking`, `quote` and
  `finalizePassengers` — so the cart path is covered too. The ₹0 child-only booking is closed.
- **Discounts** — ✅ Consistently `BigDecimal.ZERO` in `PaymentServiceImpl` and
  `InvoiceServiceImpl`; no discount feature exists to be wrong.
- **Invoice totals** — ✅ `subTotal + tax` computed from the same `app.invoice.gst-rate` property
  both services read. Internally consistent. (What is *charged* against it is C-1.)
- **Order number** — ✅ Generated once, only after the gateway confirms success.

## Security

- **SQL injection** — ✅ Every query is a Spring Data derived method or a parameterised `@Query`,
  including the new contact search. No concatenation anywhere in `repository/`.
- **IDOR / authorization** — ✅ Ownership resolved from the JWT via `CurrentUserProvider`, never
  from a client id. Booking, payment, passenger, invoice, review, wishlist and customer paths all
  re-check per call and return **404 not 403**, so ids cannot be enumerated.
- **Review ownership** — ✅ `addMyReview` / `editMyReview` resolve the customer from the principal;
  `deleteMyReview` 404s for a non-owner. The old by-customer-id write methods are gone.
- **Wishlist** — ✅ Every operation scoped by `customer_id`; `add` is idempotent; `remove` deletes
  by the composite key so no id-guessing is possible.
- **Contact module** — ✅ `POST /api/contact` is `permitAll` and matched *before* the
  `/api/contact/**` admin rule, so submitting is public while list/read/status/delete are
  ADMIN-only. `status` is absent from the request DTO and set server-side, so an enquiry cannot
  arrive pre-marked resolved.
- **CSRF / CORS / password handling / card data** — ✅ Unchanged and correct (see prior audit).

## Data integrity and transactions

- **Seat inventory on booking** — ✅ `createBooking` takes a pessimistic lock via
  `findByIdForUpdate` before the availability check; seats cannot be oversold by concurrent
  bookings. (Admin edits are QA-3.)
- **Cancellation seat release** — ✅ Uses the persisted `numberOfPassengers`, not a figure derived
  from the total, so add-ons don't miscount the release.
- **Transaction boundaries** — ✅ `createBooking`, `finalizePassengers`, `updateStatus`,
  `cancelBooking`, both payment methods, `submit`/`updateStatus`/`delete` on contact, cart
  `addToCart`/`checkout`/`remove`, and the Excel import are all correctly `@Transactional`.
- **Excel import** — ✅ Per-row error collection, duplicate detection against both the database and
  earlier rows in the same file, try-with-resources on the stream and the POI workbook.
- **Cart → booking carry-over** — ✅ Composition flows through checkout; older cart rows with no
  split fall back cleanly and are still held to the at-least-one-adult rule at
  `finalizePassengers`.

## Frontend

- **State management** — ✅ Contexts memoise their values and wrap callbacks in `useCallback`;
  reducers are pure. Booking-flow persistence is correct after QA-1.
- **Memory leaks** — ✅ `PaymentPage`, `BookingSummaryPage`, `TourDetailsPage`, `PassengerDetailsPage`
  and `ContactPage` all use the `let cancelled = false` cleanup pattern on fetch effects;
  `PaymentPage` clears its staged-feedback timeout on both paths. (`useAdminCrud` is QA-6.)
- **Error and loading states** — ✅ Every async page has explicit `loading / succeeded / failed`
  handling with `Loader` and `ErrorState`.
- **Routing** — ✅ `ProtectedRoute` returns `null` while auth rehydrates, avoiding the
  redirect-flash race, and carries `state.from`. The two new public routes sit inside the existing
  shell and touch nothing else.
- **API consistency** — ✅ Every frontend service call matched against its controller: paths, verbs,
  request bodies, response shapes and status codes align across register, login, tour listing,
  details, booking, passengers, quote, cart, payment summary, card payment, invoice, receipt and
  contact.
- **Console logs / secrets** — ✅ Zero `console.*` in `src/`. No secrets or TODO markers.

---

# Recommended order

1. **C-1** — GST charge mismatch *(revenue; ~6 lines)*
2. **H-2** — pessimistic lock before charging
3. **H-5** — batch the admin booking queries
4. **QA-3** — schedule seat overwrite *(decide the policy first)*
5. **QA-2** — review gating *(decide the policy first)*
6. Remaining Mediums, then Lows.

QA-1 is already fixed and needs no action.
