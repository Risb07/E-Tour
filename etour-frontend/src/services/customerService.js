import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

export function fetchMyProfile() {
  return httpClient(API_ENDPOINTS.CUSTOMER.ME);
}

export function updateMyProfile(payload) {
  return httpClient(API_ENDPOINTS.CUSTOMER.ME, { method: "PUT", body: payload });
}
