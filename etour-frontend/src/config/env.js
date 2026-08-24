// Central place for environment-driven config. Never read import.meta.env
// anywhere else in the app - if the env var name changes, this is the only
// file that should need to change.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
