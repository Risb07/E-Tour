import { CreditCard, Lock } from "lucide-react";
import {
  formatCardNumber,
  detectBrand,
  digitsOnly,
  expectedCvvLength,
  MONTH_OPTIONS,
  expiryYearOptions,
} from "../../utils/card";
import Input from "../common/Input";

const YEAR_OPTIONS = expiryYearOptions();

/**
 * Card entry form. Controlled by the parent so the raw values live in exactly
 * one place and are cleared as soon as the request completes - card data is
 * never written to storage, context or logs.
 */
export default function CardForm({ form, errors, onChange, disabled }) {
  const brand = detectBrand(form.cardNumber);
  const cvvLength = expectedCvvLength(form.cardNumber);

  function update(field, value) {
    onChange({ ...form, [field]: value });
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <label htmlFor="card-number" className="text-sm font-medium text-ink-700">
          Card number <span className="text-amber-600">*</span>
        </label>
        <div className="relative mt-1.5">
          <CreditCard className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-300" />
          <input
            id="card-number"
            type="text"
            inputMode="numeric"
            autoComplete="cc-number"
            // Never let a browser/password manager treat this as a saveable field
            // beyond the standard autofill contract.
            placeholder="1234 5678 9012 3456"
            value={formatCardNumber(form.cardNumber)}
            onChange={(e) => update("cardNumber", digitsOnly(e.target.value).slice(0, 19))}
            disabled={disabled}
            aria-invalid={Boolean(errors.cardNumber)}
            aria-describedby={errors.cardNumber ? "card-number-error" : undefined}
            className={[
              "w-full rounded-xl border bg-white py-2.5 pl-10 pr-20 text-sm tracking-wide text-ink-900",
              "placeholder:text-ink-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
              disabled && "cursor-not-allowed bg-ink-50",
              errors.cardNumber ? "border-red-400" : "border-ink-200 focus:border-amber-400",
            ].filter(Boolean).join(" ")}
          />
          {brand && (
            <span className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md bg-ink-100 px-2 py-1 text-[10px] font-bold tracking-wide text-ink-600">
              {brand}
            </span>
          )}
        </div>
        {errors.cardNumber && (
          <p id="card-number-error" className="mt-1 text-xs font-medium text-red-600">
            {errors.cardNumber}
          </p>
        )}
      </div>

      <Input
        label="Cardholder name"
        required
        autoComplete="cc-name"
        placeholder="As printed on the card"
        value={form.cardHolderName}
        onChange={(e) => update("cardHolderName", e.target.value)}
        error={errors.cardHolderName}
        disabled={disabled}
      />

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <div className="col-span-2 sm:col-span-2">
          <span className="text-sm font-medium text-ink-700">
            Expiry <span className="text-amber-600">*</span>
          </span>
          <div className="mt-1.5 grid grid-cols-2 gap-3">
            <select
              aria-label="Expiry month"
              value={form.expiryMonth}
              onChange={(e) => update("expiryMonth", e.target.value)}
              disabled={disabled}
              className={selectClass(errors.expiry, disabled)}
            >
              <option value="">MM</option>
              {MONTH_OPTIONS.map((m) => (
                <option key={m.value} value={m.value}>
                  {m.label}
                </option>
              ))}
            </select>
            <select
              aria-label="Expiry year"
              value={form.expiryYear}
              onChange={(e) => update("expiryYear", e.target.value)}
              disabled={disabled}
              className={selectClass(errors.expiry, disabled)}
            >
              <option value="">YYYY</option>
              {YEAR_OPTIONS.map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </div>
          {errors.expiry && <p className="mt-1 text-xs font-medium text-red-600">{errors.expiry}</p>}
        </div>

        <Input
          label="CVV"
          required
          type="password"
          autoComplete="cc-csc"
          placeholder={"•".repeat(cvvLength)}
          value={form.cvv}
          onChange={(e) => update("cvv", digitsOnly(e.target.value).slice(0, cvvLength))}
          error={errors.cvv}
          disabled={disabled}
        />
      </div>

      <label className="flex items-start gap-2 text-sm text-ink-700">
        <input
          type="checkbox"
          checked={Boolean(form.saveCard)}
          onChange={(e) => update("saveCard", e.target.checked)}
          disabled={disabled}
          className="mt-0.5 h-4 w-4 rounded border-ink-300 text-amber-500 focus:ring-amber-400"
        />
        <span>
          Save this card for next time
          <span className="block text-xs text-ink-400">
            Only the card brand and last four digits are kept — never the full number.
          </span>
        </span>
      </label>

      <p className="flex items-center gap-1.5 rounded-xl bg-ink-50 px-3 py-2 text-xs text-ink-500">
        <Lock className="h-3.5 w-3.5 shrink-0" />
        Your card details are used for this payment only and are not stored.
      </p>
    </div>
  );
}

function selectClass(error, disabled) {
  return [
    "w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-ink-900",
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
    disabled && "cursor-not-allowed bg-ink-50",
    error ? "border-red-400" : "border-ink-200 focus:border-amber-400",
  ]
    .filter(Boolean)
    .join(" ");
}
