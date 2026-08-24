import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import TourPicker from "../../components/admin/TourPicker";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import { fetchAllTours, fetchSchedulesForTour, createSchedule, updateSchedule, deleteSchedule } from "../../services/tourService";
import { formatDate } from "../../utils/format";

const EMPTY_FORM = { departureDate: "", returnDate: "", availableSeats: "", price: "" };

export default function AdminSchedulesPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: () => (selectedTourId ? fetchSchedulesForTour(selectedTourId) : Promise.resolve([])),
      create: (payload) => createSchedule(selectedTourId, payload),
      update: (id, payload) => updateSchedule(id, selectedTourId, payload),
      remove: deleteSchedule,
    }),
    [selectedTourId]
  );
  const { items: schedules, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Schedule",
  });

  useEffect(() => {
    fetchAllTours()
      .then((data) => {
        setTours(data);
        if (data.length > 0) setSelectedTourId(String(data[0].tourId));
      })
      .catch(() => showToast("Couldn't load tours.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(schedule) {
    setForm({
      departureDate: schedule.departureDate || "",
      returnDate: schedule.returnDate || "",
      availableSeats: schedule.availableSeats ?? "",
      price: schedule.price ?? "",
    });
    setEditing(schedule.scheduleId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      departureDate: form.departureDate,
      returnDate: form.returnDate,
      availableSeats: Number(form.availableSeats),
      price: Number(form.price),
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(schedule) {
    if (!window.confirm(`Delete schedule departing ${formatDate(schedule.departureDate)}?`)) return;
    await removeItem(schedule.scheduleId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Tour Schedules</h1>
          <p className="mt-1 text-sm text-ink-500">Manage departure dates, seats and prices per tour.</p>
        </div>
        <Button icon={Plus} onClick={openCreate} disabled={!selectedTourId}>
          Add Schedule
        </Button>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "scheduleId", label: "ID", className: "w-16" },
            {
              key: "departureDate",
              label: "Departure",
              render: (s) => formatDate(s.departureDate),
            },
            { key: "returnDate", label: "Return", render: (s) => formatDate(s.returnDate) },
            {
              key: "availableSeats",
              label: "Seats",
              render: (s) => (
                <Badge variant={s.availableSeats > 0 ? "success" : "danger"}>
                  {s.availableSeats}
                </Badge>
              ),
            },
            {
              key: "price",
              label: "Price",
              render: (s) =>
                new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(s.price),
            },
          ]}
          rows={schedules}
          rowKey="scheduleId"
          searchKeys={["departureDate", "returnDate"]}
          searchPlaceholder="Search schedules..."
          isLoading={status === "loading"}
          emptyTitle={selectedTourId ? "No schedules for this tour" : "Select a tour to view schedules"}
          emptyDescription="Add departure dates using the button above."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add schedule" : "Edit schedule"} onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Departure date"
            type="date"
            required
            value={form.departureDate}
            onChange={(v) => setForm((f) => ({ ...f, departureDate: v }))}
          />
          <Field
            label="Return date"
            type="date"
            required
            value={form.returnDate}
            onChange={(v) => setForm((f) => ({ ...f, returnDate: v }))}
          />
          <Field
            label="Available seats"
            type="number"
            required={editing === "new"}
            min={0}
            value={form.availableSeats}
            onChange={(v) => setForm((f) => ({ ...f, availableSeats: v }))}
            // Seats are a live balance the booking system maintains, so they
            // can only be set when the schedule is created. Editing them here
            // would overwrite bookings made since this form was opened.
            disabled={editing !== "new"}
            hint={
              editing === "new"
                ? undefined
                : "Seat availability updates automatically as bookings are made and cancelled."
            }
          />
          <Field
            label="Price (₹)"
            type="number"
            required
            min={0}
            step="0.01"
            value={form.price}
            onChange={(v) => setForm((f) => ({ ...f, price: v }))}
          />
          <div className="mt-2 flex justify-end gap-2 sm:col-span-2">
            <Button variant="ghost" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isMutating}>
              Save
            </Button>
          </div>
        </form>
      </AdminModal>
    </div>
  );
}
