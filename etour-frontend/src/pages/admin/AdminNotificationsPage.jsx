import { useCallback, useEffect, useState } from "react";
import { RefreshCw, Send, RotateCcw, X } from "lucide-react";
import {
  fetchNotifications,
  fetchNotificationStats,
  fetchNotificationTemplates,
  sendNotification,
  retryNotification,
} from "../../services/notificationService";
import { useToast } from "../../hooks/useToast";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Loader from "../../components/common/Loader";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import Badge from "../../components/ui/Badge";

const STATUS_FILTERS = [
  { value: "", label: "All" },
  { value: "PENDING", label: "Pending" },
  { value: "SENT", label: "Sent" },
  { value: "FAILED", label: "Failed" },
];

const STATUS_VARIANT = {
  PENDING: "info",
  SENT: "success",
  FAILED: "danger",
};

const PAGE_SIZE = 20;

function formatWhen(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString();
}

/**
 * Delivery log for the notification microservice.
 *
 * Everything on this page is served by that service at /svc/..., not by the
 * Java or .NET backend - which is the point: the same admin session works
 * against a separate process with its own database, because both trust the
 * same JWT.
 */
export default function AdminNotificationsPage() {
  const [rows, setRows] = useState([]);
  const [stats, setStats] = useState(null);
  const [templates, setTemplates] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [status, setStatus] = useState("loading");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [retryingId, setRetryingId] = useState(null);
  const [form, setForm] = useState({ recipient: "", template: "", subject: "", message: "" });
  const { showToast } = useToast();

  const load = useCallback(async () => {
    setStatus("loading");
    try {
      // Stats and the page are independent; fetching together keeps the header
      // and the table describing the same moment.
      const [pageData, statsData] = await Promise.all([
        fetchNotifications({ status: statusFilter || null, page, size: PAGE_SIZE }),
        fetchNotificationStats(),
      ]);
      setRows(pageData?.content || []);
      setTotalPages(pageData?.totalPages ?? 0);
      setStats(statsData);
      setStatus("succeeded");
    } catch (err) {
      // 401/403 here almost always means the microservice could not confirm the
      // admin role with the backend, which is worth saying rather than showing
      // a bare "failed".
      setStatus("failed");
      if (err.status === 403) {
        showToast("The notification service could not confirm your admin role.", "error");
      }
    }
  }, [statusFilter, page, showToast]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    fetchNotificationTemplates()
      .then((data) => setTemplates(Array.isArray(data) ? data : []))
      .catch(() => setTemplates([]));
  }, []);

  function openForm() {
    setForm({ recipient: "", template: "TEST_MESSAGE", subject: "", message: "" });
    setIsFormOpen(true);
  }

  async function handleSend(event) {
    event.preventDefault();
    setIsSending(true);
    try {
      await sendNotification({
        recipient: form.recipient,
        template: form.template,
        variables: { subject: form.subject, message: form.message },
      });
      showToast("Queued. The worker will pick it up within a few seconds.", "success");
      setIsFormOpen(false);
      setPage(0);
      load();
    } catch (err) {
      showToast(err.message || "Couldn't queue that message.", "error");
    } finally {
      setIsSending(false);
    }
  }

  async function handleRetry(row) {
    setRetryingId(row.notificationId);
    try {
      await retryNotification(row.notificationId);
      showToast(`Notification ${row.notificationId} re-queued.`, "success");
      load();
    } catch (err) {
      showToast(err.message || "Couldn't re-queue that notification.", "error");
    } finally {
      setRetryingId(null);
    }
  }

  const cards = stats
    ? [
        { label: "Pending", value: stats.pending },
        { label: "Sent", value: stats.sent },
        { label: "Failed", value: stats.failed },
        { label: "Total", value: stats.total },
      ]
    : [];

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Notifications</h1>
          <p className="mt-1 text-sm text-ink-500">
            Outbound email queue, served by the notification microservice. Each message gets one
            retry; after that it is held here for you to re-queue.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" icon={RefreshCw} onClick={load}>
            Refresh
          </Button>
          <Button icon={Send} onClick={openForm}>
            Send test
          </Button>
        </div>
      </div>

      {stats && (
        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
          {cards.map((card) => (
            <div key={card.label} className="rounded-card bg-white p-5 shadow-soft">
              <p className="font-display text-2xl font-bold text-ink-900">{card.value}</p>
              <p className="text-xs text-ink-500">{card.label}</p>
            </div>
          ))}
        </div>
      )}

      {isFormOpen && (
        <form onSubmit={handleSend} className="mt-6 rounded-card bg-white p-5 shadow-soft">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-base font-bold text-ink-900">Queue a message</h2>
            <button
              type="button"
              onClick={() => setIsFormOpen(false)}
              className="text-ink-400 hover:text-ink-700"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Recipient"
              type="email"
              required
              value={form.recipient}
              onChange={(e) => setForm((f) => ({ ...f, recipient: e.target.value }))}
              placeholder="someone@example.com"
            />
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-medium text-ink-700">Template</label>
              <select
                value={form.template}
                onChange={(e) => setForm((f) => ({ ...f, template: e.target.value }))}
                className="w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:border-amber-400 focus:outline-none"
              >
                {templates.map((t) => (
                  <option key={t.code} value={t.code}>
                    {t.code}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mt-4">
            <Input
              label="Subject"
              value={form.subject}
              onChange={(e) => setForm((f) => ({ ...f, subject: e.target.value }))}
              placeholder="Test from the eTour notification service"
            />
          </div>

          <div className="mt-4 flex flex-col gap-1.5">
            <label className="text-sm font-medium text-ink-700">Message</label>
            <textarea
              rows={3}
              value={form.message}
              onChange={(e) => setForm((f) => ({ ...f, message: e.target.value }))}
              placeholder="Body text..."
              className="w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 placeholder:text-ink-300 focus:border-amber-400 focus:outline-none"
            />
          </div>

          <p className="mt-3 text-xs text-ink-500">
            Tip: send to an address starting with <code className="font-semibold">fail</code> (e.g.
            fail@example.com) to watch the retry and dead-letter path in action.
          </p>

          <div className="mt-4 flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isSending}>
              Queue
            </Button>
          </div>
        </form>
      )}

      <div className="mt-6 flex flex-wrap gap-2">
        {STATUS_FILTERS.map((filter) => (
          <button
            key={filter.label}
            onClick={() => {
              setStatusFilter(filter.value);
              setPage(0);
            }}
            className={[
              "rounded-full px-3.5 py-1.5 text-sm font-semibold transition-colors",
              statusFilter === filter.value
                ? "bg-amber-500 text-white"
                : "bg-white text-ink-500 shadow-soft hover:text-ink-800",
            ].join(" ")}
          >
            {filter.label}
          </button>
        ))}
      </div>

      {status === "loading" && <Loader label="Loading notifications..." />}

      {status === "failed" && (
        <div className="mt-6">
          <ErrorState variant="server" />
        </div>
      )}

      {status === "succeeded" && rows.length === 0 && (
        <div className="mt-8">
          <EmptyState
            title="Nothing here yet"
            description="Queue a test message with the button above to see the pipeline work."
          />
        </div>
      )}

      {status === "succeeded" && rows.length > 0 && (
        <>
          <div className="mt-6 overflow-x-auto rounded-card bg-white shadow-soft">
            <table className="w-full min-w-[820px] text-left text-sm">
              <thead className="border-b border-ink-100 text-xs uppercase tracking-wide text-ink-400">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Recipient</th>
                  <th className="px-4 py-3">Template</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Attempts</th>
                  <th className="px-4 py-3">Created</th>
                  <th className="px-4 py-3">Last error</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {rows.map((row) => (
                  <tr key={row.notificationId}>
                    <td className="px-4 py-3 text-ink-500">{row.notificationId}</td>
                    <td className="px-4 py-3 font-medium text-ink-900">{row.recipient}</td>
                    <td className="px-4 py-3 text-ink-500">{row.templateCode}</td>
                    <td className="px-4 py-3">
                      <Badge variant={STATUS_VARIANT[row.status] || "info"}>{row.status}</Badge>
                    </td>
                    <td className="px-4 py-3 text-ink-500">
                      {row.attempts}/{row.maxAttempts}
                    </td>
                    <td className="px-4 py-3 text-ink-500">{formatWhen(row.createdAt)}</td>
                    <td className="max-w-[260px] truncate px-4 py-3 text-ink-500" title={row.lastError || ""}>
                      {row.lastError || "-"}
                    </td>
                    <td className="px-4 py-3">
                      {row.status === "FAILED" && (
                        <Button
                          variant="ghost"
                          icon={RotateCcw}
                          isLoading={retryingId === row.notificationId}
                          onClick={() => handleRetry(row)}
                        >
                          Retry
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="mt-4 flex items-center justify-between">
              <Button variant="ghost" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                Previous
              </Button>
              <span className="text-sm text-ink-500">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="ghost"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
