import { useId, useState } from "react";
import { Eye, EyeOff } from "lucide-react";

/**
 * Labeled input with optional leading icon and, for type="password", a
 * built-in visibility toggle. Every form in the app should use this
 * instead of a raw <input>.
 */
export default function Input({
  label,
  type = "text",
  value,
  onChange,
  onBlur,
  error,
  placeholder,
  autoComplete,
  required = false,
  icon: Icon,
  disabled = false,
  // Passed straight through to the native input - used by date range and
  // numeric filters to stop invalid values being entered at all.
  min,
  max,
  step,
}) {
  const id = useId();
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === "password";
  const resolvedType = isPassword && showPassword ? "text" : type;

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-ink-700">
        {label}
        {required && <span className="text-amber-600"> *</span>}
      </label>

      <div className="relative">
        {Icon && (
          <Icon
            className="pointer-events-none absolute left-3.5 top-1/2 h-4.5 w-4.5 -translate-y-1/2 text-ink-300"
            aria-hidden="true"
          />
        )}

        <input
          id={id}
          type={resolvedType}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          autoComplete={autoComplete}
          disabled={disabled}
          min={min}
          max={max}
          step={step}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className={[
            "w-full rounded-xl border bg-white py-2.5 text-sm text-ink-900 placeholder:text-ink-300",
            Icon ? "pl-10" : "pl-3.5",
            isPassword ? "pr-11" : "pr-3.5",
            "transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
            disabled && "cursor-not-allowed bg-ink-50 text-ink-400",
            error ? "border-red-400" : "border-ink-200 focus:border-amber-400",
          ].join(" ")}
        />

        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword((current) => !current)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-700"
            aria-label={showPassword ? "Hide password" : "Show password"}
            tabIndex={-1}
          >
            {showPassword ? <EyeOff className="h-4.5 w-4.5" /> : <Eye className="h-4.5 w-4.5" />}
          </button>
        )}
      </div>

      {error && (
        <p id={`${id}-error`} className="text-xs font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}
