import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AuthLayout from "../components/layout/AuthLayout";
import Loader from "../components/common/Loader";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { ROUTE_PATHS } from "../constants/routes";
import { ROLES } from "../constants/roles";

/**
 * Landing point for Google sign-in.
 *
 * The backend has already done the whole OAuth 2.0 exchange - authorization
 * code, token, ID token verification, account lookup or creation - and sends
 * the browser here with a finished eTour JWT on the query string. All this
 * page does is move that into AuthContext and get out of the way.
 *
 * The token is stripped from the URL immediately via `replace`, so it does not
 * sit in browser history, and this page is never a back-navigation target.
 */
export default function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const { loginWithToken } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [message, setMessage] = useState("Finishing sign-in...");

  // React 18 StrictMode mounts effects twice in development. Without this
  // guard the toast would fire twice and the navigation would run twice.
  const hasHandledRef = useRef(false);

  useEffect(() => {
    if (hasHandledRef.current) return;
    hasHandledRef.current = true;

    const error = searchParams.get("error");
    if (error) {
      showToast(error, "error");
      setMessage("Sign-in failed. Redirecting...");
      navigate(ROUTE_PATHS.LOGIN, { replace: true });
      return;
    }

    const token = searchParams.get("token");
    if (!token) {
      showToast("Google sign-in did not complete. Please try again.", "error");
      navigate(ROUTE_PATHS.LOGIN, { replace: true });
      return;
    }

    const profile = {
      userId: Number(searchParams.get("userId")),
      firstName: searchParams.get("firstName") || "",
      lastName: searchParams.get("lastName") || "",
      email: searchParams.get("email") || "",
      role: searchParams.get("role") || ROLES.CUSTOMER,
    };

    try {
      const user = loginWithToken(token, profile);
      showToast(`Welcome, ${user.firstName || user.email}!`, "success");

      // Same role-based landing as the password login, so the two routes are
      // indistinguishable to the user from here on.
      navigate(user.role === ROLES.ADMIN ? ROUTE_PATHS.ADMIN_DASHBOARD : ROUTE_PATHS.HOME, {
        replace: true,
      });
    } catch (err) {
      showToast(err.message || "Could not complete sign-in. Please try again.", "error");
      navigate(ROUTE_PATHS.LOGIN, { replace: true });
    }
  }, [searchParams, loginWithToken, showToast, navigate]);

  return (
    <AuthLayout title="Signing you in" subtitle="One moment while we finish setting up your session.">
      <Loader label={message} />
    </AuthLayout>
  );
}
