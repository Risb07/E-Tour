/**
 * The single button in the app. Every variant, size and state lives here so
 * spacing, radius, focus rings and motion stay identical everywhere.
 *
 * `icon` takes a Lucide component (not a rendered element) so size and stroke
 * width are set centrally rather than at each call site.
 */
export default function Button({
  children,
  type = "button",
  variant = "primary",
  size = "md",
  icon: Icon,
  iconPosition = "left",
  isLoading = false,
  disabled = false,
  onClick,
  className = "",
  fullWidth = false,
  ...rest
}) {
  const base = [
    "inline-flex items-center justify-center gap-2 rounded-pill font-semibold",
    "transition-all duration-200 ease-out",
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2",
    "disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none",
    // Subtle press feedback. Disabled buttons stay put so the UI doesn't
    // imply an action happened.
    "active:scale-[0.98] disabled:active:scale-100",
    "whitespace-nowrap",
  ].join(" ");

  const sizes = {
    sm: "px-4 py-2 text-xs",
    md: "px-5 py-2.5 text-sm",
    lg: "px-6 py-3.5 text-base",
  };

  const variants = {
    primary:
      "bg-amber-500 text-ink-900 shadow-soft hover:bg-amber-400 hover:shadow-card focus-visible:ring-amber-500",
    secondary:
      "bg-ink-900 text-white shadow-soft hover:bg-ink-800 hover:shadow-card focus-visible:ring-ink-500",
    outline:
      "border-2 border-ink-200 bg-white text-ink-800 hover:border-amber-400 hover:text-amber-600 focus-visible:ring-amber-400",
    ghost: "bg-transparent text-ink-600 hover:bg-ink-100 hover:text-ink-900 focus-visible:ring-ink-300",
    danger: "bg-red-600 text-white shadow-soft hover:bg-red-500 focus-visible:ring-red-500",
    success:
      "bg-emerald-600 text-white shadow-soft hover:bg-emerald-500 focus-visible:ring-emerald-500",
  };

  const isDisabled = disabled || isLoading;

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={isDisabled}
      aria-busy={isLoading || undefined}
      className={[base, sizes[size], variants[variant], fullWidth && "w-full", className]
        .filter(Boolean)
        .join(" ")}
      {...rest}
    >
      {isLoading ? (
        <span
          className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
          aria-hidden="true"
        />
      ) : (
        Icon && iconPosition === "left" && <Icon className="h-4 w-4 shrink-0" strokeWidth={2.25} aria-hidden="true" />
      )}
      {children}
      {!isLoading && Icon && iconPosition === "right" && (
        <Icon className="h-4 w-4 shrink-0" strokeWidth={2.25} aria-hidden="true" />
      )}
    </button>
  );
}
