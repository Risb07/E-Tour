import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/** Public - no account required. @param {{email: string, name?: string}} payload */
export function subscribeToNewsletter(payload) {
  return httpClient(API_ENDPOINTS.NEWSLETTER.SUBSCRIBE, { method: "POST", body: payload });
}

export function unsubscribeFromNewsletter(email) {
  return httpClient(API_ENDPOINTS.NEWSLETTER.UNSUBSCRIBE, { method: "POST", body: { email } });
}

// ----- Admin -----

export function fetchSubscribers({ active, page = 0, size = 25 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (active !== undefined && active !== null && active !== "") {
    params.append("active", active);
  }
  return httpClient(`${API_ENDPOINTS.NEWSLETTER.BASE}?${params.toString()}`);
}

export function deleteSubscriber(id) {
  return httpClient(API_ENDPOINTS.NEWSLETTER.BY_ID(id), { method: "DELETE" });
}
