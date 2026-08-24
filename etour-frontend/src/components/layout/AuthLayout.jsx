import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import { ROUTE_PATHS } from "../../constants/routes";

/**
 * Split layout: brand panel on the left (desktop only), form card on the
 * right. Mobile collapses to just the form - the brand panel is nice-to-have,
 * not load-bearing content.
 */
export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-2 bg-ink-50">
      <div className="hidden lg:flex flex-col justify-between bg-ink-900 text-ink-50 p-12 relative overflow-hidden">
        <div
          className="absolute inset-0 opacity-40"
          style={{
            background:
              "radial-gradient(circle at 20% 20%, rgba(255,164,31,0.25), transparent 45%), radial-gradient(circle at 80% 70%, rgba(99,102,241,0.2), transparent 50%)",
          }}
          aria-hidden="true"
        />
        <Link to={ROUTE_PATHS.HOME} className="relative flex items-center gap-2 font-display text-2xl font-bold tracking-tight">
          <Compass className="h-6 w-6 text-amber-400" strokeWidth={2} />
          eTour
        </Link>
        <div className="relative">
          <p className="font-display text-3xl font-bold leading-tight max-w-md">
            Every great journey starts with one honest plan.
          </p>
          <p className="mt-4 text-ink-300 max-w-sm">
            Browse curated tours, book with confidence, and manage every trip detail in one place.
          </p>
        </div>
        <p className="relative text-xs text-ink-400">© {new Date().getFullYear()} eTour Management System</p>
      </div>

      <div className="flex items-center justify-center p-6 sm:p-10">
        <div className="w-full max-w-sm animate-slide-up">
          <Link to={ROUTE_PATHS.HOME} className="lg:hidden flex items-center gap-2 font-display text-xl font-bold text-ink-900">
            <Compass className="h-5 w-5 text-amber-500" strokeWidth={2} />
            eTour
          </Link>
          <h1 className="mt-6 font-display text-2xl font-bold text-ink-900">{title}</h1>
          {subtitle && <p className="mt-1.5 text-sm text-ink-500">{subtitle}</p>}
          <div className="mt-8">{children}</div>
        </div>
      </div>
    </div>
  );
}
