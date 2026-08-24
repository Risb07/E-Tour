# eTour Notification Service

A standalone microservice that owns outbound notifications: templates, a queue, delivery attempts
and a dead-letter list. It runs alongside **both** eTour backends and neither of them was changed to
add it.

```
Browser ── nginx ─┬─ /api/  ──► backend:8080      (Java OR .NET, whichever profile is up)
                  └─ /svc/  ──► notification-service:8085
                                        │
                                        ├──► db:3306/etour_notify   (its own schema + user)
                                        └──► backend:8080  (admin role check only)
```

## Why it is a separate service, honestly

The queue, the retry policy and the delivery log have nothing to do with tours or bookings, they
change on a different schedule from the rest of the app, and a stuck mail server should not be able
to slow down a booking request. Splitting it out also means the schema for `notification` lives
somewhere that neither backend's Hibernate/EF model has an opinion about.

It closes Part 11 in [`../REMAINING_WORK.md`](../REMAINING_WORK.md), which was marked *"Partial —
receipt email works; no templates/queue/retry"*.

## The retry policy

**Two attempts total: the initial send, and exactly one retry.** Then the message is dead-lettered
and stops moving until a human re-queues it from the admin screen.

| Attempt | Outcome | What happens |
|---|---|---|
| 1 | success | `SENT` |
| 1 | failure | stays `PENDING`, `nextAttemptAt` set 30s out |
| 2 | success | `SENT`, the earlier error is cleared |
| 2 | failure | `FAILED` — dead letter, `nextAttemptAt` cleared so the worker never sees it again |

This is deliberately not the usual exponential-backoff ladder. Retrying a message whose recipient
address is simply wrong just delays the moment a human finds out, and for booking mail a duplicate
delivery is worse than a late one. The manual re-queue is what makes a short budget workable: the
service stops trying, an admin fixes the cause, and re-queueing resets the budget.

Configurable via `NOTIFY_MAX_ATTEMPTS` (set it to `1` to disable retrying entirely). The limit is
captured **on each row at enqueue time**, so raising or lowering it later cannot retroactively
revive messages already dead-lettered under the old policy.

## Who calls it

**Both eTour backends, for the booking receipt.** Neither sends mail itself any more — after a
successful payment each one enqueues a `BOOKING_RECEIPT` here, with the invoice PDF attached. The
customer-visible result is unchanged: same subject, same wording, same attachment, because the
template is a verbatim copy of the text they used to build inline.

### Exactly one e-mail per booking

Three independent things have to hold, and it is worth knowing which layer owns each — a change to
any one of them silently reintroduces duplicate mail:

| # | Guarantee | Owned by |
|---|---|---|
| 1 | One payment per booking | Both backends lock the booking row and refuse a second payment once it is `CONFIRMED` |
| 2 | Never enqueued before the payment commits | Java defers to `afterCommit`; .NET's call site already sits after the writes |
| 3 | Never queued twice if 2 runs twice | **This service**, via `idempotencyKey = receipt-<invoiceNumber>` |

Guarantee 3 is the backstop, and it is the one this service is responsible for. A repeat enqueue
with the same key returns the original row — enforced by a unique index, not just a lookup, so two
concurrent calls still produce exactly one message.

Note what is deliberately **absent**: neither backend falls back to direct SMTP when this service is
unreachable. A fallback would send a second copy in exactly the ambiguous case where the enqueue
actually succeeded but the response was lost. Instead the failure is logged, the receipt stays
downloadable from `GET /api/invoices/booking/{id}/receipt`, and anything that did not go out is
visible on the admin Notifications screen.

### How the backends authenticate

They mint a token for themselves with the shared `JWT_SECRET`, using the same token service that
issues customer tokens — no extra credential to configure. The subject is `system@etour.internal`,
which can never match a real account, and that gives a useful least-privilege result: the admin
endpoints re-check the ADMIN role against a backend's user table, and a subject with no user row
fails it. A backend can therefore **enqueue and nothing else** — it cannot read the delivery log.

