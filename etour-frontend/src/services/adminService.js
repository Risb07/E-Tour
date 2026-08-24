import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";
import { API_BASE_URL } from "../config/env";
import { loadSession } from "../utils/storage";

export function fetchDashboardStats() {
  return httpClient(API_ENDPOINTS.ADMIN.DASHBOARD);
}

export function uploadTourExcel(file) {
  const formData = new FormData();
  formData.append("file", file);
  return httpClient(API_ENDPOINTS.ADMIN.EXCEL_UPLOAD, { method: "POST", body: formData, isFormData: true });
}

/**
 * The template endpoint returns raw .xlsx bytes, not JSON, so it can't go
 * through httpClient. Fetches it as a Blob and triggers a browser download.
 */
export async function downloadTourExcelTemplate() {
  const session = loadSession();
  const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ADMIN.EXCEL_TEMPLATE}`, {
    headers: session?.token ? { Authorization: `Bearer ${session.token}` } : {},
  });
  if (!response.ok) {
    throw new Error("Could not download the template.");
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "etour-tour-upload-template.xlsx";
  document.body.appendChild(link);
  link.click();
  link.remove();
  // Release the object URL once the download has been handed to the browser.
  URL.revokeObjectURL(url);
}
