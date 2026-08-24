import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function fetchCategories() {
  return httpClient(API_ENDPOINTS.CATEGORIES.BASE);
}

export function fetchCategoryById(id) {
  return httpClient(API_ENDPOINTS.CATEGORIES.BY_ID(id));
}

export function createCategory(category) {
  return httpClient(API_ENDPOINTS.CATEGORIES.BASE, { method: "POST", body: category });
}

export function updateCategory(id, category) {
  return httpClient(API_ENDPOINTS.CATEGORIES.BY_ID(id), { method: "PUT", body: category });
}

export function deleteCategory(id) {
  return httpClient(API_ENDPOINTS.CATEGORIES.BY_ID(id), { method: "DELETE" });
}
