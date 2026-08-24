/**
 * Generic elevated surface used for every card-shaped thing in the app
 * (category cards, tour cards, dashboard stat cards, etc). Deliberately
 * unopinionated about content - it only owns the shell (radius, shadow,
 * hover lift, padding), never layout of what's inside.
 */
export default function Card({ children, hoverLift = false, padded = true, onClick, className = "" }) {
  const interactive = Boolean(onClick) || hoverLift;

  return (
    <div
      onClick={onClick}
      className={[
        "rounded-card bg-white shadow-card overflow-hidden",
        padded && "p-5",
        interactive && "transition-all duration-300 hover:-translate-y-1 hover:shadow-lifted cursor-pointer",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </div>
  );
}
