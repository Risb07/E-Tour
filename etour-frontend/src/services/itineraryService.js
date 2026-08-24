import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function fetchItinerary(tourId) {
  return httpClient(API_ENDPOINTS.TOURS.ITINERARY(tourId));
}

/** @param {{dayNumber: number, title: string, description: string}} dto */
export function addItineraryDay(tourId, dto) {
  return httpClient(API_ENDPOINTS.TOURS.ITINERARY(tourId), { method: "POST", body: dto });
}

export function updateItineraryDay(tourId, itineraryId, dto) {
  return httpClient(API_ENDPOINTS.TOURS.ITINERARY_ITEM(tourId, itineraryId), { method: "PUT", body: dto });
}

export function deleteItineraryDay(tourId, itineraryId) {
  return httpClient(API_ENDPOINTS.TOURS.ITINERARY_ITEM(tourId, itineraryId), { method: "DELETE" });
}
