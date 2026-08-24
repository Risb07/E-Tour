# Backend Testing (JUnit 5)

Automated test suite for the eTour Spring Boot backend: what exists, how to
run it, what it covers, and why each piece was added.

- **Framework:** JUnit 5 (Jupiter) + Mockito + AssertJ, via
  `spring-boot-starter-*-test` (Spring Boot 4.1.0)
- **Database under test:** H2 in-memory — the suite never connects to MySQL
- **Current state:** 36 tests, 0 failures, 0 errors, `BUILD SUCCESS`

---

## 1. Why this was added

### 1.1 `mvn test` did not work at all

The project shipped with exactly one test:

```java
@SpringBootTest
class BackendApplicationTests {
    @Test
    void contextLoads() { }
}
```

`@SpringBootTest` boots the full application context, which reads
`src/main/resources/application.properties` and therefore tried to open a real
MySQL connection. On any machine without `DB_PASSWORD` exported and MySQL
running, the test failed during context initialisation:

```
HHH000247: ErrorCode: 1045, SQLState: 28000
Access denied for user 'root'@'localhost' (using password: NO)
...
Failed to initialize JPA EntityManagerFactory: Unable to determine Dialect
without JDBC metadata
```

Consequences:

- `mvn test` and `mvn package` failed outright.
- The documented workaround was `-DskipTests`, so **the test phase was
  effectively switched off** and the build had no safety net.
- CI could never be added without either provisioning a MySQL instance or
  permanently skipping tests.

### 1.2 Business logic had no regression cover

Two areas carried real risk with no test behind them:

- **Passenger age banding.** `PassengerType.fromAge()` decides whether each
  traveller is an INFANT, CHILD or ADULT, which directly sets what they are
  charged. A one-character change to a boundary constant would silently
  mis-price every future booking, and nothing would fail.
- **Tour detail tabs.** The "Good to know" and "Stay & Meals" services had
  recently gained upsert/delete behaviour plus per-tour ownership checks. Those
  ownership checks are what stop `DELETE /api/tours/2/content/{id}` from
  removing tour 1's row — security-relevant logic verified only by hand.

### 1.3 A real bug had just been found by hand

Manual API sweeping found that a wrong HTTP verb (`GET` on a POST-only
endpoint) returned **500 "An unexpected error occurred"** instead of **405**,
because `GlobalExceptionHandler` had no handler for
`HttpRequestMethodNotSupportedException` and fell through to the catch-all.
Finding that by hand is exactly the kind of thing a test should have caught,
so the fix shipped together with tests that pin the status mapping.

---

## 2. What was implemented

### 2.1 Test-only H2 database

**`pom.xml`** — one dependency, `test` scope:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**`src/test/resources/application-test.properties`** — activated by
`@ActiveProfiles("test")`. It overrides only the datasource and the JWT secret
and inherits everything else from the main properties file:

```properties
spring.datasource.url=jdbc:h2:mem:etour_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

Two deliberate choices:

- `MODE=MySQL` keeps entity mappings behaving as they do in production.
- `create-drop`, never `update` — a test run must not be able to alter a real
  schema even if the URL were somehow pointed at one.

**Isolation guarantee.** H2 is `test` scope and the properties file lives in
`src/test/resources`, so neither is on the runtime classpath. `mvn
spring-boot:run`, the packaged jar and the Docker image all still use MySQL
exactly as before. No production code path changed.

### 2.2 Test classes

```
src/test/java/com/etour/
├── BackendApplicationTests.java                    context smoke test
├── enums/PassengerTypeTest.java                    pricing age bands
├── exception/GlobalExceptionHandlerTest.java       HTTP status mapping
└── service/impl/TourDetailServiceImplTest.java     tour detail tab services
```

---

## 3. How to run

```bash
cd Backend && mvn test
```

Single class, or a single method:

```bash
cd Backend && mvn test -Dtest=TourDetailServiceImplTest
```

No database, no environment variables, no running server required. Machine-
readable results land in `Backend/target/surefire-reports/`.

---

## 4. Results

Full run, all green:

| Test class | Tests | Failures | Errors | Time |
|---|---:|---:|---:|---|
| `BackendApplicationTests` | 1 | 0 | 0 | 13.78 s |
| `PassengerTypeTest` | 12 | 0 | 0 | 0.25 s |
| `GlobalExceptionHandlerTest` | 6 | 0 | 0 | 0.04 s |
| `TourDetailServiceImplTest` | 17 | 0 | 0 | 1.46 s |
| **Total** | **36** | **0** | **0** | — |

```
[INFO] Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Runtime is dominated by the single `@SpringBootTest` context load. The other
35 tests together finish in under two seconds because none of them start
Spring or touch a database.

---

## 5. What each class covers, and why

### 5.1 `BackendApplicationTests` — 1 test

| Test | Asserts |
|---|---|
| `contextLoads` | The full Spring context wires up |

Worth more than it appears: a missing bean, a bad `@Value`, a broken component
scan, a duplicate mapping or an entity that does not map will all fail here.
It is also the test that proves the H2 profile works — it is the same test
that previously failed with `Access denied for user 'root'`.

### 5.2 `PassengerTypeTest` — 12 tests

Pure logic; no Spring, no mocks. Pins the boundaries that decide what every
passenger pays, evaluated against a fixed departure date of `2026-06-15`.

