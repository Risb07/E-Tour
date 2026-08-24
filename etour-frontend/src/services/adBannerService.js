import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// BRD 3.1/3.2 - showcase page + home page ad banners. Position is a free-text
// admin-managed field (e.g. "LEFT", "RIGHT", "TOP", "BOTTOM_LEFT").
export function fetchAdBanners(position) {
  const query = position ? `?position=${encodeURIComponent(position)}` : "";
  return httpClient(`${API_ENDPOINTS.AD_BANNERS.BASE}${query}`);
}

// ----- Admin CRUD (includes inactive records so admins can re-enable) -----

export function fetchAllAdBanners(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.AD_BANNERS.BASE}?includeInactive=${includeInactive}`);
}

export function createAdBanner(banner) {
  return httpClient(API_ENDPOINTS.AD_BANNERS.BASE, { method: "POST", body: banner });
}

export function updateAdBanner(id, banner) {
  return httpClient(API_ENDPOINTS.AD_BANNERS.BY_ID(id), { method: "PUT", body: banner });
}

export function deleteAdBanner(id) {
  return httpClient(API_ENDPOINTS.AD_BANNERS.BY_ID(id), { method: "DELETE" });
}
