import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/**
 * Contact enquiries. Submitting is public - a visitor can write in without an
 * account - so this is the only call here that doesn't need a token.
 *
 * @param {{name: string, email: string, phone?: string, subject: string, message: string}} enquiry
 */
export function submitContactEnquiry(enquiry) {
  return httpClient(API_ENDPOINTS.CONTACT.BASE, { method: "POST", body: enquiry });
}

// ----- Admin -----

export function fetchContactEnquiries({ status, search, page = 0, size = 25 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (status) params.set("status", status);
  if (search) params.set("search", search);
  return httpClient(`${API_ENDPOINTS.CONTACT.BASE}?${params.toString()}`);
}

export function fetchContactEnquiry(enquiryId) {
  return httpClient(API_ENDPOINTS.CONTACT.BY_ID(enquiryId));
}

export function updateContactEnquiryStatus(enquiryId, status) {
  return httpClient(`${API_ENDPOINTS.CONTACT.STATUS(enquiryId)}?status=${status}`, { method: "PATCH" });
}

export function deleteContactEnquiry(enquiryId) {
  return httpClient(API_ENDPOINTS.CONTACT.BY_ID(enquiryId), { method: "DELETE" });
}

export function fetchNewEnquiryCount() {
  return httpClient(API_ENDPOINTS.CONTACT.COUNT);
}
