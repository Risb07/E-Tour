import { Link } from "react-router-dom";
import { ChevronRight, Home } from "lucide-react";
import { ROUTE_PATHS } from "../../constants/routes";

/** @param {{items: {label: string, to?: string}[]}} props - last item has no `to` (current page) */
export default function Breadcrumb({ items }) {
  return (
    <nav className="flex items-center gap-1.5 text-xs text-ink-500" aria-label="Breadcrumb">
      <Link to={ROUTE_PATHS.HOME} className="flex items-center hover:text-amber-600">
        <Home className="h-3.5 w-3.5" />
      </Link>
      {items.map((item) => (
        <span key={item.label} className="flex items-center gap-1.5">
          <ChevronRight className="h-3 w-3 text-ink-300" />
          {item.to ? (
            <Link to={item.to} className="hover:text-amber-600">
              {item.label}
            </Link>
          ) : (
            <span className="font-medium text-ink-700">{item.label}</span>
          )}
        </span>
      ))}
    </nav>
  );
}
