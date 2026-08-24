import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * The tour page's "Good to know" tabs - Passport & Visa, Weather, Do's &
 * Don'ts, Terms & Conditions. Reads are public (a visitor browsing a tour
 * needs them before login); writes are admin-only, enforced by @PreAuthorize
 * on TourDetailController.
 *
 * Note this is tour-scoped content and is unrelated to `contentService`,
 * which serves the site-wide `content` table (labels, hero copy, FAQ).
 */
export function fetchTourContent(tourId) {
  return httpClient(API_ENDPOINTS.TOURS.CONTENT(tourId));
}

/**
 * Upsert: the backend keeps at most one row per tour + content type +
 * language, so saving the same tab twice edits it rather than duplicating it.
 *
 * @param {{contentType: string, contentText: string, languageCode?: string}} dto
 */
export function saveTourContent(tourId, dto) {
  return httpClient(API_ENDPOINTS.TOURS.CONTENT(tourId), { method: "POST", body: dto });
}

export function deleteTourContent(tourId, tourContentId) {
  return httpClient(API_ENDPOINTS.TOURS.CONTENT_ITEM(tourId, tourContentId), { method: "DELETE" });
}
