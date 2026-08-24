import { NavLink, Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  CalendarRange,
  UploadCloud,
  Compass,
  CalendarDays,
  Plane,
  Tags,
  CalendarClock,
  Wallet,
  Images,
  Megaphone,
  AlignLeft,
  Menu,
  BedDouble,
  PlusCircle,
  Utensils,
  Info,
  Star,
  Send,
} from "lucide-react";
import { ROUTE_PATHS } from "../../constants/routes";

const NAV_GROUPS = [
  {
    label: "Overview",
    items: [{ to: ROUTE_PATHS.ADMIN_DASHBOARD, label: "Dashboard", icon: LayoutDashboard, end: true }],
  },
  {
    label: "Tours",
    items: [
      { to: ROUTE_PATHS.ADMIN_TOURS, label: "Tours", icon: Plane },
      { to: ROUTE_PATHS.ADMIN_CATEGORIES, label: "Categories", icon: Tags },
      { to: ROUTE_PATHS.ADMIN_SCHEDULES, label: "Schedules", icon: CalendarClock },
      { to: ROUTE_PATHS.ADMIN_TOUR_COSTS, label: "Tour Costs", icon: Wallet },
      { to: ROUTE_PATHS.ADMIN_ROOM_CHARGES, label: "Room Charges", icon: BedDouble },
      { to: ROUTE_PATHS.ADMIN_ADDONS, label: "Add-ons", icon: PlusCircle },
      { to: ROUTE_PATHS.ADMIN_MEDIA, label: "Media & Map", icon: Images },
      { to: ROUTE_PATHS.ADMIN_ITINERARY, label: "Itinerary", icon: CalendarDays },
      { to: ROUTE_PATHS.ADMIN_STAY_MEALS, label: "Stay & Meals", icon: Utensils },
      { to: ROUTE_PATHS.ADMIN_TOUR_CONTENT, label: "Good to Know", icon: Info },
      { to: ROUTE_PATHS.ADMIN_REVIEWS, label: "Reviews", icon: Star },
    ],
  },
  {
    label: "Catalog",
    items: [
      // "Content" is deliberately not listed: the page it pointed at is not
      // editable, so the tab only led somewhere that could not be used. The
      // route and the page are untouched - /admin/content still resolves for
      // anyone with the direct link - this removes the entry from the sidebar
      // and nothing else.
      { to: ROUTE_PATHS.ADMIN_AD_BANNERS, label: "Ad Banners", icon: Megaphone },
      { to: ROUTE_PATHS.ADMIN_CRAWLING_TEXT, label: "Crawling Text", icon: AlignLeft },
      { to: ROUTE_PATHS.ADMIN_NAV_MENU, label: "Nav Menu", icon: Menu },
    ],
  },
  {
    label: "Operations",
    items: [
      { to: ROUTE_PATHS.ADMIN_BOOKINGS, label: "Bookings", icon: CalendarRange },
      { to: ROUTE_PATHS.ADMIN_EXCEL_UPLOAD, label: "Bulk Tour Upload", icon: UploadCloud },
      // Served by the notification microservice, not by the backend.
      { to: ROUTE_PATHS.ADMIN_NOTIFICATIONS, label: "Notifications", icon: Send },
    ],
  },
];

export default function AdminLayout() {
  return (
    <div className="flex min-h-screen bg-ink-50">
      <aside className="hidden w-64 shrink-0 border-r border-ink-100 bg-white p-5 lg:block">
        <div className="flex items-center gap-2 font-display text-lg font-bold text-ink-900">
          <Compass className="h-6 w-6 text-amber-500" /> eTour Admin
        </div>
        <nav className="mt-6 flex flex-col gap-5">
          {NAV_GROUPS.map((group) => (
            <div key={group.label}>
              <p className="px-3 text-[11px] font-semibold uppercase tracking-wider text-ink-400">
                {group.label}
              </p>
              <div className="mt-1.5 flex flex-col gap-0.5">
                {group.items.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.end}
                    className={({ isActive }) =>
                      [
                        "flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm font-semibold transition-colors",
                        isActive
                          ? "bg-amber-50 text-amber-700"
                          : "text-ink-500 hover:bg-ink-50 hover:text-ink-800",
                      ].join(" ")
                    }
                  >
                    <item.icon className="h-4 w-4" /> {item.label}
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </nav>
      </aside>

      <main className="flex-1 p-5 sm:p-8">
        <Outlet />
      </main>
    </div>
  );
}
