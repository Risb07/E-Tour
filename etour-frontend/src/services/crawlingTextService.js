import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// BRD 3.2 - Home page crawling (scrolling) ticker text.
export function fetchCrawlingTexts() {
  return httpClient(API_ENDPOINTS.CRAWLING_TEXT.BASE);
}

// ----- Admin CRUD (includes inactive records so admins can re-enable) -----

export function fetchAllCrawlingTexts(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.CRAWLING_TEXT.BASE}?includeInactive=${includeInactive}`);
}

export function createCrawlingText(text) {
  return httpClient(API_ENDPOINTS.CRAWLING_TEXT.BASE, { method: "POST", body: text });
}

export function updateCrawlingText(id, text) {
  return httpClient(API_ENDPOINTS.CRAWLING_TEXT.BY_ID(id), { method: "PUT", body: text });
}

export function deleteCrawlingText(id) {
  return httpClient(API_ENDPOINTS.CRAWLING_TEXT.BY_ID(id), { method: "DELETE" });
}