## Endpoints

All under `/svc/`, never `/api/` — nginx routes all of `/api/` to whichever backend is active, so a
separate prefix means this service can never shadow a backend route, now or after either backend
adds endpoints.

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/svc/health` | none | Liveness (compose's healthcheck has no token) |
| `POST` | `/svc/notifications` | any valid eTour token | Queue a message (optionally with attachments) → `202 Accepted` |
| `GET` | `/svc/notifications` | ADMIN | Delivery log, paginated, `?status=PENDING\|SENT\|FAILED` |
| `GET` | `/svc/notifications/{id}` | ADMIN | One message with its error history |
| `GET` | `/svc/notifications/stats` | ADMIN | Counts per status |
| `GET` | `/svc/notifications/templates` | ADMIN | Available templates |
| `POST` | `/svc/notifications/{id}/retry` | ADMIN | Re-queue a dead letter |

`POST` returns **202, not 201**: the row exists, but nothing has been delivered yet and a caller
must not read the response as "sent".

### Enqueueing

```bash
curl -X POST http://localhost/svc/notifications \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
        "recipient": "traveller@example.com",
        "template": "BOOKING_CONFIRMED",
        "variables": {"customerName":"Grace","bookingRef":"ETR-1001","tourTitle":"The Rajasthan Edit"},
        "idempotencyKey": "booking-1001-confirmed"
      }'
