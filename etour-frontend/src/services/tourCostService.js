import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// Admin CRUD for tour cost sheets (per-tour pricing + validity window).

export function fetchTourCosts() {
  return httpClient(API_ENDPOINTS.TOUR_COSTS.BASE);
}

export function fetchTourCostsForTour(tourId) {
  return httpClient(API_ENDPOINTS.TOUR_COSTS.BY_TOUR(tourId));
}

export function createTourCost(tourId, cost) {
  return httpClient(API_ENDPOINTS.TOUR_COSTS.BY_TOUR(tourId), { method: "POST", body: cost });
}

export function updateTourCost(costId, tourId, cost) {
  return httpClient(API_ENDPOINTS.TOUR_COSTS.BY_ID_TOUR(costId, tourId), { method: "PUT", body: cost });
}

export function deleteTourCost(costId) {
  return httpClient(API_ENDPOINTS.TOUR_COSTS.BY_ID(costId), { method: "DELETE" });
}
