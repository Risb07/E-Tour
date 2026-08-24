import { useEffect, useState } from "react";
import { Users, Map, CalendarCheck, IndianRupee, PackageCheck, Clock, XCircle } from "lucide-react";
import { fetchDashboardStats } from "../../services/adminService";
import { formatCurrency } from "../../utils/format";
import Loader from "../../components/common/Loader";
import ErrorState from "../../components/common/ErrorState";

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    fetchDashboardStats()
      .then((data) => {
        setStats(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }, []);

  if (status === "loading") return <Loader label="Loading dashboard..." />;
  if (status === "failed" || !stats) return <ErrorState variant="server" />;

  const cards = [
    { icon: Users, label: "Total customers", value: stats.totalCustomers },
    { icon: Map, label: "Total tours", value: stats.totalTours },
    { icon: PackageCheck, label: "Active tours", value: stats.activeTours },
    { icon: CalendarCheck, label: "Total bookings", value: stats.totalBookings },
    { icon: PackageCheck, label: "Confirmed", value: stats.confirmedBookings },
    { icon: Clock, label: "Pending", value: stats.pendingBookings },
    { icon: XCircle, label: "Cancelled", value: stats.cancelledBookings },
    { icon: IndianRupee, label: "Total revenue", value: formatCurrency(stats.totalRevenue) },
  ];

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-ink-900">Dashboard</h1>
      <p className="mt-1 text-sm text-ink-500">Live overview from the admin stats endpoint.</p>

      <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        {cards.map((card) => (
          <div key={card.label} className="rounded-card bg-white p-5 shadow-soft">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-amber-50 text-amber-600">
              <card.icon className="h-4 w-4" />
            </div>
            <p className="mt-3 font-display text-2xl font-bold text-ink-900">{card.value}</p>
            <p className="text-xs text-ink-500">{card.label}</p>
          </div>
        ))}
      </div>

      {/* <div className="mt-8 rounded-card border border-dashed border-ink-200 bg-white p-6 text-sm text-ink-500">
        <strong className="text-ink-700">Note:</strong> All catalog entities (tours, categories, schedules, costs,
        media, stay &amp; meals, content, banners, nav menu) are manageable from the sidebar. Admin user management
        (creating staff accounts / roles) is not exposed in the UI yet - see the README for the full list.
      </div> */}
    </div>
  );
}
