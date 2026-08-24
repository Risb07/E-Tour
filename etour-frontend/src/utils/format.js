// Formatting helpers - kept in one place so currency/date presentation is
// consistent everywhere (booking summary, invoices, tour cards, dashboard).
export function formatCurrency(amount) {
  if (amount === null || amount === undefined) return "-";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatDate(dateValue, options = { day: "numeric", month: "short", year: "numeric" }) {
  if (!dateValue) return "-";
  return new Intl.DateTimeFormat("en-IN", options).format(new Date(dateValue));
}

export function formatDateTime(dateValue) {
  if (!dateValue) return "-";
  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(dateValue));
}