```

`idempotencyKey` is optional but strongly recommended for anything triggered by a user action.
Re-sending the same key returns the original row instead of queueing a second copy, so a client
retrying a failed HTTP call cannot double-send the email. It is enforced by a unique index, not just
a lookup, so two concurrent requests still produce exactly one message.

### Attachments

Add an `attachments` array — base64, because this is a JSON API and the caller is a backend posting
a PDF it just generated, not a browser doing a multipart upload:

```jsonc
"attachments": [
  { "filename": "receipt-INV-1001.pdf", "contentType": "application/pdf", "contentBase64": "JVBERi0..." }
]
```

Capped at 10MB total per message (`notify.max-attachment-bytes`) and rejected at **enqueue** rather
than at delivery: queueing something that can only ever fail would burn both attempts and dead-letter
for a reason the caller could have been told synchronously.

Bytes live in their own `notification_attachment` table and are loaded lazily. The delivery log is
listed and paginated constantly by the admin screen, and it must never drag receipt PDFs through the
database for rows nobody is looking at — so only the single-message endpoint reports attachment
metadata, and only the transport ever reads the bytes.

## Authentication, and the one honest compromise

The service verifies the eTour JWT with the **same `JWT_SECRET` both backends sign with**, deriving
the key exactly as they do (base64 if it decodes to ≥32 bytes, otherwise SHA-256 of the raw string).
A token minted by either backend therefore works here with no coordination.

That establishes *who* the caller is. It cannot establish whether they are an admin: **an eTour JWT
carries only `sub` and `iat`/`exp`** — both backends re-read the role from the database on every
request. This service has no access to that database, so for admin endpoints it asks whichever
backend is live, using the caller's own token, against a route that is ADMIN-only on both
implementations (`GET /api/admin/dashboard`). 200 means admin; 401/403 means not.

The trade-off, stated plainly: **admin endpoints on this service need a backend to be reachable.**
Delivery does not — the queue keeps draining regardless. Only positive results are cached (60s), so
revoking someone's admin role takes effect within the TTL rather than being remembered indefinitely.

The alternative — adding a `role` claim to the JWT — would have been simpler here but required
modifying both backends, which this work was explicitly not allowed to do.

## Transports

Auto-detected at startup, matching how both backends already degrade:

- **`smtp`** when `MAIL_USERNAME` is set — real delivery, using the same mail credentials.
- **`simulated`** otherwise — logs instead of sending, so the service is useful out of the box with
  no SMTP server.

Force one with `NOTIFY_TRANSPORT=smtp|simulated`.

In simulated mode, **any recipient starting with `fail`** (e.g. `fail@example.com`) always throws.
That is the only way to exercise the retry and dead-letter paths on a machine with no mail server,
and it is scoped to the simulated transport so it can never affect real delivery.

## Its own database

`etour_notify` is a **separate database with its own MySQL user**, on the same `db` container the
eTour backends use. The notify user is granted rights on `etour_notify` only, so it cannot read or
write a single eTour table — the isolation that matters is preserved, and `ddl-auto: update` stays
safe because no other application has an opinion about this schema.

What it gives up, stated plainly: the two databases share a server, a volume and a backup. Restoring
one restores both, `docker compose down -v` discards both, and a query storm in one is felt by the
other. That is the deliberate trade for not running a second MySQL — a full second server for a
message queue is a lot of memory on a laptop or a small EC2 instance.

The database and the `notify` user are created by
[`docker/mysql/20-grants.sh`](../docker/mysql/20-grants.sh), which runs inside the `db` container's
own initialisation. No bootstrap container is involved.

**The one catch, and it matters when you deploy:** that init directory runs **once**, on first start
with an empty volume. On a machine whose database already exists — the EC2 host, or any laptop that
ran this stack before the notification service was added — the script will not run again and the
notify user will not exist, so the service fails on startup with access denied. Fix it once, by hand:

```bash
docker exec etour-db mysql -uroot -prootpassword -e "CREATE DATABASE IF NOT EXISTS etour_notify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; CREATE USER IF NOT EXISTS 'notify'@'%' IDENTIFIED BY 'notifypassword'; GRANT ALL PRIVILEGES ON etour_notify.* TO 'notify'@'%'; FLUSH PRIVILEGES;"
```

Same command applies if you change `NOTIFY_DB_PASSWORD` later: edit `.env`, then re-run it with the
new password so the MySQL user matches.

## Running

Comes up automatically with the rest of the stack, under **either** backend profile:

```bash
docker compose --profile dotnet up --build     # or --profile java
```

It has **no compose profile**, and that is load-bearing: nginx resolves every upstream when it loads
its config and exits if one is missing, so a service that existed under only one profile would
crash-loop nginx under the other.

Standalone, for development:

```bash
cd notification-service
mvn spring-boot:run
```

## Tests

```bash
mvn test
```

23 tests, running against in-memory H2 — nothing needs to be running. They cover the retry policy
end to end (succeeds first time; one retry scheduled; dead-lettered after that retry and **not**
retried again; a retry that succeeds; the attempt budget never exceeded), template rendering
including the `$`/backslash case that would otherwise throw at runtime, idempotency, and the rules
around manual re-queueing.

The image build runs them, so `docker compose build` fails if the retry policy regresses.

### What the tests cannot catch

They run on H2, and H2 is more permissive than MySQL in two ways that matter here:

- **`FOR UPDATE SKIP LOCKED`** — H2 cannot execute it, so the worker's claim query is only exercised
  against real MySQL. It is what stops two instances of this service delivering the same message
  twice.
- **Column widths.** This bit for real during development: Hibernate 6 maps a `@Lob String` onto
  MySQL's **`tinytext` (255 bytes)** unless a length is given, so every template body was silently
  truncated and the service crash-looped on startup with *"Data too long for column 'body'"*. H2
  does not enforce that width, so all 23 tests passed while the container would not start. The
  columns now declare `length = 65535` (→ `text`) and carry a comment saying why.

  Worth knowing if you add a column: `ddl-auto: update` **adds** columns and tables but never
  changes an existing column's type, so fixing a mapping like that also means dropping the table.

## Trying the whole pipeline

From **Admin → Notifications**:

1. **Send test** → any address → **Queue**. It appears as `PENDING`, then `SENT` within ~5s.
2. Send another to `fail@example.com`. Watch it go `PENDING` (attempt 1/2) → ~30s later `FAILED`
   (2/2) with the cause in the *Last error* column.
3. Hit **Retry** on that row — it returns to `PENDING` with a fresh budget.

Step 2 is the interesting one: it is the retry policy and the dead letter, visible on screen.
