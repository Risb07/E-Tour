import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";
import { API_BASE_URL } from "../config/env";

/**
 * @param {{ firstName: string, lastName: string, email: string, password: string, phone: string }} payload
 * @returns {Promise<{userId: number, firstName: string, lastName: string, email: string, role: string}>}
 */
export function registerUser(payload) {
  return httpClient(API_ENDPOINTS.USERS.REGISTER, {
    method: "POST",
    body: payload,
  });
}

/**
 * @param {{ email: string, password: string }} payload
 * @returns {Promise<{token: string, message: string, userId: number, firstName: string, lastName: string, email: string, role: string}>}
 */
export function loginUser(payload) {
  return httpClient(API_ENDPOINTS.AUTH.LOGIN, {
    method: "POST",
    body: payload,
  });
}

/**
 * Which sign-in methods the backend actually has configured.
 *
 * Never throws: if the endpoint is missing (an older backend) or the network
 * is down, it resolves to "no social providers" so the login form still
 * renders normally instead of the page erroring out over an optional feature.
 *
 * @returns {Promise<{ google: boolean }>}
 */
export async function fetchAuthProviders() {
  try {
    const data = await httpClient(API_ENDPOINTS.AUTH.PROVIDERS);
    return { google: Boolean(data?.google) };
  } catch {
    return { google: false };
  }
}

/**
 * The absolute URL that starts Google sign-in.
 *
 * This must be an absolute URL on the *backend* origin, and it must be
 * navigated to with `window.location.assign` rather than fetched: the browser
 * has to follow redirects to Google's consent screen and back, which XHR
 * cannot do. It is also why the flow carries no Authorization header - the
 * user is not authenticated yet.
 *
 * @returns {string}
 */
export function getGoogleLoginUrl() {
  return `${API_BASE_URL}${API_ENDPOINTS.OAUTH2.GOOGLE_AUTHORIZE}`;
}
