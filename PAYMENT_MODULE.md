# Card Payment Module

A dedicated payment step now sits between Booking Summary and Confirmation.

## New flow

```
Tour → Add-ons → Passengers → Summary → [PAYMENT] → Success
                                 │           │
                    creates booking      charges card, confirms booking,
                    (PENDING, seats held) generates invoice + PDF + email
```

The cart path (`/cart/checkout/:bookingId`) now feeds into the **same** payment page instead of its own inline pay button — one checkout screen, not two.

## What was reused vs. built

Most of the backend already existed from earlier sessions. Rather than recreate it:

| Required | Status |
|---|---|
| Payment entity | **Existed** — added `gateway`, `card_summary`, `updated_at` |
| PaymentRepository | **Existed** — unchanged |
| PaymentService / Controller | **Existed** — extended |
| Gateway abstraction | **Existed** (`PaymentGateway`, `GatewayResult`, `SimulatedPaymentGateway`) — extended with card support |
| PDF receipt + email | **Existed** — untouched, now triggered by the card path too |
| Card DTOs, card form, validation, processing UI, success page | **New** |

`transactionId` maps to the existing `transaction_ref` column. I kept the column name so existing payment rows stay valid, and exposed it as `transactionId` in the API response (with `transactionRef` retained for backward compatibility).

## No duplicated business logic

`recordPayment()` and `payWithCard()` both funnel into one private `charge()` method holding the entire confirmation path — ownership check, status transition, order number, invoice, receipt email. Adding cards changed *how the gateway is called*, not what happens after it succeeds.

```java
recordPayment(request)  ─┐
                         ├─→ requirePayableBooking() → charge() → invoice → PDF → email
payWithCard(request)    ─┘
```

## Card data handling

**The PAN and CVV are never persisted, logged, or stored client-side.**

- Only `"VISA ****4242"` (brand + last four) is written to `payment.card_summary`
- `CardDetails.toString()` and `CardPaymentRequest.toString()` are overridden so a stray log line or stack trace can't leak a card number
- The gateway logs the masked summary only
- Frontend keeps card state in component-local `useState`, cleared the moment the request returns — no localStorage, no context, no wishlist-style caching

Storing a full PAN would put your entire database in PCI-DSS scope. With a real gateway the card would never reach your server at all — the browser tokenises against the provider and sends only a token. `CardPaymentRequest` is the seam where that swap happens.

## Validation — client *and* server

Client-side (`utils/card.js`): Luhn, brand-aware length (AMEX 15 / others 16), expiry not in the past, brand-aware CVV length (AMEX 4 / others 3), required fields.

**The server re-runs all of it** (`CardDetails.passesLuhn/isExpired/hasValidCvv`, checked in both `payWithCard()` and the gateway). Client validation is a UX convenience — it can be bypassed with curl, so it is never the only gate.

## Amounts come from the server

The client never computes or sends a total. `GET /api/payments/booking/{id}/summary` recalculates from the persisted booking using the same `TourPricingCalculator` used at booking time, so the figure shown is exactly what is charged. A tampered client can't alter the amount.

## New APIs

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/payments/booking/{bookingId}/summary` | Tour, passengers, rooms, fare lines, add-ons, subtotal, GST, discount, grand total, `alreadyPaid` |
| `POST` | `/api/payments/card` | Card charge → confirms booking, invoice, PDF, email |

Both under the existing `/api/payments/**` rule (ADMIN/CUSTOMER); booking ownership is re-checked per call.

## Database changes

Additive only, via `ddl-auto=update`:

| Table | New columns |
|---|---|
| `payment` | `gateway` VARCHAR(30), `card_summary` VARCHAR(40), `updated_at` DATETIME |

No column renamed or dropped. Existing payment rows remain valid.

> Running total for a future Flyway baseline: 3 new tables (`newsletter_subscriber`, `wishlist_item`) and columns across `passenger` (6 address) and `payment` (3).

## Test cards (simulator only)

| Number | Result |
|---|---|
| `4242 4242 4242 4242` | Success (Visa) |
| `5555 5555 5555 4444` | Success (Mastercard) |
| `3782 822463 10005` | Success (AMEX, 4-digit CVV) |
| `4000 0000 0000 0002` | Declined by issuer |
| `4000 0000 0000 9995` | Insufficient funds |
| Any failing Luhn | Rejected before the gateway |

Any future expiry and a correct-length CVV work.

## Swapping in Razorpay / Stripe

1. Add `RazorpayGateway implements PaymentGateway`
2. Set `PAYMENT_PROVIDER=razorpay`

`@ConditionalOnProperty(matchIfMissing = true)` keeps the simulator as the default. **No change to `PaymentServiceImpl`, the booking flow, invoicing or email.** For a real async gateway, `GatewayResult.pending()` already exists for the create-PENDING-then-settle-via-webhook pattern.

## Files

**New (backend):** `dto/CardPaymentRequest.java`, `dto/PaymentSummaryResponse.java`, `payment/CardDetails.java`
**Modified (backend):** `entity/Payment.java`, `dto/PaymentResponse.java`, `payment/PaymentGateway.java`, `payment/SimulatedPaymentGateway.java`, `service/PaymentService.java`, `service/impl/PaymentServiceImpl.java`, `controller/PaymentController.java`

**New (frontend):** `pages/PaymentPage.jsx`, `pages/PaymentSuccessPage.jsx`, `components/domain/CardForm.jsx`, `components/domain/PaymentProcessingOverlay.jsx`, `utils/card.js`
**Modified (frontend):** `services/paymentService.js`, `pages/BookingSummaryPage.jsx`, `pages/CartCheckoutPage.jsx`, `routes/AppRoutes.jsx`, `constants/routes.js`, `constants/apiEndpoints.js`

## Verification

- **ESLint clean** on all payment files; whole `src` was clean before this change
- Structural checks on all 10 changed/new Java files — balanced, packaged
- Every symbol used in `PaymentServiceImpl` confirmed imported (15 checked)
- **No duplicate route mappings** introduced (re-ran the full 148-endpoint scan)
- Confirmed no log statement touches a raw card number; `toString()` overridden on both card-carrying classes
- Confirmed no `localStorage`/`sessionStorage` in the payment UI
- Existing `recordPayment`, invoice `generate`, `sendReceiptEmail`, `getReceiptPdf` all still referenced and unmodified

**Not verified: a compile or a run** — this environment has no JDK 17/Maven and Maven Central is blocked. Run `mvn clean compile` and `npm run build` before using it.

## Manual test checklist

- [ ] Summary → "Continue to payment" creates a PENDING booking and routes to `/booking/{id}/payment`
- [ ] Payment page shows tour, dates, passengers, rooms, fare lines, add-ons, subtotal, GST, grand total
- [ ] Card number auto-formats in 4-digit groups; AMEX formats 4-6-5
- [ ] Brand badge appears (VISA / MASTERCARD / AMEX / RUPAY)
- [ ] `4242...4241` (bad Luhn) → "Please check the card number"
- [ ] Past expiry → "This card has expired"
- [ ] 2-digit CVV → "CVV must be 3 digits"; AMEX asks for 4
- [ ] Valid card → Processing → Verifying → Payment successful → success page
- [ ] `4000...0002` → declined, booking stays PENDING, **no** invoice created
- [ ] Success page shows transaction ID (copyable), order number, card summary
- [ ] Receipt PDF downloads and includes the address
- [ ] Receipt email sends (if SMTP configured)
- [ ] Reopening the payment URL after paying → "already paid" guard
- [ ] Cart checkout → passengers → same payment page
- [ ] Responsive: summary stacks above the form on mobile
