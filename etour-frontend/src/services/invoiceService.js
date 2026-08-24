import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";
import { API_BASE_URL } from "../config/env";
import { loadSession } from "../utils/storage";

export function fetchMyInvoices() {
  return httpClient(API_ENDPOINTS.INVOICES.ME);
}

export function fetchInvoiceForBooking(bookingId) {
  return httpClient(API_ENDPOINTS.INVOICES.BY_BOOKING(bookingId));
}

/**
 * The receipt endpoint returns raw PDF bytes (not JSON), so it can't go
 * through httpClient - fetch it directly and hand back a Blob the caller can
 * open in a new tab (URL.createObjectURL) or download.
 */
export async function fetchReceiptPdfBlob(bookingId) {
  const session = loadSession();
  const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.INVOICES.RECEIPT(bookingId)}`, {
    headers: session?.token ? { Authorization: `Bearer ${session.token}` } : {},
  });
  if (!response.ok) {
    throw new Error("Could not load the receipt PDF.");
  }
  return response.blob();
}
