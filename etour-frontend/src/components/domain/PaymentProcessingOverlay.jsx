import { CheckCircle2, Loader2, ShieldCheck, CreditCard } from "lucide-react";

/**
 * Full-screen status while a charge is in flight.
 *
 * The stages are cosmetic pacing over one real request - they reassure rather
 * than report, so the labels stay generic ("Verifying") instead of claiming a
 * specific step completed. The overlay blocks interaction, which is what
 * actually prevents a double submit.
 */
const STAGES = [
  { key: "processing", label: "Processing payment", icon: CreditCard },
  { key: "verifying", label: "Verifying with your bank", icon: ShieldCheck },
  { key: "success", label: "Payment successful", icon: CheckCircle2 },
];

export default function PaymentProcessingOverlay({ stage }) {
  if (!stage) return null;

  const activeIndex = STAGES.findIndex((s) => s.key === stage);

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink-900/80 p-4 backdrop-blur-sm"
    >
      <div className="w-full max-w-sm rounded-card bg-white p-8 text-center shadow-lifted">
        <div
          className={`mx-auto flex h-16 w-16 items-center justify-center rounded-full ${
            stage === "success" ? "bg-emerald-50 text-emerald-600" : "bg-amber-50 text-amber-600"
          }`}
        >
          {stage === "success" ? (
            <CheckCircle2 className="h-8 w-8" />
          ) : (
            <Loader2 className="h-8 w-8 animate-spin" />
          )}
        </div>

        <p className="mt-5 font-display text-lg font-bold text-ink-900">
          {STAGES[activeIndex]?.label}
        </p>
        <p className="mt-1 text-sm text-ink-500">
          {stage === "success"
            ? "Redirecting to your confirmation..."
            : "Please don't close or refresh this page."}
        </p>

        <ol className="mt-6 flex items-center justify-center gap-2">
          {STAGES.map((s, i) => (
            <li
              key={s.key}
              aria-current={i === activeIndex ? "step" : undefined}
              className={`h-1.5 rounded-full transition-all duration-500 ${
                i <= activeIndex ? "w-10 bg-amber-500" : "w-6 bg-ink-200"
              }`}
            />
          ))}
        </ol>
      </div>
    </div>
  );
}
