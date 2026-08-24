import { useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";
import { fetchAllBookings, updateBookingStatus } from "../../services/bookingService";
import { useToast } from "../../hooks/useToast";
import { formatCurrency, formatDate } from "../../utils/format";
import { BOOKING_STATUS_LABELS } from "../../constants/enums";
import Badge from "../../components/ui/Badge";
import Input from "../../components/common/Input";
import Loader from "../../components/common/Loader";
import EmptyState from "../../components/common/EmptyState";

const STATUS_OPTIONS = ["PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"];

export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [status, setStatus] = useState("loading");
  const [search, setSearch] = useState("");
  const { showToast } = useToast();

  function loadBookings() {
    setStatus("loading");
    fetchAllBookings()
      .then((data) => {
        setBookings(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(loadBookings, []);

  const filtered = useMemo(() => {
    if (!search.trim()) return bookings;
    const term = search.toLowerCase();
    return bookings.filter(
      (b) =>
        b.tourTitle?.toLowerCase().includes(term) ||
        b.customerName?.toLowerCase().includes(term) ||
        String(b.bookingId).includes(term)
    );
  }, [bookings, search]);

  async function handleStatusChange(bookingId, newStatus) {
    try {
      await updateBookingStatus(bookingId, newStatus);
      setBookings((current) =>
        current.map((b) => (b.bookingId === bookingId ? { ...b, bookingStatus: newStatus } : b))
      );
      showToast("Booking status updated.", "success");
    } catch (err) {
      showToast(err.message || "Couldn't update status.", "error");
    }
  }

  if (status === "loading") return <Loader label="Loading bookings..." />;

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Bookings</h1>
          <p className="mt-1 text-sm text-ink-500">{filtered.length} of {bookings.length} bookings</p>
        </div>
        <div className="w-full max-w-xs [&_label]:hidden">
          <Input label="Search" icon={Search} placeholder="Search by tour, customer, ID..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="mt-8">
          <EmptyState title="No bookings found" description="Try a different search term." />
        </div>
      ) : (
        <div className="mt-6 overflow-x-auto rounded-card bg-white shadow-soft">
          <table className="w-full text-left text-sm">
            <thead className="sticky top-0 bg-ink-50 text-xs uppercase text-ink-500">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Customer</th>
                <th className="px-4 py-3">Tour</th>
                <th className="px-4 py-3">Departure</th>
                <th className="px-4 py-3">Amount</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {filtered.map((booking) => {
                const info = BOOKING_STATUS_LABELS[booking.bookingStatus] || { label: booking.bookingStatus, variant: "info" };
                return (
                  <tr key={booking.bookingId} className="hover:bg-ink-50">
                    <td className="px-4 py-3 font-medium text-ink-900">#{booking.bookingId}</td>
                    <td className="px-4 py-3">{booking.customerName}</td>
                    <td className="px-4 py-3">{booking.tourTitle}</td>
                    <td className="px-4 py-3 text-ink-500">{formatDate(booking.departureDate)}</td>
                    <td className="px-4 py-3 font-semibold">{formatCurrency(booking.totalAmount)}</td>
                    <td className="px-4 py-3">
                      <select
                        value={booking.bookingStatus}
                        onChange={(e) => handleStatusChange(booking.bookingId, e.target.value)}
                        className="rounded-lg border border-ink-200 bg-white px-2 py-1.5 text-xs font-semibold"
                      >
                        {STATUS_OPTIONS.map((option) => (
                          <option key={option} value={option}>
                            {BOOKING_STATUS_LABELS[option]?.label || option}
                          </option>
                        ))}
                      </select>
                      <span className="ml-2 inline-block align-middle">
                        <Badge variant={info.variant}>{info.label}</Badge>
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
