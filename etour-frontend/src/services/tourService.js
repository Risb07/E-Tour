import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function fetchTourById(id) {
  return httpClient(API_ENDPOINTS.TOURS.BY_ID(id));
}

/** Admin tour picker (e.g. itinerary management) - full unfiltered list. */
export function fetchAllTours() {
  return httpClient(API_ENDPOINTS.TOURS.BASE);
}

/** Aggregated: { tour, schedules, itinerary, reviews, reviewSummary } */
export function fetchTourDetails(id) {
  return httpClient(API_ENDPOINTS.TOURS.DETAILS(id));
}

export function fetchTourJourney(id) {
  return httpClient(API_ENDPOINTS.TOURS.JOURNEY(id));
}

export function fetchTourStayMeals(id) {
  return httpClient(API_ENDPOINTS.TOURS.STAY_MEALS(id));
}

export function fetchTourContent(id) {
  return httpClient(API_ENDPOINTS.TOURS.CONTENT(id));
}

export function fetchTourMedia(id) {
  return httpClient(API_ENDPOINTS.TOURS.MEDIA(id));
}

export function fetchTourAddons(id) {
  return httpClient(API_ENDPOINTS.TOURS.ADDONS(id));
}

// Add-on writes are admin-only (enforced by @PreAuthorize on
// TourDetailController); the read above is public because the booking step
// needs it before login.

export function createTourAddon(tourId, addon) {
  return httpClient(API_ENDPOINTS.TOURS.ADDONS(tourId), { method: "POST", body: addon });
}

export function updateTourAddon(tourId, addonId, addon) {
  return httpClient(API_ENDPOINTS.TOURS.ADDON_ITEM(tourId, addonId), { method: "PUT", body: addon });
}

export function deleteTourAddon(tourId, addonId) {
  return httpClient(API_ENDPOINTS.TOURS.ADDON_ITEM(tourId, addonId), { method: "DELETE" });
}

/**
 * @param {object} filters - tourName, tourCode, categoryId, minPrice, maxPrice,
 *   minDuration, maxDuration, page, size, sortBy, sortDir
 * @returns {Promise<{content: object[], totalElements: number, totalPages: number, number: number}>}
 */
export function searchTours(filters = {}) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.append(key, value);
    }
  });
  return httpClient(`${API_ENDPOINTS.TOURS.SEARCH}?${params.toString()}`);
}

export function fetchSchedulesForTour(tourId) {
  return httpClient(API_ENDPOINTS.TOUR_SCHEDULES.BY_TOUR(tourId));
}

export function createTour(tour) {
  return httpClient(API_ENDPOINTS.TOURS.BASE, { method: "POST", body: tour });
}

export function updateTour(id, tour) {
  return httpClient(API_ENDPOINTS.TOURS.BY_ID(id), { method: "PUT", body: tour });
}

export function deleteTour(id) {
  return httpClient(API_ENDPOINTS.TOURS.BY_ID(id), { method: "DELETE" });
}

export function fetchAllSchedules() {
  return httpClient(API_ENDPOINTS.TOUR_SCHEDULES.BASE);
}

export function createSchedule(tourId, schedule) {
  return httpClient(API_ENDPOINTS.TOUR_SCHEDULES.BY_TOUR(tourId), { method: "POST", body: schedule });
}

export function updateSchedule(scheduleId, tourId, schedule) {
  return httpClient(API_ENDPOINTS.TOUR_SCHEDULES.BY_ID_TOUR(scheduleId, tourId), {
    method: "PUT",
    body: schedule,
  });
}

export function deleteSchedule(scheduleId) {
  return httpClient(API_ENDPOINTS.TOUR_SCHEDULES.BY_ID(scheduleId), { method: "DELETE" });
}

export function addTourMedia(tourId, media) {
  return httpClient(API_ENDPOINTS.TOURS.MEDIA(tourId), { method: "POST", body: media });
}

export function deleteTourMedia(tourId, mediaId) {
  return httpClient(API_ENDPOINTS.TOURS.MEDIA_ITEM(tourId, mediaId), { method: "DELETE" });
}
