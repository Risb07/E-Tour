// Same shape as storage.js (session token/user), but for the in-progress
// booking flow - so a refresh mid-flow doesn't bounce the user back to Home
// and silently drop everything they'd already picked.
const BOOKING_FLOW_KEY = "etour_booking_flow";

export function saveBookingFlow(state) {
  sessionStorage.setItem(BOOKING_FLOW_KEY, JSON.stringify(state));
}

export function loadBookingFlow() {
  const raw = sessionStorage.getItem(BOOKING_FLOW_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function clearBookingFlow() {
  sessionStorage.removeItem(BOOKING_FLOW_KEY);
}
