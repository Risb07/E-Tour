import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { User, Mail, Phone, Lock, UserPlus } from "lucide-react";
import AuthLayout from "../components/layout/AuthLayout";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import GoogleSignInButton from "../components/common/GoogleSignInButton";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import {
  validateEmail,
  validatePassword,
  validateRequired,
  validatePhone,
} from "../utils/validators";
import { ROUTE_PATHS } from "../constants/routes";
import { ROLES } from "../constants/roles";

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  phone: "",
};

export default function RegisterPage() {
  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const { register, status } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const isSubmitting = status === "loading";

  function handleChange(field) {
    return (event) => {
      setForm((current) => ({ ...current, [field]: event.target.value }));
      setFieldErrors((current) => ({ ...current, [field]: "" }));
    };
  }

  function validate() {
    const errors = {
      firstName: validateRequired(form.firstName, "First name"),
      lastName: "",
      email: validateEmail(form.email),
      password: validatePassword(form.password),
      phone: validatePhone(form.phone),
    };
    setFieldErrors(errors);
    return Object.values(errors).every((message) => !message);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (!validate()) return;

    try {
      const user = await register(form);
      showToast(`Welcome, ${user.firstName}! Your account is ready.`, "success");
      // Auto-login happened in register() - route by role like the login page.
      if (user.role === ROLES.ADMIN) {
        navigate(ROUTE_PATHS.ADMIN_DASHBOARD, { replace: true });
      } else {
        navigate(ROUTE_PATHS.HOME, { replace: true });
      }
    } catch (err) {
      showToast(err.message || "Registration failed. Please try again.", "error");
    }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Start planning trips in a couple of minutes.">
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="First name"
            required
            icon={User}
            value={form.firstName}
            onChange={handleChange("firstName")}
            error={fieldErrors.firstName}
          />
          <Input
            label="Last name"
            value={form.lastName}
            onChange={handleChange("lastName")}
            error={fieldErrors.lastName}
          />
        </div>
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
          label="Phone number"
          type="tel"
          required
          icon={Phone}
          value={form.phone}
          onChange={handleChange("phone")}
          error={fieldErrors.phone}
        />
        <Input
          label="Password"
          type="password"
          autoComplete="new-password"
          required
          icon={Lock}
          value={form.password}
          onChange={handleChange("password")}
          error={fieldErrors.password}
        />

        <Button type="submit" icon={UserPlus} isLoading={isSubmitting} className="w-full mt-1">
          Create account
        </Button>
      </form>

      {/* Signing up with Google creates the account on first use, so the same
          button serves both pages. */}
      <GoogleSignInButton label="Sign up with Google" disabled={isSubmitting} />

      <p className="mt-6 text-sm text-ink-500">
        Already have an account?{" "}
        <Link to={ROUTE_PATHS.LOGIN} className="font-semibold text-ink-800 hover:text-amber-600">
          Log in
        </Link>
      </p>
    </AuthLayout>
  );
}
