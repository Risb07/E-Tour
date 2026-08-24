// One source of truth for backend paths.
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/api/auth/login",
    // Which sign-in methods this deployment has configured. Lets the login
    // page hide the Google button when the backend has no Google credentials.
    PROVIDERS: "/api/auth/providers",
  },
  // Not an /api path: this is a top-level browser navigation that starts
  // Spring Security's OAuth 2.0 authorization code flow. It is never fetched
  // with the httpClient - the browser must actually leave the SPA so Google
  // can show its consent screen.
  OAUTH2: { GOOGLE_AUTHORIZE: "/oauth2/authorization/google" },
  USERS: { REGISTER: "/api/users/register" },
  CATEGORIES: {
    BASE: "/api/categories",
    BY_ID: (id) => `/api/categories/${id}`,
  },
  TOURS: {
    BASE: "/api/tours",
    BY_ID: (id) => `/api/tours/${id}`,
    DETAILS: (id) => `/api/tours/${id}/details`,
    SEARCH: "/api/tours/search",
    JOURNEY: (id) => `/api/tours/${id}/journey`,
    STAY_MEALS: (id) => `/api/tours/${id}/stay-meals`,
    STAY_MEAL_ITEM: (id, stayMealId) => `/api/tours/${id}/stay-meals/${stayMealId}`,
    CONTENT: (id) => `/api/tours/${id}/content`,
    CONTENT_ITEM: (id, tourContentId) => `/api/tours/${id}/content/${tourContentId}`,
    MEDIA: (id) => `/api/tours/${id}/media`,
    MEDIA_ITEM: (id, mediaId) => `/api/tours/${id}/media/${mediaId}`,
    ADDONS: (id) => `/api/tours/${id}/addons`,
    ADDON_ITEM: (id, addonId) => `/api/tours/${id}/addons/${addonId}`,
    ITINERARY: (id) => `/api/tours/${id}/itinerary`,
    ITINERARY_ITEM: (id, itineraryId) => `/api/tours/${id}/itinerary/${itineraryId}`,
  },
  TOUR_SCHEDULES: {
    BASE: "/api/tour-schedules",
    BY_ID: (scheduleId) => `/api/tour-schedules/${scheduleId}`,
    BY_TOUR: (tourId) => `/api/tour-schedules/tour/${tourId}`,
    BY_ID_TOUR: (scheduleId, tourId) => `/api/tour-schedules/${scheduleId}/tour/${tourId}`,
  },
  TOUR_COSTS: {
    BASE: "/api/tour-costs",
    BY_ID: (costId) => `/api/tour-costs/${costId}`,
    BY_TOUR: (tourId) => `/api/tour-costs/tour/${tourId}`,
    BY_ID_TOUR: (costId, tourId) => `/api/tour-costs/${costId}/tour/${tourId}`,
  },
  CART: {
    BASE: "/api/cart",
    CHECKOUT: (cartId) => `/api/cart/${cartId}/checkout`,
    BY_ID: (id) => `/api/cart/${id}`,
  },
  BOOKINGS: {
    BASE: "/api/bookings",
    ME: "/api/bookings/me",
    BY_ID: (id) => `/api/bookings/${id}`,
    STATUS: (id) => `/api/bookings/${id}/status`,
    QUOTE: "/api/bookings/quote",
    PASSENGERS: (id) => `/api/bookings/${id}/passengers`,
  },
  AD_BANNERS: {
    BASE: "/api/ad-banners",
    BY_ID: (id) => `/api/ad-banners/${id}`,
  },
  CRAWLING_TEXT: {
    BASE: "/api/crawling-text",
    BY_ID: (id) => `/api/crawling-text/${id}`,
  },
  NAV_MENU: {
    BASE: "/api/nav-menu",
    BY_ID: (id) => `/api/nav-menu/${id}`,
  },
  CONTENT: {
    BASE: "/api/content",
    BY_ID: (id) => `/api/content/${id}`,
  },
  PASSENGERS: {
    BASE: "/api/passengers",
    BY_ID: (id) => `/api/passengers/${id}`,
    BY_BOOKING: (bookingId) => `/api/passengers/booking/${bookingId}`,
  },
  PAYMENTS: {
    BASE: "/api/payments",
    BY_ID: (id) => `/api/payments/${id}`,
    CARD: "/api/payments/card",
    SUMMARY: (bookingId) => `/api/payments/booking/${bookingId}/summary`,
  },
  INVOICES: {
    ME: "/api/invoices/me",
    BY_BOOKING: (bookingId) => `/api/invoices/booking/${bookingId}`,
    RECEIPT: (bookingId) => `/api/invoices/booking/${bookingId}/receipt`,
  },
  REVIEWS: {
    BASE: "/api/reviews",
    BY_ID: (reviewId) => `/api/reviews/${reviewId}`,
    BY_TOUR: (tourId) => `/api/reviews/tour/${tourId}`,
    TOP5: (tourId) => `/api/reviews/tour/${tourId}/top5`,
    ADD_MINE: (tourId) => `/api/reviews/me/tour/${tourId}`,
    EDIT_MINE: (tourId) => `/api/reviews/me/tour/${tourId}`,
    DELETE_MINE: (reviewId) => `/api/reviews/me/${reviewId}`,
  },
  ROOM_CHARGES: {
    BY_TOUR: (tourId) => `/api/room-charges/tour/${tourId}`,
    BY_ID: (id) => `/api/room-charges/${id}`,
  },
  LOCATIONS: {
    BASE: "/api/locations",
    BY_ID: (id) => `/api/locations/${id}`,
  },
  CUSTOMER: { ME: "/api/customer/me" },
  WISHLIST: {
    BASE: "/api/wishlist",
    BY_TOUR: (tourId) => `/api/wishlist/tour/${tourId}`,
  },
  CONTACT: {
    BASE: "/api/contact",
    BY_ID: (id) => `/api/contact/${id}`,
    STATUS: (id) => `/api/contact/${id}/status`,
    COUNT: "/api/contact/count",
  },
  NEWSLETTER: {
    BASE: "/api/newsletter",
    SUBSCRIBE: "/api/newsletter/subscribe",
    UNSUBSCRIBE: "/api/newsletter/unsubscribe",
    BY_ID: (id) => `/api/newsletter/${id}`,
  },
  ADMIN: {
    DASHBOARD: "/api/admin/dashboard",
    EXCEL_UPLOAD: "/api/excel-upload",
    EXCEL_TEMPLATE: "/api/excel-upload/template",
  },
  // Served by the notification microservice, not by the backend. The /svc
  // prefix is what keeps the two apart: nginx routes all of /api/ to whichever
  // backend is running, and everything under /svc/ to the microservice, so
  // neither can ever shadow the other's routes.
  NOTIFICATIONS: {
    BASE: "/svc/notifications",
    BY_ID: (id) => `/svc/notifications/${id}`,
    RETRY: (id) => `/svc/notifications/${id}/retry`,
    STATS: "/svc/notifications/stats",
    TEMPLATES: "/svc/notifications/templates",
  },
};
