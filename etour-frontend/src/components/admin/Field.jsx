import { useId } from "react";

/**
 * Labeled form control for admin forms - text/number/date/textarea/select/
 * checkbox in one component so every admin page renders fields the same way.
 */
export default function Field({
  label,
  type = "text",
  value,
  onChange,
  options = [],
  required = false,
  error,
  placeholder,
  hint,
  rows = 3,
  min,
  max,
  step,
  disabled = false,
}) {
  const id = useId();
  const errorId = `${id}-error`;
  const hintId = `${id}-hint`;

  const inputClass = [
    "w-full rounded-xl border bg-white py-2.5 text-sm text-ink-900 placeholder:text-ink-300",
    "pl-3.5 pr-3.5 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
    disabled && "cursor-not-allowed bg-ink-50 text-ink-400",
    error ? "border-red-400" : "border-ink-200 focus:border-amber-400",
  ].join(" ");

  const isCheckbox = type === "checkbox";

  if (isCheckbox) {
    return (
      <div className="flex flex-col gap-1.5">
        <label className="flex items-center gap-2.5 text-sm font-medium text-ink-700">
          <input
            id={id}
            type="checkbox"
            checked={Boolean(value)}
            onChange={(e) => onChange(e.target.checked)}
            disabled={disabled}
            className="h-4 w-4 rounded border-ink-300 text-amber-500 focus:ring-amber-400"
          />
          {label}
        </label>
        {error && (
          <p id={errorId} className="text-xs font-medium text-red-600">
            {error}
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-ink-700">
        {label}
        {required && <span className="text-amber-600"> *</span>}
      </label>

      {type === "textarea" ? (
        <textarea
          id={id}
          rows={rows}
          value={value ?? ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : hint ? hintId : undefined}
          className={inputClass}
        />
      ) : type === "select" ? (
        <select
          id={id}
          value={value ?? ""}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : hint ? hintId : undefined}
          className={`${inputClass} cursor-pointer`}
        >
          <option value="">{placeholder || "Select..."}</option>
          {options.map((opt) => {
            const optionValue = typeof opt === "object" ? opt.value : opt;
            const optionLabel = typeof opt === "object" ? opt.label : opt;
            return (
              <option key={optionValue} value={optionValue}>
                {optionLabel}
              </option>
            );
          })}
        </select>
      ) : (
        <input
          id={id}
          type={type}
          value={value ?? ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          min={min}
          max={max}
          step={step}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : hint ? hintId : undefined}
          className={inputClass}
        />
      )}

      {hint && !error && (
        <p id={hintId} className="text-xs text-ink-400">
          {hint}
        </p>
      )}
      {error && (
        <p id={errorId} className="text-xs font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}
