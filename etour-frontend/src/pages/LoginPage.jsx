import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { Mail, Lock, LogIn } from "lucide-react";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import GoogleSignInButton from "../components/common/GoogleSignInButton";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { validateEmail } from "../utils/validators";
import { ROUTE_PATHS } from "../constants/routes";
import { ROLES } from "../constants/roles";

const initialForm = { email: "", password: "" };

export default function LoginPage() {
  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const { login, status } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const isSubmitting = status === "loading";

  function handleChange(field) {
    return (event) => {
      setForm((current) => ({ ...current, [field]: event.target.value }));
      setFieldErrors((current) => ({ ...current, [field]: "" }));
    };
  }

  function validate() {
    const errors = {
      email: validateEmail(form.email),
      password: form.password ? "" : "Password is required",
    };
    setFieldErrors(errors);
    return Object.values(errors).every((message) => !message);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (!validate()) return;

    try {
      const user = await login(form);
      showToast(`Welcome back, ${user.firstName}!`, "success");

      // Respect a redirect the user was trying to reach before being
      // bounced to /login by ProtectedRoute, otherwise route by role.
      const redirectTo = location.state?.from;
      if (redirectTo) {
        navigate(redirectTo, { replace: true });
      } else if (user.role === ROLES.ADMIN) {
        navigate(ROUTE_PATHS.ADMIN_DASHBOARD, { replace: true });
      } else {
        navigate(ROUTE_PATHS.HOME, { replace: true });
      }
    } catch (err) {
      showToast(err.message || "Login failed. Please try again.", "error");
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to continue planning your next trip.">
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          required
          icon={Mail}
          value={form.email}
          onChange={handleChange("email")}
          error={fieldErrors.email}
        />
        <Input
          label="Password"
          type="password"
          autoComplete="current-password"
          required
          icon={Lock}
          value={form.password}
          onChange={handleChange("password")}
          error={fieldErrors.password}
        />

        <Button type="submit" icon={LogIn} isLoading={isSubmitting} className="w-full mt-1">
          Log in
        </Button>
      </form>

      {/* Renders only when the backend has Google credentials configured. */}
      <GoogleSignInButton label="Continue with Google" disabled={isSubmitting} />

      <p className="mt-6 text-sm text-ink-500">
        New to eTour?{" "}
        <Link to={ROUTE_PATHS.REGISTER} className="font-semibold text-ink-800 hover:text-amber-600">
          Create an account
        </Link>
      </p>
    </AuthLayout>
  );
}
