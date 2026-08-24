import { useEffect, useState } from "react";
import { fetchAuthProviders, getGoogleLoginUrl } from "../../services/authService";

/**
 * "Continue with Google" button.
 *
 * Renders nothing at all unless the backend reports that Google credentials
 * are configured, so a deployment without them shows a clean email/password
 * form rather than a button that leads to an error page.
 *
 * Clicking performs a full-page navigation to the backend, which is what
 * starts the OAuth 2.0 authorization code flow. This deliberately is not a
 * fetch: the browser has to follow redirects to Google's consent screen and
 * back, and only a top-level navigation can do that.
 *
 * The mark is Google's official four-colour "G", inlined as SVG so the button
 * needs no third-party script and stays styled by our own Tailwind tokens.
 */
export default function GoogleSignInButton({ label = "Continue with Google", disabled = false }) {
  const [isEnabled, setIsEnabled] = useState(false);
  const [isRedirecting, setIsRedirecting] = useState(false);

  useEffect(() => {
    // Guards against a state update after the page has navigated away or the
    // component unmounted mid-request.
    let isMounted = true;

    fetchAuthProviders().then((providers) => {
      if (isMounted) setIsEnabled(providers.google);
    });

    return () => {
      isMounted = false;
    };
  }, []);

  if (!isEnabled) return null;

  function handleClick() {
    setIsRedirecting(true);
    window.location.assign(getGoogleLoginUrl());
  }

  return (
    <div className="mt-6">
      <div className="flex items-center gap-3" aria-hidden="true">
        <span className="h-px flex-1 bg-ink-200" />
        <span className="text-xs font-medium uppercase tracking-wide text-ink-400">or</span>
        <span className="h-px flex-1 bg-ink-200" />
      </div>

      <button
        type="button"
        onClick={handleClick}
        disabled={disabled || isRedirecting}
        aria-busy={isRedirecting || undefined}
        className={[
          "mt-4 inline-flex w-full items-center justify-center gap-3 rounded-pill",
          "border-2 border-ink-200 bg-white px-5 py-2.5 text-sm font-semibold text-ink-800",
          "transition-all duration-200 ease-out",
          "hover:border-amber-400 hover:text-amber-600",
          "focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 focus-visible:ring-offset-2",
          "active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60 disabled:active:scale-100",
        ].join(" ")}
      >
        {isRedirecting ? (
          <span
            className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
            aria-hidden="true"
          />
        ) : (
          <GoogleMark />
        )}
        {isRedirecting ? "Redirecting to Google..." : label}
      </button>
    </div>
  );
}

function GoogleMark() {
  return (
    <svg className="h-4 w-4 shrink-0" viewBox="0 0 18 18" aria-hidden="true" focusable="false">
      <path
        fill="#4285F4"
        d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.72v2.26h2.92c1.7-1.57 2.68-3.88 2.68-6.62Z"
      />
      <path
        fill="#34A853"
        d="M9 18c2.43 0 4.47-.81 5.96-2.18l-2.92-2.26c-.81.54-1.84.86-3.04.86-2.34 0-4.32-1.58-5.03-3.7H.96v2.33A9 9 0 0 0 9 18Z"
      />
      <path
        fill="#FBBC05"
        d="M3.97 10.72a5.4 5.4 0 0 1 0-3.44V4.95H.96a9 9 0 0 0 0 8.1l3.01-2.33Z"
      />
      <path
        fill="#EA4335"
        d="M9 3.58c1.32 0 2.5.46 3.44 1.35l2.58-2.58C13.46.9 11.43 0 9 0A9 9 0 0 0 .96 4.95l3.01 2.33C4.68 5.16 6.66 3.58 9 3.58Z"
      />
    </svg>
  );
}
