// Route path constants avoid typo'd string literals scattered across
// <Link> and navigate() calls throughout the app.
export const ROUTE_PATHS = {
  // BRD 3.1 - Showcase/intro page is the actual entry point; "Skip"/"Continue"
  // both land on Home.
  SHOWCASE: "/",
  HOME: "/home",
  LOGIN: "/login",
  REGISTER: "/register",
  // Where the backend sends the browser after Google has authenticated the
  // user. Must match app.oauth2.frontend-redirect-uri in the backend config.
  OAUTH2_CALLBACK: "/oauth2/callback",
  UNAUTHORIZED: "/unauthorized",

  // Company pages - public, inside the normal site shell.
  ABOUT: "/about",
  CONTACT: "/contact",

  CATEGORY_SUBCATEGORIES: "/categories/:categoryId/subcategories",
  categorySubcategories: (categoryId) => `/categories/${categoryId}/subcategories`,

  TOUR_LISTING: "/categories/:categoryId/tours",
  tourListing: (categoryId) => `/categories/${categoryId}/tours`,

  SEARCH: "/search",

  TOUR_DETAILS: "/tours/:tourId",
  tourDetails: (tourId) => `/tours/${tourId}`,

  BOOKING_FORM: "/tours/:tourId/book",
  bookingForm: (tourId) => `/tours/${tourId}/book`,
  BOOKING_PASSENGERS: "/booking/passengers",
  BOOKING_SUMMARY: "/booking/summary",
  // BRD 3.7 - dedicated payment step between summary and confirmation.
  BOOKING_PAYMENT: "/booking/:bookingId/payment",
  bookingPayment: (bookingId) => `/booking/${bookingId}/payment`,
  PAYMENT_SUCCESS: "/booking/:bookingId/success",
  paymentSuccess: (bookingId) => `/booking/${bookingId}/success`,
  BOOKING_CONFIRMATION: "/booking/confirmation",

  CART: "/cart",
  CART_CHECKOUT: "/cart/checkout/:bookingId",
  cartCheckout: (bookingId) => `/cart/checkout/${bookingId}`,

  CUSTOMER_DASHBOARD: "/dashboard",
  PROFILE: "/profile",
  WISHLIST: "/wishlist",

  ADMIN_DASHBOARD: "/admin",
  ADMIN_BOOKINGS: "/admin/bookings",
  ADMIN_ROOM_CHARGES: "/admin/room-charges",
  ADMIN_ADDONS: "/admin/addons",
  ADMIN_EXCEL_UPLOAD: "/admin/excel-upload",
  ADMIN_ITINERARY: "/admin/itinerary",
  ADMIN_STAY_MEALS: "/admin/stay-meals",
  ADMIN_TOUR_CONTENT: "/admin/good-to-know",
  ADMIN_REVIEWS: "/admin/reviews",
  // Served by the notification microservice rather than the backend.
  ADMIN_NOTIFICATIONS: "/admin/notifications",

  ADMIN_TOURS: "/admin/tours",
  ADMIN_CATEGORIES: "/admin/categories",
  ADMIN_SCHEDULES: "/admin/schedules",
  ADMIN_TOUR_COSTS: "/admin/tour-costs",
  ADMIN_MEDIA: "/admin/media",
  ADMIN_CONTENT: "/admin/content",
  ADMIN_AD_BANNERS: "/admin/ad-banners",
  ADMIN_CRAWLING_TEXT: "/admin/crawling-text",
  ADMIN_NAV_MENU: "/admin/nav-menu",
};
