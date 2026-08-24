import { Check } from "lucide-react";

/** @param {{steps: string[], currentStep: number}} props - currentStep is 0-based */
export default function StepIndicator({ steps, currentStep }) {
  return (
    <ol className="flex items-center w-full">
      {steps.map((step, index) => {
        const isComplete = index < currentStep;
        const isActive = index === currentStep;
        return (
          <li key={step} className="flex flex-1 items-center last:flex-none">
            <div className="flex flex-col items-center gap-1.5">
              <div
                className={[
                  "flex h-9 w-9 items-center justify-center rounded-full text-sm font-bold transition-colors",
                  isComplete && "bg-amber-500 text-ink-900",
                  isActive && "bg-ink-900 text-white ring-4 ring-ink-100",
                  !isComplete && !isActive && "bg-ink-100 text-ink-400",
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                {isComplete ? <Check className="h-4 w-4" /> : index + 1}
              </div>
              <span
                className={`hidden text-xs font-medium sm:block ${isActive ? "text-ink-900" : "text-ink-400"}`}
              >
                {step}
              </span>
            </div>
            {index < steps.length - 1 && (
              <div className={`mx-2 h-0.5 flex-1 ${isComplete ? "bg-amber-400" : "bg-ink-100"}`} />
            )}
          </li>
        );
      })}
    </ol>
  );
}
