import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTE_PATHS } from "../constants/routes";

/**
 * Wraps a set of routes (via <Outlet />) and enforces:
 *  1. must be authenticated
 *  2. if `allowedRoles` is given, user.role must be in that list
 *
 * Redirects carry `state.from` so LoginPage can send the user back to
 * where they were headed after a successful login.
 */
export default function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, user, isInitializing } = useAuth();
  const location = useLocation();

  // Auth state rehydrates from sessionStorage on mount (see AuthContext).
  // Avoid a false "redirect to login" flash while that check is settling.
  if (isInitializing) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTE_PATHS.LOGIN} replace state={{ from: location.pathname }} />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={ROUTE_PATHS.UNAUTHORIZED} replace />;
  }

  return <Outlet />;
}
