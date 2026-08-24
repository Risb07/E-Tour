import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CreditCard } from "lucide-react";
import { useBookingFlow } from "../hooks/useBookingFlow";
import { useToast } from "../hooks/useToast";
import { formatCurrency, formatDate } from "../utils/format";
import { occupancyLabel, passengerTypeLabel, passengerTypeOf } from "../utils/passengerPricing";
import { ROUTE_PATHS } from "../constants/routes";
import BookingStepLayout from "../components/layout/BookingStepLayout";
import Button from "../components/common/Button";

export default function BookingSummaryPage() {
  const navigate = useNavigate();
  const { tour, schedule, numberOfPassengers, selectedAddons, passengers, estimatedTotal, submitBooking, getQuote } =
    useBookingFlow();
  const { showToast } = useToast();
  const [isProcessing, setIsProcessing] = useState(false);
  const [quote, setQuote] = useState(null);
  const [quoteStatus, setQuoteStatus] = useState("loading");

  // BRD 3.7 "Done" summary - real age-banded price + room requirement,
  // computed server-side from the passengers collected on the previous step.
  useEffect(() => {
    let cancelled = false;
    getQuote()
      .then((data) => {
        if (!cancelled) setQuote(data);
      })
      .catch(() => {
        if (!cancelled) setQuote(null);
      })
      .finally(() => {
        if (!cancelled) setQuoteStatus("done");
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const displayTotal = quote?.totalAmount ?? estimatedTotal;

  /**
   * BRD 3.7 - this step now only CREATES the booking (status PENDING, seats
   * held) and hands off to the dedicated payment step. The charge itself
   * happens on PaymentPage, so the customer sees the full breakdown and enters
   * card details on a page of its own.
   */
  async function handleContinueToPayment() {
    setIsProcessing(true);
    try {
      const booking = await submitBooking();
      navigate(ROUTE_PATHS.bookingPayment(booking.bookingId));
    } catch (err) {
      showToast(err.message || "Something went wrong creating your booking.", "error");
      setIsProcessing(false);
    }
  }

  return (
    <BookingStepLayout step={2} title="Review & confirm">
      <div className="flex flex-col gap-4">
        <SummaryBlock title="Trip">
          <p className="font-semibold text-ink-900">{tour?.title}</p>
          <p className="text-sm text-ink-500">
            {formatDate(schedule?.departureDate)} → {formatDate(schedule?.returnDate)}
          </p>
        </SummaryBlock>

        <SummaryBlock title={`Passengers (${numberOfPassengers})`}>
          <ul className="flex flex-col divide-y divide-ink-100 text-sm text-ink-600">
            {passengers.map((p, i) => {
              // The server's line is authoritative - it reflects what will
              // actually be charged. Until the quote lands (or if it fails)
              // fall back to the same calculation the passenger form showed.
              const line = quote?.passengerLines?.[i];
              // Both sides classify by date of birth, so the fallback shows
              // the same band the server will report once the quote lands.
              const type =
                line?.passengerTypeLabel ??
                passengerTypeLabel(passengerTypeOf(p, schedule?.departureDate));
              const category = line?.occupancyLabel ?? occupancyLabel(p.occupancy);
              const price = line?.price;

              return (
                <li key={i} className="flex items-start justify-between gap-3 py-2 first:pt-0 last:pb-0">
                  <div>
                    <p className="font-medium text-ink-900">
                      {i + 1}. {p.fullName}
                    </p>
                    <p className="text-xs text-ink-400">
                      {[type, category].filter(Boolean).join(" · ")}
                      {p.idProofNumber ? ` · ${p.idProofNumber}` : ""}
                    </p>
                  </div>
                  {price != null && (
                    <span className="shrink-0 font-semibold text-ink-900">
                      {Number(price) === 0 ? "Free" : formatCurrency(price)}
                    </span>
                  )}
                </li>
              );
            })}
          </ul>
        </SummaryBlock>

        {selectedAddons.length > 0 && (
          <SummaryBlock title="Add-ons">
            <ul className="flex flex-col gap-1 text-sm text-ink-600">
              {selectedAddons.map((a) => (
                <li key={a.addonId} className="flex justify-between">
                  <span>
                    {a.name} × {a.quantity}
                  </span>
                  <span>{formatCurrency(a.price * a.quantity)}</span>
                </li>
              ))}
            </ul>
          </SummaryBlock>
        )}

        {quoteStatus === "loading" && (
          <SummaryBlock title="Cost breakdown">
            <p className="text-sm text-ink-400">Calculating your price...</p>
          </SummaryBlock>
        )}

        {quote && (
          <SummaryBlock title="Cost breakdown">
            <ul className="flex flex-col gap-1 text-sm text-ink-600">
              {quote.breakdown.map((line, i) => (
                <li key={i} className="flex justify-between">
                  <span>
                    {line.label} × {line.quantity}
                  </span>
                  <span>{formatCurrency(line.lineTotal)}</span>
                </li>
              ))}
            </ul>
            {/* BRD 3.7 pax + room requirement summary. */}
            <p className="mt-3 text-xs text-ink-400">
              Rooms: {quote.roomSummary.doubleRooms} twin, {quote.roomSummary.singleRooms} single
              {quote.roomSummary.extraBeds > 0 ? `, ${quote.roomSummary.extraBeds} extra bed(s)` : ""}
              {" · "}
              {quote.roomSummary.adults} adult(s)
              {quote.roomSummary.children > 0 ? `, ${quote.roomSummary.children} child` : ""}
              {quote.roomSummary.infants > 0 ? `, ${quote.roomSummary.infants} infant (free)` : ""}
            </p>
          </SummaryBlock>
        )}

        <div className="flex items-center justify-between rounded-card bg-ink-900 p-5 text-white">
          <span className="text-sm text-ink-200">Total amount</span>
          <span className="font-display text-2xl font-bold">{formatCurrency(displayTotal)}</span>
        </div>

        <p className="text-xs text-ink-400">
          GST is added on the next step, where you will see the final amount before paying.
        </p>
      </div>

      <div className="mt-8 flex justify-between">
        <Button variant="ghost" onClick={() => navigate(-1)} disabled={isProcessing}>
          Back
        </Button>
        <Button onClick={handleContinueToPayment} size="lg" isLoading={isProcessing} icon={CreditCard}>
          Continue to payment
        </Button>
      </div>
    </BookingStepLayout>
  );
}

function SummaryBlock({ title, children }) {
  return (
    <div className="rounded-card bg-white p-5 shadow-soft">
      <h4 className="mb-2 text-xs font-bold uppercase tracking-wide text-ink-400">{title}</h4>
      {children}
    </div>
  );
}
