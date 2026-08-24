import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * "Stay & Meals" rows for a tour - one entry per day, naming the hotel and
 * which meals are included. Reads are public (the tour page tab needs them
 * before login); writes are admin-only, enforced by @PreAuthorize on
 * TourDetailController.
 */
export function fetchStayMeals(tourId) {
  return httpClient(API_ENDPOINTS.TOURS.STAY_MEALS(tourId));
}

/** @param {{dayNumber: number, hotelName: string, locationId: number|null, breakfast: boolean, lunch: boolean, dinner: boolean}} dto */
export function addStayMeal(tourId, dto) {
  return httpClient(API_ENDPOINTS.TOURS.STAY_MEALS(tourId), { method: "POST", body: dto });
}

export function deleteStayMeal(tourId, stayMealId) {
  return httpClient(API_ENDPOINTS.TOURS.STAY_MEAL_ITEM(tourId, stayMealId), { method: "DELETE" });
}

/** Optional location dropdown on the admin form. */
export function fetchLocations() {
  return httpClient(API_ENDPOINTS.LOCATIONS.BASE);
}
