import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * @param {{scheduleId: number, numberOfPassengers: number, addons: {addonId:number, quantity:number}[],
 *   passengers?: object[]}} payload - include `passengers` (matching
 *   numberOfPassengers) to price + persist everything in one call.
 */
export function createBooking(payload) {
  return httpClient(API_ENDPOINTS.BOOKINGS.BASE, { method: "POST", body: payload });
}

/**
 * Read-only price preview (BRD 3.7 "Done" summary) - nothing is created.
 * Requires a full passenger list (with dob) to compute age-banded pricing.
 */
export function quoteBooking(payload) {
  return httpClient(API_ENDPOINTS.BOOKINGS.QUOTE, { method: "POST", body: payload });
}

/** Attaches passengers to a cart-originated booking and recalculates its total. */
export function finalizeBookingPassengers(bookingId, passengers) {
  return httpClient(API_ENDPOINTS.BOOKINGS.PASSENGERS(bookingId), { method: "PATCH", body: passengers });
}

export function fetchMyBookings() {
  return httpClient(API_ENDPOINTS.BOOKINGS.ME);
}

export function fetchBookingById(id) {
  return httpClient(API_ENDPOINTS.BOOKINGS.BY_ID(id));
}

export function cancelBooking(id) {
  return httpClient(API_ENDPOINTS.BOOKINGS.BY_ID(id), { method: "DELETE" });
}

// Admin only
export function fetchAllBookings() {
  return httpClient(API_ENDPOINTS.BOOKINGS.BASE);
}

export function updateBookingStatus(id, status) {
  return httpClient(`${API_ENDPOINTS.BOOKINGS.STATUS(id)}?status=${status}`, { method: "PATCH" });
}
