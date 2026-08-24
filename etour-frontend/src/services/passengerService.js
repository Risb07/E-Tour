import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function addPassenger(payload) {
  return httpClient(API_ENDPOINTS.PASSENGERS.BASE, { method: "POST", body: payload });
}

export function fetchPassengersForBooking(bookingId) {
  return httpClient(API_ENDPOINTS.PASSENGERS.BY_BOOKING(bookingId));
}
