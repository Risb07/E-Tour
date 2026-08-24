import { API_BASE_URL } from "../config/env";
import { loadSession, clearSession } from "../utils/storage";

const DEFAULT_TIMEOUT_MS = 15000;

/**
 * Normalized error shape thrown by every httpClient call, so callers can
 * always do `catch (err) { err.status, err.message }` regardless of
 * whether the failure was a validation error, an auth error, or the
 * network dying entirely.
 */
export class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

function buildHeaders(customHeaders, isFormData) {
  const headers = { ...customHeaders };

  // Let the browser set the multipart boundary itself - never set
  // Content-Type manually for FormData bodies.
  if (!isFormData) {
    headers["Content-Type"] = "application/json";
  }

  const session = loadSession();
  if (session?.token) {
    headers["Authorization"] = `Bearer ${session.token}`;
  }

  return headers;
}

async function parseResponseBody(response) {
  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) return null;

  try {
    return await response.json();
  } catch {
    return null;
  }
}

/**
 * @param {string} path - e.g. "/api/auth/login"
 * @param {object} options - { method, body, headers, isFormData, signal }
 */
export async function httpClient(path, options = {}) {
  const { method = "GET", body, headers, isFormData = false } = options;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), DEFAULT_TIMEOUT_MS);

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: buildHeaders(headers, isFormData),
      body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
      signal: controller.signal,
    });
  } catch (networkError) {
    clearTimeout(timeoutId);
    if (networkError.name === "AbortError") {
      throw new ApiError("Request timed out. Please try again.", 0);
    }
    throw new ApiError("Network error. Check your connection and try again.", 0);
  }
  clearTimeout(timeoutId);

  const data = await parseResponseBody(response);

  if (response.status === 401) {
    // Session expired or invalid - force a clean logout state. Components
    // reading AuthContext will react to this on their next render.
    clearSession();
    throw new ApiError(data?.message || "Session expired. Please log in again.", 401, data);
  }

  if (!response.ok) {
    const message = data?.message || data?.error || `Request failed (${response.status})`;
    throw new ApiError(message, response.status, data);
  }

  return data;
}
