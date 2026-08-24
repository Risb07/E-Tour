/**
 * Card helpers for the payment form: brand detection, display formatting and
 * validation (Luhn, expiry, CVV).
 *
 * These are a UX convenience only. The server re-validates every one of these
 * rules before charging - a client check can be bypassed entirely, so it is
 * never the only gate. See CardDetails.java.
 */

/** Digits only, stripping the spaces the input adds for readability. */
export function digitsOnly(value) {
  return (value || "").replace(/\D/g, "");
}

/** Brand from the IIN/BIN prefix. Presentational - the gateway is the authority. */
export function detectBrand(cardNumber) {
  const n = digitsOnly(cardNumber);
  if (!n) return null;
  if (/^4/.test(n)) return "VISA";
  if (/^5[1-5]/.test(n) || /^2[2-7]/.test(n)) return "MASTERCARD";
  if (/^3[47]/.test(n)) return "AMEX";
  if (/^(60|65|81|82|508)/.test(n)) return "RUPAY";
  if (/^35/.test(n)) return "JCB";
  if (/^6/.test(n)) return "DISCOVER";
  return null;
}

/** AMEX is 15 digits in 4-6-5 groups; everything else is 4-digit groups. */
export function formatCardNumber(value) {
  const n = digitsOnly(value).slice(0, 19);
  if (detectBrand(n) === "AMEX") {
    return [n.slice(0, 4), n.slice(4, 10), n.slice(10, 15)].filter(Boolean).join(" ");
  }
  return n.replace(/(.{4})/g, "$1 ").trim();
}

/** Expected digit count for the brand (used for both length and CVV rules). */
export function expectedLength(cardNumber) {
  return detectBrand(cardNumber) === "AMEX" ? 15 : 16;
}

export function expectedCvvLength(cardNumber) {
  return detectBrand(cardNumber) === "AMEX" ? 4 : 3;
}

/**
 * Luhn (mod-10) check digit validation - catches most mistyped card numbers
 * before a request is ever made.
 */
export function passesLuhn(cardNumber) {
  const n = digitsOnly(cardNumber);
  if (n.length < 12) return false;

  let sum = 0;
  let doubleIt = false;
  for (let i = n.length - 1; i >= 0; i--) {
    let digit = Number(n[i]);
    if (doubleIt) {
      digit *= 2;
      if (digit > 9) digit -= 9;
    }
    sum += digit;
    doubleIt = !doubleIt;
  }
  return sum % 10 === 0;
}

/** A card is valid through the LAST day of its expiry month. */
export function isExpiryValid(month, year) {
  const m = Number(month);
  const y = Number(year);
  if (!m || !y || m < 1 || m > 12) return false;

  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;

  if (y < currentYear) return false;
  if (y === currentYear && m < currentMonth) return false;
  // Guard against obvious typos far in the future.
  if (y > currentYear + 25) return false;
  return true;
}

/** Years offered in the expiry dropdown - current year plus the next 15. */
export function expiryYearOptions(count = 16) {
  const start = new Date().getFullYear();
  return Array.from({ length: count }, (_, i) => start + i);
}

export const MONTH_OPTIONS = Array.from({ length: 12 }, (_, i) => {
  const value = i + 1;
  return { value, label: String(value).padStart(2, "0") };
});

/**
 * Validates the whole form and returns a field->message map.
 * An empty object means the form is valid.
 */
export function validateCardForm(form) {
  const errors = {};
  const number = digitsOnly(form.cardNumber);

  if (!number) {
    errors.cardNumber = "Card number is required";
  } else if (number.length !== expectedLength(number)) {
    errors.cardNumber = `Card number must be ${expectedLength(number)} digits`;
  } else if (!passesLuhn(number)) {
    errors.cardNumber = "Please check the card number";
  }

  if (!form.cardHolderName?.trim()) {
    errors.cardHolderName = "Cardholder name is required";
  } else if (form.cardHolderName.trim().length < 2) {
    errors.cardHolderName = "Enter the name as printed on the card";
  }

  if (!form.expiryMonth || !form.expiryYear) {
    errors.expiry = "Expiry date is required";
  } else if (!isExpiryValid(form.expiryMonth, form.expiryYear)) {
    errors.expiry = "This card has expired";
  }

  const cvvLength = expectedCvvLength(number);
  if (!form.cvv) {
    errors.cvv = "CVV is required";
  } else if (digitsOnly(form.cvv).length !== cvvLength) {
    errors.cvv = `CVV must be ${cvvLength} digits`;
  }

  return errors;
}
