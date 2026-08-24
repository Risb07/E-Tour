# eTour Frontend

React (Vite) + Tailwind CSS + React Router. No Redux, no Axios, no UI libraries — per spec.

## Setup
```
npm install
cp .env.example .env      # adjust VITE_API_BASE_URL if your backend isn't on :8080
npm run dev
```
Requires the backend running (see Backend/README.md) with at least the `ADMIN` and `CUSTOMER`
roles seeded, since registration/login depend on them.

## Module 1: Authentication — done

**Built:** register, login, logout, JWT stored in sessionStorage, role-aware redirect after
login, `ProtectedRoute` (auth + optional role gating), toast notifications, full folder
structure for the rest of the app to build into.

**Backend dependency added:** `/api/auth/login` now returns `{userId, firstName, lastName,
email, role}` alongside the token (previously just `{token, message}`) — required for
role-based navigation. See the updated `Backend/src/main/java/com/etour/dto/LoginResponse.java`
and `AuthServiceImpl.java` if you want to diff against your existing backend zip.

**Manually verify before moving on:**
1. Register a customer → redirected to login with a success toast.
2. Log in → redirected to `/` (or wherever `ProtectedRoute` sent you from).
3. Refresh the page while logged in → still logged in (sessionStorage rehydration).
4. Log out → session cleared, back at `/`.
5. As a non-admin, visit `/admin` directly → redirected to `/unauthorized`.

**Not yet built (intentionally — later modules):** Home Page (categories grid), navbar/layout
shell for authenticated pages, Customer Dashboard, Profile, Admin Dashboard real content. The
placeholder pages exist only to prove routing/guards work.

## Folder structure
```
src/
  assets/            static images/icons (empty until a module needs one)
  components/
    common/           Button, Input, Loader - reused everywhere
    forms/            (empty - form-specific composite fields land here later)
    ui/               (empty - larger stateless UI blocks, e.g. Card/Modal/Table, land here)
    layout/           AuthLayout (Navbar/Footer land here once Home Page module builds them)
  pages/               one file per route
  context/             AuthContext, ToastContext
  reducers/             authReducer (one per Context that needs one)
  hooks/               useAuth, useToast
  services/            httpClient (fetch wrapper), authService
  routes/               AppRoutes, ProtectedRoute
  constants/           apiEndpoints, roles, routes - no magic strings elsewhere
  utils/               storage, validators
  styles/              index.css (Tailwind entry)
  config/              env.js
  types/               JSDoc typedefs (no TypeScript, per spec)
```

## Design system checkpoint

Before building Home Page (the module with the most visual surface area — hero, search,
category grid, featured tours, etc.), the shared component library was upgraded to match the
premium-travel-platform spec:

- **Tokens** (`tailwind.config.js`): "dusk horizon" palette (`ink` = deep indigo, `amber` =
  warm accent) kept from Module 1, extended with a shadow scale (`soft`/`card`/`lifted`), a
  card radius token, and animation keyframes (`fade-in`, `slide-up`, `shimmer`).
- **Icons**: `lucide-react` added — the only new dependency.
- **Component library** (`components/common`, `components/ui`):
  - `Button` — primary/secondary/outline/danger/ghost, icon support, loading state
  - `Input` — icon slot, built-in password visibility toggle
  - `Card` — the shell every future card (category/tour/dashboard stat) will use
  - `Badge` — status/discount/availability pills
  - `Skeleton` / `SkeletonCard` — shimmer loading placeholders (respects `prefers-reduced-motion`)
  - `EmptyState` — icon + message + up to two actions, for "no results" everywhere
  - `ErrorState` — 404/403/500/network/generic presets, one component

The Auth pages (Module 1) were retrofitted to use the upgraded `Input`/`Button` (icons, pill
buttons) and the new `ErrorState` component, so the whole app already looks like one product
rather than two design eras stitched together.


## Full application — build log

Everything below was built in one continuous pass across Home/Categories, Tour Listing/Details,
Search, Booking (multi-step), Customer Dashboard, Profile, Reviews, and Admin (Dashboard,
Bookings, Excel Upload). Being upfront about depth, same as the backend README:

