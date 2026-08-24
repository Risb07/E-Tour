import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { CalendarCheck, History, Receipt, XCircle } from "lucide-react";
import { fetchMyBookings, cancelBooking } from "../services/bookingService";
import { fetchMyInvoices } from "../services/invoiceService";
import { useToast } from "../hooks/useToast";
import { formatCurrency, formatDate } from "../utils/format";
import { BOOKING_STATUS_LABELS } from "../constants/enums";
import { ROUTE_PATHS } from "../constants/routes";
import Badge from "../components/ui/Badge";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";

export default function CustomerDashboardPage() {
  const [bookings, setBookings] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [status, setStatus] = useState("loading");
  const [activeTab, setActiveTab] = useState("upcoming");
  const { showToast } = useToast();

  function loadData() {
    setStatus("loading");
    Promise.all([fetchMyBookings(), fetchMyInvoices()])
      .then(([bookingsRes, invoicesRes]) => {
        setBookings(bookingsRes);
        setInvoices(invoicesRes);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(loadData, []);

  async function handleCancel(bookingId) {
    if (!window.confirm("Cancel this booking? This can't be undone.")) return;
    try {
      await cancelBooking(bookingId);
      showToast("Booking cancelled.", "success");
      loadData();
    } catch (err) {
      showToast(err.message || "Couldn't cancel this booking.", "error");
    }
  }

  if (status === "loading") return <Loader label="Loading your dashboard..." />;

  const upcoming = bookings.filter((b) => ["PENDING", "CONFIRMED"].includes(b.bookingStatus));
  const past = bookings.filter((b) => ["CANCELLED", "COMPLETED"].includes(b.bookingStatus));

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">My Dashboard</h1>

      <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard icon={CalendarCheck} label="Upcoming" value={upcoming.length} />
        <StatCard icon={History} label="Total bookings" value={bookings.length} />
        <StatCard icon={Receipt} label="Invoices" value={invoices.length} />
        <StatCard
          icon={CalendarCheck}
          label="Total spent"
          value={formatCurrency(invoices.reduce((sum, i) => sum + Number(i.totalAmount || 0), 0))}
        />
      </div>

      <div className="mt-8 flex gap-1 border-b border-ink-100">
        {[
          { key: "upcoming", label: "Upcoming Tours" },
          { key: "history", label: "Booking History" },
          { key: "invoices", label: "Invoices" },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`border-b-2 px-4 py-3 text-sm font-semibold ${
              activeTab === tab.key ? "border-amber-500 text-ink-900" : "border-transparent text-ink-400"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="mt-6">
        {activeTab === "upcoming" && (
          <BookingList bookings={upcoming} onCancel={handleCancel} showCancel />
        )}
        {activeTab === "history" && <BookingList bookings={past} />}
        {activeTab === "invoices" && <InvoiceList invoices={invoices} />}
      </div>
    </div>
  );
}

function StatCard({ icon: Icon, label, value }) {
  return (
    <div className="rounded-card bg-white p-4 shadow-soft">
      <Icon className="h-5 w-5 text-amber-500" />
      <p className="mt-2 font-display text-xl font-bold text-ink-900">{value}</p>
      <p className="text-xs text-ink-500">{label}</p>
    </div>
  );
}

function BookingList({ bookings, onCancel, showCancel = false }) {
  if (bookings.length === 0) {
    return <EmptyState title="Nothing here yet" description="Your bookings will show up here." />;
  }
  return (
    <div className="flex flex-col gap-3">
      {bookings.map((booking) => {
        const statusInfo = BOOKING_STATUS_LABELS[booking.bookingStatus] || { label: booking.bookingStatus, variant: "info" };
        return (
          <div key={booking.bookingId} className="flex flex-wrap items-center justify-between gap-4 rounded-card bg-white p-5 shadow-soft">
            <div>
              <Link to={ROUTE_PATHS.tourDetails(booking.tourId)} className="font-semibold text-ink-900 hover:text-amber-600">
                {booking.tourTitle}
              </Link>
              <p className="mt-1 text-sm text-ink-500">
                Departs {formatDate(booking.departureDate)} · Booking #{booking.bookingId}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Badge variant={statusInfo.variant}>{statusInfo.label}</Badge>
              <span className="font-display text-base font-bold text-ink-900">{formatCurrency(booking.totalAmount)}</span>
              {showCancel && booking.bookingStatus !== "CANCELLED" && (
                <Button variant="ghost" size="sm" icon={XCircle} onClick={() => onCancel(booking.bookingId)}>
                  Cancel
                </Button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

function InvoiceList({ invoices }) {
  if (invoices.length === 0) {
    return <EmptyState title="No invoices yet" description="Invoices appear here after you complete a payment." />;
  }
  return (
    <div className="overflow-x-auto rounded-card bg-white shadow-soft">
      <table className="w-full text-left text-sm">
        <thead className="bg-ink-50 text-xs uppercase text-ink-500">
          <tr>
            <th className="px-4 py-3">Invoice #</th>
            <th className="px-4 py-3">Date</th>
            <th className="px-4 py-3">Sub-total</th>
            <th className="px-4 py-3">Tax</th>
            <th className="px-4 py-3">Total</th>
            <th className="px-4 py-3">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-100">
          {invoices.map((invoice) => (
            <tr key={invoice.invoiceId} className="hover:bg-ink-50">
              <td className="px-4 py-3 font-medium text-ink-900">{invoice.invoiceNumber}</td>
              <td className="px-4 py-3 text-ink-500">{formatDate(invoice.invoiceDate)}</td>
              <td className="px-4 py-3">{formatCurrency(invoice.subTotal)}</td>
              <td className="px-4 py-3">{formatCurrency(invoice.taxAmount)}</td>
              <td className="px-4 py-3 font-semibold">{formatCurrency(invoice.totalAmount)}</td>
              <td className="px-4 py-3">
                <Badge variant={invoice.invoiceStatus === "GENERATED" ? "success" : "danger"}>
                  {invoice.invoiceStatus}
                </Badge>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
