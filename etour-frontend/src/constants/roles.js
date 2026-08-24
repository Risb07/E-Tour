// Mirrors the backend's roles table role_name values exactly.
// If the backend adds a role, add it here - nowhere else should hardcode
// the string "ADMIN" or "CUSTOMER".
export const ROLES = {
  ADMIN: "ADMIN",
  CUSTOMER: "CUSTOMER",
};
