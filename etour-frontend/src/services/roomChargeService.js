import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * Room-sharing supplements per tour (BRD 3.7 room options).
 * Reads are public - the booking page needs prices before login.
 */
export function fetchRoomCharges(tourId, includeInactive = false) {
  return httpClient(`${API_ENDPOINTS.ROOM_CHARGES.BY_TOUR(tourId)}?includeInactive=${includeInactive}`);
}

// ----- Admin -----

export function createRoomCharge(tourId, charge) {
  return httpClient(API_ENDPOINTS.ROOM_CHARGES.BY_TOUR(tourId), { method: "POST", body: charge });
}

export function updateRoomCharge(roomChargeId, charge) {
  return httpClient(API_ENDPOINTS.ROOM_CHARGES.BY_ID(roomChargeId), { method: "PUT", body: charge });
}

export function deleteRoomCharge(roomChargeId) {
  return httpClient(API_ENDPOINTS.ROOM_CHARGES.BY_ID(roomChargeId), { method: "DELETE" });
}