| Group | Tests | Asserts |
|---|---:|---|
| `bandsByAgeOnDepartureDate` (parameterised) | 8 | Exact INFANT / CHILD / ADULT boundaries |
| `incomplete data` | 2 | Null DOB and null departure both fall back to ADULT |
| top level | 2 | Birthday later in the departure year; human labels |

The parameterised rows check each boundary to the day — age 1 y 364 d is an
INFANT, exactly 2 is a CHILD, one day short of 12 is a CHILD, exactly 12 is an
ADULT.

The null-fallback tests matter for a non-obvious reason: the fallback is the
**most expensive** band. Incomplete passenger data therefore over-charges
rather than under-charges, which is the safe direction to fail. A refactor
that "helpfully" defaulted to CHILD would be a revenue leak, and this test
blocks it.

### 5.3 `GlobalExceptionHandlerTest` — 6 tests

The frontend's `httpClient` branches on status codes (401 clears the session,
409 shows the message verbatim in a toast), so the exception-to-status mapping
is part of the API contract.

| Test | Asserts |
|---|---|
| `methodNotSupportedIs405` | Wrong verb → 405, not the old 500 |
| `methodNotSupportedMessageIsActionable` | Message names verb, path and allowed methods |
| `methodNotSupportedWithoutSupportedList` | Null supported-methods list does not NPE back into a 500 |
| `resourceNotFoundIs404` | `ResourceNotFoundException` → 404, message preserved |
| `illegalOperationIs409` | Business-rule violation → 409, **message preserved verbatim** |
| `unexpectedIs500AndOpaque` | Catch-all → 500 and does **not** leak the exception text |

The last one is a security assertion, not a formatting one: it feeds the
handler an exception whose message contains a fake JDBC URL with a password
and asserts that string never reaches the response body.

### 5.4 `TourDetailServiceImplTest` — 17 tests

Plain Mockito against mocked repositories — no Spring context, no database, so
these fail for exactly one reason: the service logic changed.

| Group | Tests | Focus |
|---|---:|---|
| `upsertContent` | 4 | Update-vs-insert, language default, unknown tour |
| `deleteContent` | 3 | Soft delete, cross-tour guard, missing row |
| `addStayMeal` | 4 | Null meal flags, set meal flags, unknown tour, unknown location |
| `deleteStayMeal` | 3 | Hard delete, cross-tour guard, missing row |
| `getStayMeals` | 2 | DTO mapping, empty list |
| `deleteMedia` | 1 | Cross-tour guard |

Two groups deserve explanation.

**The upsert regression tests.** `upsertContent` is named "upsert" but used to
build a `new TourContent()` unconditionally, so saving the Weather tab twice
left two active rows and the tour page rendered the section twice with no way
to remove either. `updatesExistingRow` captures the saved entity and asserts
it carries the pre-existing row id, which is only true if the method looked
the row up first.

**The cross-tour guards.** `rejectsCrossTenantDelete` appears three times — for
content, stay-meals and media. Each asserts that a delete addressed to the
wrong tour raises `ResourceNotFoundException` and that **no** save or delete
reaches the repository. Without these, a guessable row id on one tour's URL
would delete another tour's data.

---

## 6. The regression tests were verified, not just written

A test that passes proves nothing on its own — it has to fail when the bug is
present. The upsert fix was mutation-checked by temporarily restoring the
original defect:

```java
// TourDetailServiceImpl.upsertContent - bug deliberately reintroduced
TourContent entity = new TourContent();
```

Result:

```
[ERROR] Tests run: 4, Failures: 1, Errors: 2 -- in upsertContent
[ERROR]   TourDetailServiceImplTest.updatesExistingRow
        at TourDetailServiceImplTest$UpsertContent.updatesExistingRow(...:130)
```

The intended test failed at the intended assertion, and two sibling tests
errored as collateral. Reverting the mutation returned the suite to
`Tests run: 36, Failures: 0, Errors: 0`.

---

## 7. What is *not* covered

Stated plainly so the coverage is not over-read:

- **No controller-layer tests.** There is no `@WebMvcTest`, so the JWT filter
  chain, the `@PreAuthorize` rules in `SecurityConfig`, and request/response
  JSON binding are still only verified by hand.
- **No repository tests.** There is no `@DataJpaTest`, so custom derived
  queries (`findFirstByTour_TourIdAndContentTypeAndLanguageCode`,
  `findByActiveTrueOrderBySortOrderAsc`, …) are not executed against a real
  schema. A typo in a derived-query method name fails at context startup, so
  `contextLoads` catches that much and no more.
- **Booking, payment, invoice and Excel-upload services are untested.**
  `BookingServiceImpl` in particular holds seat-availability and pricing
  orchestration and is the highest-value target for the next round.
- **No frontend tests.** The React app has ESLint but no test runner.

### Suggested next steps, highest value first

1. `@DataJpaTest` for the repositories, against the same H2 profile.
2. `@WebMvcTest` + `spring-security-test` to assert that admin-only endpoints
   reject a customer token — currently proven only by manual API calls.
3. Unit tests for `TourPricingCalculator` and `BookingServiceImpl` seat
   validation.
4. Wire `mvn test` into CI, now that it passes without a database.

---

## 8. Constraint honoured

No functional production code was modified to make these tests pass. The only
`src/main` change in this work was the additive
`HttpRequestMethodNotSupportedException` handler in `GlobalExceptionHandler`,
which fixes the 500→405 defect described in §1.3. Everything else is confined
to `src/test/**` and one `test`-scoped `pom.xml` dependency.
