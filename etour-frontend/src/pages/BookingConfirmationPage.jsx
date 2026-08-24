import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { CheckCircle2, Calendar, Download } from "lucide-react";
import { useBookingFlow } from "../hooks/useBookingFlow";
import { useToast } from "../hooks/useToast";
import { fetchReceiptPdfBlob } from "../services/invoiceService";
import { formatCurrency, formatDate } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import BookingStepLayout from "../components/layout/BookingStepLayout";
import Button from "../components/common/Button";

export default function BookingConfirmationPage() {
  const { bookingResult, tour, reset } = useBookingFlow();
  const { showToast } = useToast();
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    return () => reset();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!bookingResult) {
    return <Navigate to={ROUTE_PATHS.HOME} replace />;
  }

  async function handleDownloadReceipt() {
    setDownloading(true);
    try {
      const blob = await fetchReceiptPdfBlob(bookingResult.bookingId);
      const url = URL.createObjectURL(blob);
      window.open(url, "_blank");
    } catch (err) {
      showToast(err.message || "Couldn't load the receipt yet - it may still be generating.", "error");
    } finally {
      setDownloading(false);
    }
  }

  return (
    <BookingStepLayout step={3} title="" requireTour={false}>
      <div className="flex flex-col items-center gap-4 rounded-card bg-white p-10 text-center shadow-card animate-fade-in">
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
          <CheckCircle2 className="h-8 w-8" />
        </div>
        <h2 className="font-display text-2xl font-bold text-ink-900">Booking confirmed!</h2>
        <p className="max-w-sm text-sm text-ink-500">
          Your trip to <strong>{tour?.title || bookingResult.tourTitle}</strong> is booked. A PDF receipt has been
          e-mailed to you, and a confirmation has been added to your dashboard.
        </p>

        <div className="mt-2 flex w-full flex-col gap-2 rounded-xl bg-ink-50 p-4 text-left text-sm">
          <Row label="Booking ID" value={`#${bookingResult.bookingId}`} />
          <Row label="Departure" value={formatDate(bookingResult.departureDate)} icon={Calendar} />
          <Row label="Total paid" value={formatCurrency(bookingResult.totalAmount)} />
          <Row label="Status" value={bookingResult.bookingStatus} />
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
            <Button className="w-full">Go to my bookings</Button>
          </Link>
        </div>
      </div>
    </BookingStepLayout>
  );
}

function Row({ label, value, icon: Icon }) {
  return (
    <div className="flex items-center justify-between">
      <span className="flex items-center gap-1.5 text-ink-500">
        {Icon && <Icon className="h-3.5 w-3.5" />} {label}
      </span>
      <span className="font-semibold text-ink-900">{value}</span>
    </div>
  );
}
