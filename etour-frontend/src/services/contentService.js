import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// BRD 2.1 - database-driven site content with multilingual support.
// Content records can be filtered by language and page name.

export function fetchContentEntries(language, pageName) {
  const params = new URLSearchParams();
  if (language) params.append("language", language);
  if (pageName) params.append("pageName", pageName);
  const query = params.toString();
  return httpClient(query ? `${API_ENDPOINTS.CONTENT.BASE}?${query}` : API_ENDPOINTS.CONTENT.BASE);
}

export function fetchContentByKey(key, language = "en") {
  return httpClient(`${API_ENDPOINTS.CONTENT.BASE}/${key}?language=${language}`);
}

/** Admin list - includes retired (status = false) rows so they can be re-enabled. */
export function fetchAllContent() {
  return httpClient(`${API_ENDPOINTS.CONTENT.BASE}?includeInactive=true`);
}

export function createContent(content) {
  return httpClient(API_ENDPOINTS.CONTENT.BASE, { method: "POST", body: content });
}

export function updateContent(id, content) {
  return httpClient(API_ENDPOINTS.CONTENT.BY_ID(id), { method: "PUT", body: content });
}

export function deleteContent(id) {
  return httpClient(API_ENDPOINTS.CONTENT.BY_ID(id), { method: "DELETE" });
}
