// Small, pure, testable validation functions. Every validator returns an
// error string, or an empty string when the value is valid - this lets
// forms do `error={validateEmail(email)}` without extra boolean plumbing.

export function validateEmail(value) {
  if (!value.trim()) return "Email is required";
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(value)) return "Enter a valid email address";
  return "";
}

export function validatePassword(value) {
  if (!value) return "Password is required";
  if (value.length < 8) return "Password must be at least 8 characters";
  return "";
}

export function validateRequired(value, fieldLabel) {
  if (!value || !value.trim()) return `${fieldLabel} is required`;
  return "";
}

export function validatePhone(value) {
  if (!value.trim()) return "Phone number is required";
  if (!/^[0-9]{10}$/.test(value)) return "Enter a valid 10-digit phone number";
  return "";
}
