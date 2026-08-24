import { AlertTriangle, WifiOff, ServerCrash, ShieldAlert, SearchX } from "lucide-react";
import Button from "./Button";

// One shared component for every "something went wrong" screen (404, 403,
// 500, network failure, generic API error) - consistent look, one place
// to change the copy/illustration approach later.
const PRESETS = {
  notFound: { icon: SearchX, title: "Page not found", message: "This page doesn't exist or may have moved." },
  forbidden: {
    icon: ShieldAlert,
    title: "Access restricted",
    message: "You don't have permission to view this page.",
  },
  network: {
    icon: WifiOff,
    title: "Connection lost",
    message: "Check your internet connection and try again.",
  },
  server: {
    icon: ServerCrash,
    title: "Something went wrong on our end",
    message: "Our team has been notified. Please try again shortly.",
  },
  generic: { icon: AlertTriangle, title: "Something went wrong", message: "Please try again." },
};

export default function ErrorState({ variant = "generic", message, onRetry, retryLabel = "Try again" }) {
  const preset = PRESETS[variant] || PRESETS.generic;
  const Icon = preset.icon;

  return (
    <div className="flex flex-col items-center justify-center gap-3 py-20 px-6 text-center animate-fade-in">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-red-50 text-red-500">
        <Icon className="h-7 w-7" strokeWidth={1.75} aria-hidden="true" />
      </div>
      <h3 className="font-display text-lg font-bold text-ink-900">{preset.title}</h3>
      <p className="max-w-sm text-sm text-ink-500">{message || preset.message}</p>
      {onRetry && (
        <Button variant="outline" onClick={onRetry} className="mt-2">
          {retryLabel}
        </Button>
      )}
    </div>
  );
}
