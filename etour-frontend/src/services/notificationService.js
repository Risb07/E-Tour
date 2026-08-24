import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * Client for the notification microservice.
 *
 * Everything here goes to /svc/... rather than /api/..., so these calls are
 * served by a separate service with its own database - not by the Java or .NET
 * backend. It reuses `httpClient` unchanged because the microservice speaks the
 * same two conventions: the eTour bearer token, and the
 * {timestamp, status, message, path} error envelope httpClient reads.
 */

/** Admin: paginated delivery log. `status` is PENDING | SENT | FAILED, or null for all. */
export function fetchNotifications({ status = null, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.set("status", status);
  return httpClient(`${API_ENDPOINTS.NOTIFICATIONS.BASE}?${params.toString()}`);
}

/** Admin: counts per status for the summary cards. */
export function fetchNotificationStats() {
  return httpClient(API_ENDPOINTS.NOTIFICATIONS.STATS);
}

/** Admin: the available templates and their placeholders. */
export function fetchNotificationTemplates() {
  return httpClient(API_ENDPOINTS.NOTIFICATIONS.TEMPLATES);
}

export function fetchNotification(id) {
  return httpClient(API_ENDPOINTS.NOTIFICATIONS.BY_ID(id));
}

/**
 * Queue a message. Returns 202 with the queued row - not "sent", because
 * delivery happens on the service's worker a moment later.
 *
 * @param {{recipient: string, template: string, variables?: object, idempotencyKey?: string}} payload
 */
export function sendNotification(payload) {
  return httpClient(API_ENDPOINTS.NOTIFICATIONS.BASE, { method: "POST", body: payload });
}

/**
 * Admin: put a dead letter back on the queue. This is the manual counterpart
 * to the service's one-retry policy - it stops trying by itself after two
 * attempts, and a human decides whether the underlying problem is fixed.
 */
export function retryNotification(id) {
  return httpClient(API_ENDPOINTS.NOTIFICATIONS.RETRY(id), { method: "POST" });
}
