import { Suspense, lazy } from "react";
import { Routes, Route } from "react-router-dom";
import Loader from "../components/common/Loader";
import MainLayout from "../components/layout/MainLayout";
import AdminLayout from "../components/layout/AdminLayout";
import ProtectedRoute from "./ProtectedRoute";
import { ROUTE_PATHS } from "../constants/routes";
import { ROLES } from "../constants/roles";

// Lazy-loaded for code splitting - each route only pays for what it needs.
const ShowcasePage = lazy(() => import("../pages/ShowcasePage"));
const HomePage = lazy(() => import("../pages/HomePage"));
const LoginPage = lazy(() => import("../pages/LoginPage"));
const RegisterPage = lazy(() => import("../pages/RegisterPage"));
const OAuth2CallbackPage = lazy(() => import("../pages/OAuth2CallbackPage"));
const UnauthorizedPage = lazy(() => import("../pages/UnauthorizedPage"));
const NotFoundPage = lazy(() => import("../pages/NotFoundPage"));
const AboutPage = lazy(() => import("../pages/AboutPage"));
const ContactPage = lazy(() => import("../pages/ContactPage"));

const SubCategoryListingPage = lazy(() => import("../pages/SubCategoryListingPage"));
const TourListingPage = lazy(() => import("../pages/TourListingPage"));
const SearchPage = lazy(() => import("../pages/SearchPage"));
const TourDetailsPage = lazy(() => import("../pages/TourDetailsPage"));

const BookingFormPage = lazy(() => import("../pages/BookingFormPage"));
const PassengerDetailsPage = lazy(() => import("../pages/PassengerDetailsPage"));
const BookingSummaryPage = lazy(() => import("../pages/BookingSummaryPage"));
const BookingConfirmationPage = lazy(() => import("../pages/BookingConfirmationPage"));
const PaymentPage = lazy(() => import("../pages/PaymentPage"));
const PaymentSuccessPage = lazy(() => import("../pages/PaymentSuccessPage"));

const CartPage = lazy(() => import("../pages/CartPage"));
const CartCheckoutPage = lazy(() => import("../pages/CartCheckoutPage"));
const WishlistPage = lazy(() => import("../pages/WishlistPage"));

const CustomerDashboardPage = lazy(() => import("../pages/CustomerDashboardPage"));
const ProfilePage = lazy(() => import("../pages/ProfilePage"));

const AdminDashboardPage = lazy(() => import("../pages/admin/AdminDashboardPage"));
const AdminBookingsPage = lazy(() => import("../pages/admin/AdminBookingsPage"));
const AdminExcelUploadPage = lazy(() => import("../pages/admin/AdminExcelUploadPage"));
const AdminRoomChargesPage = lazy(() => import("../pages/admin/AdminRoomChargesPage"));
const AdminAddonsPage = lazy(() => import("../pages/admin/AdminAddonsPage"));
const AdminItineraryPage = lazy(() => import("../pages/admin/AdminItineraryPage"));
const AdminStayMealsPage = lazy(() => import("../pages/admin/AdminStayMealsPage"));
const AdminTourContentPage = lazy(() => import("../pages/admin/AdminTourContentPage"));
const AdminReviewsPage = lazy(() => import("../pages/admin/AdminReviewsPage"));
const AdminNotificationsPage = lazy(() => import("../pages/admin/AdminNotificationsPage"));
const AdminToursPage = lazy(() => import("../pages/admin/AdminToursPage"));
const AdminCategoriesPage = lazy(() => import("../pages/admin/AdminCategoriesPage"));
const AdminSchedulesPage = lazy(() => import("../pages/admin/AdminSchedulesPage"));
const AdminTourCostsPage = lazy(() => import("../pages/admin/AdminTourCostsPage"));
const AdminMediaPage = lazy(() => import("../pages/admin/AdminMediaPage"));
const AdminContentPage = lazy(() => import("../pages/admin/AdminContentPage"));
const AdminAdBannersPage = lazy(() => import("../pages/admin/AdminAdBannersPage"));
const AdminCrawlingTextPage = lazy(() => import("../pages/admin/AdminCrawlingTextPage"));
const AdminNavMenuPage = lazy(() => import("../pages/admin/AdminNavMenuPage"));

