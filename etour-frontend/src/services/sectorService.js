import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

// BRD 3.2/3.3/3.4 - Home > Sector > Sub-Sector > Product drill-down.
export function fetchSectors() {
  return httpClient(API_ENDPOINTS.SECTORS.BASE);
}

export function fetchSectorById(id) {
  return httpClient(API_ENDPOINTS.SECTORS.BY_ID(id));
}

export function fetchSubSectorsForSector(sectorId) {
  return httpClient(API_ENDPOINTS.SECTORS.SUB_SECTORS(sectorId));
}

export function fetchSubSectorById(id) {
  return httpClient(API_ENDPOINTS.SUB_SECTORS.BY_ID(id));
}

export function fetchProductsForSubSector(subSectorId) {
  return httpClient(API_ENDPOINTS.SUB_SECTORS.PRODUCTS(subSectorId));
}

export function fetchProductById(id) {
  return httpClient(API_ENDPOINTS.PRODUCTS.BY_ID(id));
}

// ----- Admin CRUD (lists include inactive records so admins can re-enable) -----

export function fetchAllSectors(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.SECTORS.BASE}?includeInactive=${includeInactive}`);
}

export function createSector(sector) {
  return httpClient(API_ENDPOINTS.SECTORS.BASE, { method: "POST", body: sector });
}

export function updateSector(id, sector) {
  return httpClient(API_ENDPOINTS.SECTORS.BY_ID(id), { method: "PUT", body: sector });
}

export function deleteSector(id) {
  return httpClient(API_ENDPOINTS.SECTORS.BY_ID(id), { method: "DELETE" });
}

export function fetchAllSubSectors(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.SUB_SECTORS.BASE}?includeInactive=${includeInactive}`);
}

export function createSubSector(subSector) {
  return httpClient(API_ENDPOINTS.SUB_SECTORS.BASE, { method: "POST", body: subSector });
}

export function updateSubSector(id, subSector) {
  return httpClient(API_ENDPOINTS.SUB_SECTORS.BY_ID(id), { method: "PUT", body: subSector });
}

export function deleteSubSector(id) {
  return httpClient(API_ENDPOINTS.SUB_SECTORS.BY_ID(id), { method: "DELETE" });
}

export function fetchAllProducts(includeInactive = true) {
  return httpClient(`${API_ENDPOINTS.PRODUCTS.BASE}?includeInactive=${includeInactive}`);
}

export function createProduct(product) {
  return httpClient(API_ENDPOINTS.PRODUCTS.BASE, { method: "POST", body: product });
}

export function updateProduct(id, product) {
  return httpClient(API_ENDPOINTS.PRODUCTS.BY_ID(id), { method: "PUT", body: product });
}

export function deleteProduct(id) {
  return httpClient(API_ENDPOINTS.PRODUCTS.BY_ID(id), { method: "DELETE" });
}
