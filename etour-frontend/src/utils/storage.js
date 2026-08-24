// Thin wrapper around sessionStorage so the rest of the app never touches
// the Web Storage API (or a raw string key) directly. If we ever needed to
// swap storage mechanisms, this is the only file that changes.
const TOKEN_KEY = "etour_token";
const USER_KEY = "etour_user";

export function saveSession(token, user) {
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function loadSession() {
  const token = sessionStorage.getItem(TOKEN_KEY);
  const rawUser = sessionStorage.getItem(USER_KEY);

  if (!token || !rawUser) return null;

  try {
    return { token, user: JSON.parse(rawUser) };
  } catch {
    return null;
  }
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}
