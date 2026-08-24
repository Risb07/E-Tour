import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { CreditCard, Landmark, Lock, ArrowLeft } from "lucide-react";
import { fetchPaymentSummary, payWithCard } from "../services/paymentService";
import { useToast } from "../hooks/useToast";
import { validateCardForm, digitsOnly } from "../utils/card";
import { formatCurrency, formatDate } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import CardForm from "../components/domain/CardForm";
import PaymentProcessingOverlay from "../components/domain/PaymentProcessingOverlay";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import ErrorState from "../components/common/ErrorState";
import Badge from "../components/ui/Badge";

const METHODS = [
  { value: "CREDIT_CARD", label: "Credit Card", icon: CreditCard },
  { value: "DEBIT_CARD", label: "Debit Card", icon: Landmark },
];

const emptyCard = () => ({
  cardNumber: "",
  cardHolderName: "",
  expiryMonth: "",
  expiryYear: "",
  cvv: "",
  saveCard: false,
});

/**
 * BRD 3.7 payment step, between Booking Summary and Confirmation.
 *
 * The booking already exists (created as PENDING on the summary step), so this
 * page only has to charge it. All amounts come from the server - the client
 * never computes or sends a total.
 */
export default function PaymentPage() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [summary, setSummary] = useState(null);
  const [status, setStatus] = useState("loading");
  const [method, setMethod] = useState("CREDIT_CARD");
  const [card, setCard] = useState(emptyCard);
  const [errors, setErrors] = useState({});
  const [stage, setStage] = useState(null); // processing | verifying | success

  useEffect(() => {
    let cancelled = false;
    fetchPaymentSummary(bookingId)
      .then((data) => {
        if (cancelled) return;
        setSummary(data);
        setStatus("succeeded");
      })
      .catch(() => {
        if (!cancelled) setStatus("failed");
      });
    return () => {
      cancelled = true;
    };
  }, [bookingId]);

  async function handlePay(event) {
    event.preventDefault();

    const validationErrors = validateCardForm(card);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      showToast("Please correct the highlighted card details.", "error");
      return;
    }

    setStage("processing");
    // Brief staged feedback so a fast response doesn't flash past unreadably.
    const toVerifying = setTimeout(() => setStage("verifying"), 700);

    try {
      const payment = await payWithCard({
        bookingId: Number(bookingId),
        paymentMethod: method,
        cardNumber: digitsOnly(card.cardNumber),
        cardHolderName: card.cardHolderName.trim(),
        expiryMonth: Number(card.expiryMonth),
        expiryYear: Number(card.expiryYear),
        cvv: digitsOnly(card.cvv),
        saveCard: Boolean(card.saveCard),
      });

      clearTimeout(toVerifying);
      // Drop the card data from memory the moment it's no longer needed.
      setCard(emptyCard());
      setStage("success");

      setTimeout(() => {
        navigate(ROUTE_PATHS.paymentSuccess(bookingId), {
          replace: true,
          state: { payment },
        });
      }, 1100);
    } catch (err) {
      clearTimeout(toVerifying);
      setStage(null);
      showToast(err.message || "Payment failed. Please try again.", "error");
    }
  }

  if (status === "loading") return <Loader label="Loading your booking..." />;
  if (status === "failed" || !summary) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16">
        <ErrorState variant="server" message="We couldn't load this booking for payment." />
      </div>
    );
  }

  // Guard against paying twice (e.g. someone reopens the URL after paying).
  if (summary.alreadyPaid || summary.bookingStatus === "CONFIRMED") {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-center">
        <h1 className="font-display text-2xl font-bold text-ink-900">This booking is already paid</h1>
        <p className="mt-2 text-sm text-ink-500">
          Order {summary.orderNumber || `#${summary.bookingId}`} has been confirmed.
        </p>
        <Button className="mt-6" onClick={() => navigate(ROUTE_PATHS.paymentSuccess(bookingId))}>
          View confirmation
        </Button>
      </div>
    );
  }

  const isBusy = stage !== null;

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
      <PaymentProcessingOverlay stage={stage} />

      <button
        onClick={() => navigate(-1)}
        disabled={isBusy}
        className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium text-ink-500 hover:text-ink-900 disabled:opacity-50"
      >
        <ArrowLeft className="h-4 w-4" /> Back to summary
      </button>

      <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">Payment</h1>
      <p className="mt-1 text-sm text-ink-500">
        Step 3 of 4 &middot; Secure checkout for booking #{summary.bookingId}
      </p>

      <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-[1fr_380px]">
        {/* ---------- Card form ---------- */}
        <form onSubmit={handlePay} className="order-2 lg:order-1">
          <div className="rounded-card bg-white p-6 shadow-card">
            <h2 className="font-display text-lg font-bold text-ink-900">Pay by card</h2>

            <div className="mt-4 grid grid-cols-2 gap-3">
              {METHODS.map((m) => (
                <button
                  key={m.value}
                  type="button"
                  onClick={() => setMethod(m.value)}
                  disabled={isBusy}
                  aria-pressed={method === m.value}
                  className={`flex items-center gap-2 rounded-xl border p-3 text-sm font-medium transition-colors disabled:opacity-60 ${
                    method === m.value
                      ? "border-amber-400 bg-amber-50 text-ink-900"
                      : "border-ink-200 text-ink-600 hover:border-ink-300"
                  }`}
                >
                  <m.icon className="h-4 w-4" /> {m.label}
                </button>
              ))}
            </div>

            <div className="mt-6">
              <CardForm form={card} errors={errors} onChange={setCard} disabled={isBusy} />
            </div>
          </div>

          <Button type="submit" size="lg" isLoading={isBusy} className="mt-5 w-full" icon={Lock}>
            Pay {formatCurrency(summary.grandTotal)}
          </Button>

          <p className="mt-3 text-center text-xs text-ink-400">
            Payments are simulated in this environment — no real card is charged.
          </p>
        </form>

        {/* ---------- Booking summary ---------- */}
        <aside className="order-1 lg:order-2 lg:sticky lg:top-20 lg:self-start">
          <div className="flex flex-col gap-4 rounded-card bg-white p-6 shadow-card">
            <div>
              <h2 className="font-display text-base font-bold text-ink-900">{summary.tourTitle}</h2>
              <p className="mt-1 text-sm text-ink-500">
                {formatDate(summary.departureDate)}
                {summary.returnDate && <> &rarr; {formatDate(summary.returnDate)}</>}
              </p>
              <p className="mt-0.5 text-sm text-ink-500">
                {summary.numberOfPassengers} passenger{summary.numberOfPassengers === 1 ? "" : "s"}
              </p>
            </div>

            {summary.passengers?.length > 0 && (
              <Block title="Passengers">
                <ul className="flex flex-col gap-1 text-sm text-ink-600">
                  {summary.passengers.map((p, i) => (
                    <li key={p.passengerId ?? i}>
                      {i + 1}. {p.fullName}
                    </li>
                  ))}
                </ul>
              </Block>
            )}

            {summary.roomSummary && (
              <Block title="Rooms">
                <p className="text-sm text-ink-600">
                  {summary.roomSummary.doubleRooms} twin, {summary.roomSummary.singleRooms} single
                  {summary.roomSummary.extraBeds > 0 && <>, {summary.roomSummary.extraBeds} extra bed</>}
                </p>
                <p className="mt-1 text-xs text-ink-400">
                  {summary.roomSummary.adults} adult
                  {summary.roomSummary.children > 0 && <>, {summary.roomSummary.children} child</>}
                  {summary.roomSummary.infants > 0 && <>, {summary.roomSummary.infants} infant (free)</>}
                </p>
              </Block>
            )}

            {summary.breakdown?.length > 0 && (
              <Block title="Fare breakdown">
                <ul className="flex flex-col gap-1 text-sm text-ink-600">
                  {summary.breakdown.map((line, i) => (
                    <li key={i} className="flex justify-between gap-3">
                      <span>
                        {line.label} × {line.quantity}
                      </span>
                      <span>{formatCurrency(line.lineTotal)}</span>
                    </li>
                  ))}
                </ul>
              </Block>
            )}

            {summary.addons?.length > 0 && (
              <Block title="Add-ons">
                <ul className="flex flex-col gap-1 text-sm text-ink-600">
                  {summary.addons.map((a) => (
                    <li key={a.bookingAddonId} className="flex justify-between gap-3">
                      <span>
                        {a.addonName} × {a.quantity}
                      </span>
                      <span>{formatCurrency(a.totalAddonCost)}</span>
                    </li>
                  ))}
                </ul>
              </Block>
            )}

            <div className="flex flex-col gap-2 border-t border-ink-100 pt-4 text-sm">
              <Row label="Sub-total" value={formatCurrency(summary.subTotal)} />
              {Number(summary.discountAmount) > 0 && (
                <Row
                  label="Discount"
                  value={`- ${formatCurrency(summary.discountAmount)}`}
                  accent
                />
              )}
              <Row
                label={`GST (${Number(summary.gstRatePercent).toFixed(0)}%)`}
                value={formatCurrency(summary.taxAmount)}
              />
            </div>

            <div className="flex items-center justify-between rounded-xl bg-ink-900 p-4 text-white">
              <span className="text-sm text-ink-200">Grand total</span>
              <span className="font-display text-xl font-bold">{formatCurrency(summary.grandTotal)}</span>
            </div>

            <Badge variant="info">Booking held as {summary.bookingStatus}</Badge>
          </div>
        </aside>
      </div>
    </div>
  );
}

function Block({ title, children }) {
  return (
    <div className="border-t border-ink-100 pt-4">
      <h3 className="mb-1.5 text-xs font-bold uppercase tracking-wide text-ink-400">{title}</h3>
      {children}
    </div>
  );
}

function Row({ label, value, accent = false }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-ink-500">{label}</span>
      <span className={accent ? "font-medium text-emerald-600" : "text-ink-900"}>{value}</span>
    </div>
  );
}
