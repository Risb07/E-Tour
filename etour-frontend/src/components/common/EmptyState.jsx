import Button from "./Button";

/**
 * Meaningful empty state - icon + message + up to two actions. Used for
 * "no results", "no bookings yet", "wishlist is empty", etc. Never leave
 * a page blank; render this instead.
 */
export default function EmptyState({
  icon: Icon,
  title,
  description,
  primaryAction,
  secondaryAction,
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 px-6 text-center animate-fade-in">
      {Icon && (
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-ink-100 text-ink-400">
          <Icon className="h-7 w-7" strokeWidth={1.75} aria-hidden="true" />
        </div>
      )}
      <h3 className="font-display text-lg font-bold text-ink-900">{title}</h3>
      {description && <p className="max-w-sm text-sm text-ink-500">{description}</p>}
      {(primaryAction || secondaryAction) && (
        <div className="mt-2 flex flex-wrap items-center justify-center gap-3">
          {primaryAction && (
            <Button onClick={primaryAction.onClick} icon={primaryAction.icon}>
              {primaryAction.label}
            </Button>
          )}
          {secondaryAction && (
            <Button variant="ghost" onClick={secondaryAction.onClick}>
              {secondaryAction.label}
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
