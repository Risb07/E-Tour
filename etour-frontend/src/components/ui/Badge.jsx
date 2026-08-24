/**
 * Small status/label pill - discount badges, availability badges, booking
 * status, tour codes. One component, one set of variants, used everywhere
 * a colored label is needed instead of ad-hoc styled <span>s.
 */
const VARIANTS = {
  success: "bg-emerald-50 text-emerald-700",
  warning: "bg-amber-50 text-amber-700",
  danger: "bg-red-50 text-red-700",
  info: "bg-ink-100 text-ink-700",
  accent: "bg-amber-500 text-ink-900",
};

export default function Badge({ children, variant = "info", icon: Icon, className = "" }) {
  return (
    <span
      className={[
        "inline-flex items-center gap-1 rounded-pill px-2.5 py-1 text-xs font-semibold",
        VARIANTS[variant],
        className,
      ].join(" ")}
    >
      {Icon && <Icon className="h-3.5 w-3.5" strokeWidth={2.5} aria-hidden="true" />}
      {children}
    </span>
  );
}
