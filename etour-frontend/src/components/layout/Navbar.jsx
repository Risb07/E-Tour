import { useEffect, useRef, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import {
  Compass,
  Menu,
  X,
  User,
  LogOut,
  Search,
  ShoppingCart,
  ChevronDown,
  Heart,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useContent } from "../../hooks/useContent";
import { ROUTE_PATHS } from "../../constants/routes";
import { ROLES } from "../../constants/roles";
import { fetchNavMenu } from "../../services/navMenuService";
import Button from "../common/Button";

/**
 * Site header.
 *
 * Public navigation is 100% database-driven (nav_menu_item). It previously
 * ALSO rendered hardcoded "Home"/"Explore Tours" links, which duplicated the
 * same entries coming from the database - that is what made the nav look
 * repeated. Those are gone.
 *
 * The only links still defined in code are the role-gated app destinations
 * (My Bookings / Admin). Those are not content: they depend on who is signed
 * in and map to protected routes, so they can't live in a CMS table.
 */
export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { text } = useContent();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [navMenu, setNavMenu] = useState([]);
  const navigate = useNavigate();
  const location = useLocation();
  const menuRef = useRef(null);

  useEffect(() => {
    fetchNavMenu()
      .then((data) => setNavMenu(Array.isArray(data) ? data : []))
      .catch(() => setNavMenu([]));
  }, []);

  // Close the drawer/dropdown whenever the route changes, otherwise the menu
  // stays open on top of the page the user just navigated to.
  useEffect(() => {
    setMobileOpen(false);
    setOpenMenuId(null);
  }, [location.pathname]);

  // Lock body scroll behind the mobile drawer.
  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);

  // Escape closes whichever layer is open; click-away closes dropdowns.
  useEffect(() => {
    function onKeyDown(e) {
      if (e.key !== "Escape") return;
      setOpenMenuId(null);
      setMobileOpen(false);
    }
    function onClickAway(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) setOpenMenuId(null);
    }
    document.addEventListener("keydown", onKeyDown);
    document.addEventListener("mousedown", onClickAway);
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.removeEventListener("mousedown", onClickAway);
    };
  }, []);

  function handleLogout() {
    logout();
    setMobileOpen(false);
    navigate(ROUTE_PATHS.HOME);
  }

  // Role-gated app links, appended after the database-driven ones.
  const roleLinks = [];
  if (isAuthenticated && user?.role === ROLES.CUSTOMER) {
    roleLinks.push({ label: "My Bookings", to: ROUTE_PATHS.CUSTOMER_DASHBOARD });
  }
  if (isAuthenticated && user?.role === ROLES.ADMIN) {
    roleLinks.push({ label: "Admin", to: ROUTE_PATHS.ADMIN_DASHBOARD });
  }

  const brand = text("site.brand") || "eTour";

  return (
    <header className="sticky top-0 z-40 border-b border-ink-100 bg-white/90 backdrop-blur supports-[backdrop-filter]:bg-white/80">
      <nav
        aria-label="Main"
        className="mx-auto flex h-16 max-w-7xl items-center gap-6 px-4 sm:px-6 lg:px-8"
      >
        <Link
          to={ROUTE_PATHS.HOME}
          className="flex shrink-0 items-center gap-2 rounded-lg font-display text-xl font-bold text-ink-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
        >
          <Compass className="h-6 w-6 text-amber-500" strokeWidth={2.25} aria-hidden="true" />
          {brand}
        </Link>

        {/* ---------- Desktop navigation (database-driven) ---------- */}
        <div ref={menuRef} className="hidden flex-1 items-center gap-1 md:flex">
          {navMenu.map((item) => {
            const hasChildren = item.children?.length > 0;
            const isOpen = openMenuId === item.navMenuItemId;

            if (!hasChildren) {
              return (
                <NavLink key={item.navMenuItemId} to={item.link} className={navLinkClass} end={item.link === "/"}>
                  {item.label}
                </NavLink>
              );
            }

            return (
              <div key={item.navMenuItemId} className="relative">
                <button
                  onClick={() => setOpenMenuId(isOpen ? null : item.navMenuItemId)}
                  aria-expanded={isOpen}
                  aria-haspopup="true"
                  className={`${navLinkBase} ${isOpen ? "bg-ink-50 text-ink-900" : "text-ink-600 hover:bg-ink-50 hover:text-ink-900"}`}
                >
                  {item.label}
                  <ChevronDown
                    className={`h-3.5 w-3.5 transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}
                    aria-hidden="true"
                  />
                </button>

                {isOpen && (
                  <div className="absolute left-0 top-full z-50 mt-1 flex min-w-[200px] flex-col gap-0.5 rounded-xl border border-ink-100 bg-white p-1.5 shadow-lifted animate-slide-up">
                    {item.children.map((child) => (
                      <Link
                        key={child.navMenuItemId}
                        to={child.link}
                        className="rounded-lg px-3 py-2 text-sm text-ink-700 transition-colors hover:bg-ink-50 hover:text-ink-900 focus:outline-none focus-visible:bg-ink-50"
                      >
                        {child.label}
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            );
          })}

          {roleLinks.map((link) => (
            <NavLink key={link.to} to={link.to} className={navLinkClass}>
              {link.label}
            </NavLink>
          ))}
        </div>

        {/* ---------- Desktop actions ---------- */}
        <div className="ml-auto hidden items-center gap-1 md:flex">
          <IconLink to={ROUTE_PATHS.SEARCH} label="Search tours" icon={Search} />
          {isAuthenticated && user?.role === ROLES.CUSTOMER && (
            <IconLink to={ROUTE_PATHS.WISHLIST} label="View wishlist" icon={Heart} hoverClass="hover:text-red-500" />
          )}
          {isAuthenticated && <IconLink to={ROUTE_PATHS.CART} label="View cart" icon={ShoppingCart} />}

          {isAuthenticated ? (
            <div className="ml-2 flex items-center gap-2">
              <Link
                to={user.role === ROLES.ADMIN ? ROUTE_PATHS.ADMIN_DASHBOARD : ROUTE_PATHS.PROFILE}
                className="flex items-center gap-2 rounded-pill bg-ink-50 px-3 py-1.5 text-sm font-semibold text-ink-800 transition-colors hover:bg-ink-100 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
              >
                <User className="h-4 w-4" aria-hidden="true" />
                <span className="max-w-[120px] truncate">{user.firstName}</span>
              </Link>
              <Button variant="ghost" size="sm" icon={LogOut} onClick={handleLogout}>
                Log out
              </Button>
            </div>
          ) : (
            <div className="ml-2 flex items-center gap-2">
              <Link to={ROUTE_PATHS.LOGIN}>
                <Button variant="ghost" size="sm">
                  Log in
                </Button>
              </Link>
              <Link to={ROUTE_PATHS.REGISTER}>
                <Button size="sm">Sign up</Button>
              </Link>
            </div>
          )}
        </div>

        <button
          className="ml-auto rounded-lg p-2 text-ink-700 transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 md:hidden"
          onClick={() => setMobileOpen(true)}
          aria-label="Open menu"
          aria-expanded={mobileOpen}
        >
          <Menu className="h-6 w-6" aria-hidden="true" />
        </button>
      </nav>

      {/* ---------- Mobile drawer ---------- */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <button
            className="absolute inset-0 bg-ink-900/50 animate-fade-in"
            onClick={() => setMobileOpen(false)}
            aria-label="Close menu"
            tabIndex={-1}
          />

          <div
            role="dialog"
            aria-modal="true"
            aria-label="Menu"
            className="absolute right-0 top-0 flex h-full w-[85%] max-w-sm flex-col bg-white shadow-lifted animate-slide-in-right"
          >
            <div className="flex h-16 shrink-0 items-center justify-between border-b border-ink-100 px-4">
              <span className="flex items-center gap-2 font-display text-lg font-bold text-ink-900">
                <Compass className="h-5 w-5 text-amber-500" aria-hidden="true" />
                {brand}
              </span>
              <button
                onClick={() => setMobileOpen(false)}
                aria-label="Close menu"
                className="rounded-lg p-2 text-ink-600 transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
              >
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>

            <div className="flex flex-1 flex-col gap-1 overflow-y-auto p-4">
              {navMenu.map((item) => (
                <div key={item.navMenuItemId}>
                  <NavLink to={item.link} className={mobileLinkClass} end={item.link === "/"}>
                    {item.label}
                  </NavLink>
                  {item.children?.length > 0 && (
                    <div className="ml-3 border-l border-ink-100 pl-3">
                      {item.children.map((child) => (
                        <NavLink key={child.navMenuItemId} to={child.link} className={mobileLinkClass}>
                          {child.label}
                        </NavLink>
                      ))}
                    </div>
                  )}
                </div>
              ))}

              {roleLinks.map((link) => (
                <NavLink key={link.to} to={link.to} className={mobileLinkClass}>
                  {link.label}
                </NavLink>
              ))}

              <div className="my-2 border-t border-ink-100" />

              <NavLink to={ROUTE_PATHS.SEARCH} className={mobileLinkClass}>
                Search tours
              </NavLink>
              {isAuthenticated && user?.role === ROLES.CUSTOMER && (
                <NavLink to={ROUTE_PATHS.WISHLIST} className={mobileLinkClass}>
                  Wishlist
                </NavLink>
              )}
              {isAuthenticated && (
                <NavLink to={ROUTE_PATHS.CART} className={mobileLinkClass}>
                  Cart
                </NavLink>
              )}
              {isAuthenticated && (
                <NavLink to={ROUTE_PATHS.PROFILE} className={mobileLinkClass}>
                  Profile
                </NavLink>
              )}
            </div>

            <div className="shrink-0 border-t border-ink-100 p-4">
              {isAuthenticated ? (
                <Button variant="outline" icon={LogOut} onClick={handleLogout} className="w-full">
                  Log out
                </Button>
              ) : (
                <div className="flex gap-3">
                  <Link to={ROUTE_PATHS.LOGIN} className="flex-1">
                    <Button variant="outline" className="w-full">
                      Log in
                    </Button>
                  </Link>
                  <Link to={ROUTE_PATHS.REGISTER} className="flex-1">
                    <Button className="w-full">Sign up</Button>
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  );
}

const navLinkBase =
  "flex items-center gap-1 rounded-lg px-3 py-2 text-sm font-semibold transition-colors " +
  "focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400";

const navLinkClass = ({ isActive }) =>
  [navLinkBase, isActive ? "bg-amber-50 text-amber-700" : "text-ink-600 hover:bg-ink-50 hover:text-ink-900"].join(" ");

const mobileLinkClass = ({ isActive }) =>
  [
    "block rounded-lg px-3 py-2.5 text-sm font-semibold transition-colors",
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
    isActive ? "bg-amber-50 text-amber-700" : "text-ink-700 hover:bg-ink-50",
  ].join(" ");

function IconLink({ to, label, icon: Icon, hoverClass = "hover:text-ink-900" }) {
  return (
    <Link
      to={to}
      aria-label={label}
      title={label}
      className={`rounded-lg p-2 text-ink-500 transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 ${hoverClass}`}
    >
      <Icon className="h-5 w-5" aria-hidden="true" />
    </Link>
  );
}
