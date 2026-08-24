import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// Wishlist is always scoped to the logged-in customer server-side - no
// customerId is ever sent from here.

export function fetchMyWishlist() {
  return httpClient(API_ENDPOINTS.WISHLIST.BASE);
}

/** @returns {Promise<{saved: boolean}>} */
export function isTourSaved(tourId) {
  return httpClient(API_ENDPOINTS.WISHLIST.BY_TOUR(tourId));
}

export function addToWishlist(tourId) {
  return httpClient(API_ENDPOINTS.WISHLIST.BY_TOUR(tourId), { method: "POST" });
}

export function removeFromWishlist(tourId) {
  return httpClient(API_ENDPOINTS.WISHLIST.BY_TOUR(tourId), { method: "DELETE" });
}