### Fully built and wired to real endpoints
- **Home Page**: hero + search, live category grid (backend-driven subcategory branching via
  `parentCategory`), "Newly added tours" (see note below on why it's not "Featured/Trending")
- **Category → SubCategory → Tour Listing** branching, exactly per the BRD's two-case flow
- **Search**: full filter panel (category, tour code, price range, duration), debounced text
  search, pagination, URL-synced (shareable/bookmarkable results)
- **Tour Details**: tabs for Overview/Itinerary/Journey/Stay & Meals/Gallery/Add-ons/Info
  (Passport-Visa/Weather/Do's & Don'ts/Terms)/Reviews, sticky booking card, mobile floating CTA
- **Booking flow**: Add-ons → Passenger Details → Summary (with live payment-method picker) →
  Confirmation, backed by `BookingFlowContext` so state survives the route transitions
- **Customer Dashboard**: upcoming/history bookings with cancel, invoices table, stats
- **Profile**: self-service view/edit (email locked - see backend note)
- **Reviews**: read + submit, tied to the new ownership-safe `/api/reviews/me/tour/{id}`
- **Admin**: Dashboard (live stats), Bookings table (status update), Excel Upload (drag-drop,
  per-row error reporting)

### Backend fixes made while building this (in the extracted project, re-zip to apply)
- **`CategoryController` GET endpoints were accidentally locked behind `@PreAuthorize`**,
  overriding the `permitAll` URL rule - this was silently blocking the entire homepage for
  anonymous visitors. Fixed.
- **Added `GET/PUT /api/customer/me`** - there was previously no way for the frontend to fetch
  "my own" customer profile without an admin-only path-param endpoint. Needed for Profile page
  and used internally for review/booking ownership checks.
- **Review submission had the same IDOR class of bug as the original Booking one** - the
  `/customer/{id}/tour/{id}` write endpoints trusted a client-supplied customer ID. Added
  `/api/reviews/me/tour/{id}` (JWT-derived customer) and locked the legacy paths to admin-only.
- **`Category.tours` had `@JsonIgnore`**, meaning the tour count shown on category cards would
  always have silently rendered as 0. Changed to `@JsonIgnoreProperties` (hiding just the heavy
  nested fields) so real counts serialize, with the corresponding back-reference on
  `Tour.categories` updated to avoid infinite recursion.

### Known simplifications / backend dependencies (be aware before demoing)
- **"Featured/Trending Tours" on the homepage** are actually "newest tours by ID" - the backend
  has no `is_featured`/`is_trending` flag on `Tour`. Labeled honestly rather than faked.
- **No homepage-level testimonials/stats section** - there's no aggregate review or stats
  endpoint that's safe to call anonymously (the real stats endpoint is admin-only). Omitted
  rather than hardcoded.
- **Newsletter signup** has UI but no backend endpoint - shows an honest toast saying so.
- **Cart is not exposed in the UI** - `cartService.js` exists and mirrors the real backend Cart
  API, but the booking flow goes through direct `POST /api/bookings` instead, matching the BRD's
  described Tour Details → Booking Form → Passenger Details → Summary → Confirmation flow more
  directly. Wiring an actual "Add to Cart" UI on top of the existing service is straightforward
  if you want the staging-cart behavior later.
- **Tour Listing cards don't show a "starting location"** - the `Tour` entity has no location
  field (only `JourneyDetail`/`StayMeal` sub-resources do, and fetching those per card in a list
  would be expensive). Omitted rather than fabricated from the tour title.
- **Search doesn't filter by departure date** - documented as a backend gap in the backend
  README (`Tour` has no bidirectional relation to `TourSchedule`).
- **No dedicated admin CRUD screens for Category/Tour/User management** - the backend endpoints
  exist (and are exercised elsewhere - Bulk Upload creates tours, the Bookings table reads/writes
  bookings), but building full create/edit forms for every entity was deprioritized in favor of
  finishing the customer-facing purchase flow end-to-end. This is flagged directly in the Admin
  Dashboard UI, not hidden.
- **No Reports module** - there's no reporting endpoint beyond the dashboard stats; Reports
  reuses that same data rather than inventing new numbers.
- **Payment is simulated** (documented in the backend README) - the payment method picker on
  Booking Summary is real UI, but there's no actual payment gateway behind it.

## Next steps if continuing
1. `npm install && npm run dev`, with the backend running and roles seeded.
2. Register a customer, browse a category with sub-categories and one without, confirm the
   branching works without any hardcoded logic.
3. Walk the full booking flow end-to-end, including a review submission.
4. Log in as an admin, check the dashboard stats, update a booking's status, try an Excel upload.
5. If the CDAC demo needs Category/Tour admin CRUD forms, that's the highest-value remaining gap.
