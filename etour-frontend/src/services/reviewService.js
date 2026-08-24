import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function fetchReviewsForTour(tourId) {
  return httpClient(API_ENDPOINTS.REVIEWS.BY_TOUR(tourId));
}

/** Admin moderation: every review across all tours. */
export function fetchAllReviews() {
  return httpClient(API_ENDPOINTS.REVIEWS.BASE);
}

/**
 * Admin moderation: remove any review by id. Customers cannot reach this -
 * they manage only their own review through deleteMyReview below.
 */
export function deleteReviewAsAdmin(reviewId) {
  return httpClient(API_ENDPOINTS.REVIEWS.BY_ID(reviewId), { method: "DELETE" });
}

export function addMyReview(tourId, payload) {
  return httpClient(API_ENDPOINTS.REVIEWS.ADD_MINE(tourId), { method: "POST", body: payload });
}

export function editMyReview(tourId, payload) {
  return httpClient(API_ENDPOINTS.REVIEWS.EDIT_MINE(tourId), { method: "PUT", body: payload });
}

export function deleteMyReview(reviewId) {
  return httpClient(API_ENDPOINTS.REVIEWS.DELETE_MINE(reviewId), { method: "DELETE" });
}
