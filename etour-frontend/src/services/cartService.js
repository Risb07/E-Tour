import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/** @param {{scheduleId: number, numberOfPassengers: number, addons: {addonId:number, quantity:number}[]}} payload */
export function addToCart(payload) {
  return httpClient(API_ENDPOINTS.CART.BASE, { method: "POST", body: payload });
}

export function fetchMyCart() {
  return httpClient(API_ENDPOINTS.CART.BASE);
}

export function removeFromCart(cartId) {
  return httpClient(API_ENDPOINTS.CART.BY_ID(cartId), { method: "DELETE" });
}

/**
 * Converts a cart item into a PENDING booking with an ESTIMATED total (the
 * backend doesn't know passenger ages yet). The caller must still collect
 * passenger details and call bookingService.finalizePassengers() before the
 * booking can be paid for.
 */
export function checkoutCartItem(cartId) {
  return httpClient(API_ENDPOINTS.CART.CHECKOUT(cartId), { method: "POST" });
}
