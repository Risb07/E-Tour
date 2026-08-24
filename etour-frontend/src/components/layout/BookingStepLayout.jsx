import { Navigate } from "react-router-dom";
import StepIndicator from "../ui/StepIndicator";
import { useBookingFlow } from "../../hooks/useBookingFlow";
import { ROUTE_PATHS } from "../../constants/routes";

const STEPS = ["Add-ons", "Passengers", "Summary", "Confirmation"];

/**
 * `requireTour` guards against a user landing on /booking/passengers etc.
 * directly (bookmarked URL, back button after reset) without having gone
 * through Tour Details first to pick a schedule - there'd be nothing to
 * show. Confirmation doesn't require it since it reads bookingResult instead.
 */
export default function BookingStepLayout({ step, title, children, requireTour = true }) {
  const { tour, schedule, bookingResult } = useBookingFlow();

  if (requireTour && (!tour || !schedule) && !bookingResult) {
    return <Navigate to={ROUTE_PATHS.HOME} replace />;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6 lg:px-8">
      <StepIndicator steps={STEPS} currentStep={step} />
      <h1 className="mt-8 font-display text-2xl font-bold text-ink-900">{title}</h1>
      <div className="mt-6">{children}</div>
    </div>
  );
}