export default function AppRoutes() {
  return (
    <Suspense fallback={<Loader label="Loading page..." />}>
      <Routes>
        {/* BRD 3.1 - Showcase page is the actual entry point, no navbar/footer */}
        <Route path={ROUTE_PATHS.SHOWCASE} element={<ShowcasePage />} />

        {/* Auth pages - no navbar/footer */}
        <Route path={ROUTE_PATHS.LOGIN} element={<LoginPage />} />
        <Route path={ROUTE_PATHS.REGISTER} element={<RegisterPage />} />
        {/* Google sign-in lands here with the JWT the backend minted. */}
        <Route path={ROUTE_PATHS.OAUTH2_CALLBACK} element={<OAuth2CallbackPage />} />

        {/* Public site shell */}
        <Route element={<MainLayout />}>
          <Route path={ROUTE_PATHS.HOME} element={<HomePage />} />
          <Route path={ROUTE_PATHS.ABOUT} element={<AboutPage />} />
          <Route path={ROUTE_PATHS.CONTACT} element={<ContactPage />} />
          <Route path={ROUTE_PATHS.UNAUTHORIZED} element={<UnauthorizedPage />} />
          <Route path={ROUTE_PATHS.CATEGORY_SUBCATEGORIES} element={<SubCategoryListingPage />} />
          <Route path={ROUTE_PATHS.TOUR_LISTING} element={<TourListingPage />} />
          <Route path={ROUTE_PATHS.SEARCH} element={<SearchPage />} />
          <Route path={ROUTE_PATHS.TOUR_DETAILS} element={<TourDetailsPage />} />

          {/* Any authenticated user (booking flow, cart, dashboard, profile) */}
          <Route element={<ProtectedRoute />}>
            <Route path={ROUTE_PATHS.BOOKING_FORM} element={<BookingFormPage />} />
            <Route path={ROUTE_PATHS.BOOKING_PASSENGERS} element={<PassengerDetailsPage />} />
            <Route path={ROUTE_PATHS.BOOKING_SUMMARY} element={<BookingSummaryPage />} />
            <Route path={ROUTE_PATHS.BOOKING_PAYMENT} element={<PaymentPage />} />
            <Route path={ROUTE_PATHS.PAYMENT_SUCCESS} element={<PaymentSuccessPage />} />
            {/* Kept so any existing link/bookmark still resolves. */}
            <Route path={ROUTE_PATHS.BOOKING_CONFIRMATION} element={<BookingConfirmationPage />} />
            <Route path={ROUTE_PATHS.CART} element={<CartPage />} />
            <Route path={ROUTE_PATHS.CART_CHECKOUT} element={<CartCheckoutPage />} />
            <Route path={ROUTE_PATHS.PROFILE} element={<ProfilePage />} />
          </Route>

          {/* Customer only */}
          <Route element={<ProtectedRoute allowedRoles={[ROLES.CUSTOMER]} />}>
            <Route path={ROUTE_PATHS.CUSTOMER_DASHBOARD} element={<CustomerDashboardPage />} />
            <Route path={ROUTE_PATHS.WISHLIST} element={<WishlistPage />} />
          </Route>

          {/* Catch-all inside the public shell so 404s still show nav/footer */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>

        {/* Admin shell - separate layout (sidebar, no public navbar/footer) */}
        <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
          <Route element={<AdminLayout />}>
            <Route path={ROUTE_PATHS.ADMIN_DASHBOARD} element={<AdminDashboardPage />} />
            <Route path={ROUTE_PATHS.ADMIN_BOOKINGS} element={<AdminBookingsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_ITINERARY} element={<AdminItineraryPage />} />
            <Route path={ROUTE_PATHS.ADMIN_STAY_MEALS} element={<AdminStayMealsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_TOUR_CONTENT} element={<AdminTourContentPage />} />
            <Route path={ROUTE_PATHS.ADMIN_REVIEWS} element={<AdminReviewsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_NOTIFICATIONS} element={<AdminNotificationsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_ROOM_CHARGES} element={<AdminRoomChargesPage />} />
            <Route path={ROUTE_PATHS.ADMIN_ADDONS} element={<AdminAddonsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_EXCEL_UPLOAD} element={<AdminExcelUploadPage />} />
            <Route path={ROUTE_PATHS.ADMIN_TOURS} element={<AdminToursPage />} />
            <Route path={ROUTE_PATHS.ADMIN_CATEGORIES} element={<AdminCategoriesPage />} />
            <Route path={ROUTE_PATHS.ADMIN_SCHEDULES} element={<AdminSchedulesPage />} />
            <Route path={ROUTE_PATHS.ADMIN_TOUR_COSTS} element={<AdminTourCostsPage />} />
            <Route path={ROUTE_PATHS.ADMIN_MEDIA} element={<AdminMediaPage />} />
            <Route path={ROUTE_PATHS.ADMIN_CONTENT} element={<AdminContentPage />} />
            <Route path={ROUTE_PATHS.ADMIN_AD_BANNERS} element={<AdminAdBannersPage />} />
            <Route path={ROUTE_PATHS.ADMIN_CRAWLING_TEXT} element={<AdminCrawlingTextPage />} />
            <Route path={ROUTE_PATHS.ADMIN_NAV_MENU} element={<AdminNavMenuPage />} />
          </Route>
        </Route>
      </Routes>
    </Suspense>
  );
}
