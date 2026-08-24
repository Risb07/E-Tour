import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { CheckCircle2, Calendar, Download, Receipt, Copy, Check } from "lucide-react";
import { fetchPaymentSummary } from "../services/paymentService";
import { fetchReceiptPdfBlob } from "../services/invoiceService";
import { useToast } from "../hooks/useToast";
import { useBookingFlow } from "../hooks/useBookingFlow";
import { formatCurrency, formatDate } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import Badge from "../components/ui/Badge";

/**
 * Payment success / booking confirmation.
 *
 * The payment record arrives via router state (set by PaymentPage) so the
 * transaction id shows immediately. The booking summary is refetched so a
 * direct visit or a refresh still renders correctly.
 */
export default function PaymentSuccessPage() {
  const { bookingId } = useParams();
  const { state } = useLocation();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { reset } = useBookingFlow();

  const payment = state?.payment || null;
  const [summary, setSummary] = useState(null);
  const [status, setStatus] = useState("loading");
  const [downloading, setDownloading] = useState(false);
  const [copied, setCopied] = useState(false);

  // The multi-step booking flow is finished - clear it so the wizard doesn't
  // resume from stale state if the user starts another booking.
  useEffect(() => {
    reset();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchPaymentSummary(bookingId)
      .then((data) => {
        setSummary(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }, [bookingId]);

  async function handleDownloadReceipt() {
    setDownloading(true);
    try {
      const blob = await fetchReceiptPdfBlob(bookingId);
      const url = URL.createObjectURL(blob);
      window.open(url, "_blank");
      URL.revokeObjectURL(url);
    } catch (err) {
      showToast(err.message || "Couldn't load the receipt yet.", "error");
    } finally {
      setDownloading(false);
    }
  }

  function copyTransactionId() {
    const id = payment?.transactionId || payment?.transactionRef;
    if (!id) return;
    navigator.clipboard?.writeText(id).then(
      () => {
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      },
      () => showToast("Couldn't copy the transaction ID.", "error")
    );
  }

  if (status === "loading") return <Loader label="Confirming your booking..." />;

  const transactionId = payment?.transactionId || payment?.transactionRef;

  return (
    <div className="mx-auto max-w-2xl px-4 py-12 sm:px-6">
      <div className="flex flex-col items-center gap-4 rounded-card bg-white p-8 text-center shadow-card animate-fade-in sm:p-10">
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
          <CheckCircle2 className="h-8 w-8" />
        </div>

        <h1 className="font-display text-2xl font-bold text-ink-900">Payment successful</h1>
        <p className="max-w-sm text-sm text-ink-500">
          Your trip to <strong>{summary?.tourTitle}</strong> is confirmed. A PDF receipt has been
          e-mailed to you.
        </p>

        <div className="mt-2 flex w-full flex-col gap-2 rounded-xl bg-ink-50 p-4 text-left text-sm">
          <Row label="Booking ID" value={`#${bookingId}`} />
          {summary?.orderNumber && <Row label="Order number" value={summary.orderNumber} />}
          {transactionId && (
            <div className="flex items-center justify-between gap-3">
              <span className="text-ink-500">Transaction ID</span>
              <button
                onClick={copyTransactionId}
                className="flex items-center gap-1.5 font-mono text-xs font-semibold text-ink-900 hover:text-amber-600"
                aria-label="Copy transaction ID"
              >
                <span className="max-w-[180px] truncate">{transactionId}</span>
                {copied ? <Check className="h-3.5 w-3.5 text-emerald-600" /> : <Copy className="h-3.5 w-3.5" />}
              </button>
            </div>
          )}
          {payment?.cardSummary && <Row label="Paid with" value={payment.cardSummary} />}
          {summary?.departureDate && (
            <Row label="Departure" value={formatDate(summary.departureDate)} icon={Calendar} />
          )}
          {summary?.grandTotal != null && (
            <Row label="Total paid" value={formatCurrency(summary.grandTotal)} />
          )}
          <div className="flex items-center justify-between">
            <span className="text-ink-500">Status</span>
            <Badge variant="success">{summary?.bookingStatus || "CONFIRMED"}</Badge>
          </div>
        </div>

        <div className="mt-4 flex w-full flex-col gap-3 sm:flex-row">
          <Button
            variant="outline"
            className="flex-1"
            icon={Download}
            isLoading={downloading}
            onClick={handleDownloadReceipt}
          >
            Download receipt
          </Button>
          <Link to={ROUTE_PATHS.CUSTOMER_DASHBOARD} className="flex-1">
            <Button className="w-full" icon={Receipt}>
              Go to my bookings
            </Button>
          </Link>
        </div>

        <button
          onClick={() => navigate(ROUTE_PATHS.SEARCH)}
          className="mt-2 text-sm font-medium text-ink-500 underline hover:text-ink-900"
        >
          Browse more tours
        </button>
      </div>
    </div>
  );
}

function Row({ label, value, icon: Icon }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="flex items-center gap-1.5 text-ink-500">
        {Icon && <Icon className="h-3.5 w-3.5" />} {label}
      </span>
      <span className="text-right font-semibold text-ink-900">{value}</span>
    </div>
  );
}
