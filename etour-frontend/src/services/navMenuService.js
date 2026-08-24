import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// BRD 2.1 - database-driven top menu bar, with mouse-over multi-level
// support via parent/child items.
export function fetchNavMenu() {
  return httpClient(API_ENDPOINTS.NAV_MENU.BASE);
}

// ----- Admin CRUD (includes inactive records so admins can re-enable) -----

export function fetchAllNavMenuItems(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.NAV_MENU.BASE}?includeInactive=${includeInactive}`);
}

export function createNavMenuItem(item) {
  return httpClient(API_ENDPOINTS.NAV_MENU.BASE, { method: "POST", body: item });
}

export function updateNavMenuItem(id, item) {
  return httpClient(API_ENDPOINTS.NAV_MENU.BY_ID(id), { method: "PUT", body: item });
}

export function deleteNavMenuItem(id) {
  return httpClient(API_ENDPOINTS.NAV_MENU.BY_ID(id), { method: "DELETE" });
}
