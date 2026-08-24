import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * Multipath rules - automatically place tours onto extra navigation paths
 * (categories, and optionally sector products) instead of linking each tour
 * by hand. Admin only.
 */

export function fetchMultipathRules() {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.BASE);
}

export function createMultipathRule(rule) {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.BASE, { method: "POST", body: rule });
}

export function updateMultipathRule(id, rule) {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.BY_ID(id), { method: "PUT", body: rule });
}

export function deleteMultipathRule(id) {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.BY_ID(id), { method: "DELETE" });
}

/** Dry run - returns what would change without writing anything. */
export function previewMultipath() {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.PREVIEW, { method: "POST" });
}

/** Applies all active rules. Idempotent - existing links are skipped. */
export function applyMultipath() {
  return httpClient(API_ENDPOINTS.MULTIPATH_RULES.APPLY, { method: "POST" });
}
