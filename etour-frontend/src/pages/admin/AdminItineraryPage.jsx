import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, X } from "lucide-react";
import { fetchAllTours } from "../../services/tourService";
import {
  fetchItinerary,
  addItineraryDay,
  updateItineraryDay,
  deleteItineraryDay,
} from "../../services/itineraryService";
import { useToast } from "../../hooks/useToast";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Loader from "../../components/common/Loader";
import EmptyState from "../../components/common/EmptyState";

const EMPTY_FORM = { dayNumber: "", title: "", description: "" };

export default function AdminItineraryPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [days, setDays] = useState([]);
  const [status, setStatus] = useState("loading");
  const [editingId, setEditingId] = useState(null); // null = not editing, "new" = add form open
  const [form, setForm] = useState(EMPTY_FORM);
  const [isSaving, setIsSaving] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    fetchAllTours()
      .then((data) => {
        setTours(data);
        if (data.length > 0) setSelectedTourId(String(data[0].tourId));
      })
      .catch(() => showToast("Couldn't load tours.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function loadDays(tourId) {
    if (!tourId) return;
    setStatus("loading");
    fetchItinerary(tourId)
      .then((data) => {
        setDays(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(() => {
    if (selectedTourId) loadDays(selectedTourId);
  }, [selectedTourId]);

  function openAddForm() {
    setForm({ dayNumber: days.length + 1, title: "", description: "" });
    setEditingId("new");
  }

  function openEditForm(day) {
    setForm({ dayNumber: day.dayNumber, title: day.title || "", description: day.description || "" });
    setEditingId(day.itineraryId);
  }

  function closeForm() {
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setIsSaving(true);
    const dto = {
      dayNumber: Number(form.dayNumber),
      title: form.title,
      description: form.description,
    };
    try {
      if (editingId === "new") {
        await addItineraryDay(selectedTourId, dto);
        showToast("Itinerary day added.", "success");
      } else {
        await updateItineraryDay(selectedTourId, editingId, dto);
        showToast("Itinerary day updated.", "success");
      }
      closeForm();
      loadDays(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't save itinerary day.", "error");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete(day) {
    if (!window.confirm(`Delete Day ${day.dayNumber} - "${day.title}"?`)) return;
    try {
      await deleteItineraryDay(selectedTourId, day.itineraryId);
      showToast("Itinerary day deleted.", "success");
      loadDays(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't delete itinerary day.", "error");
    }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Itinerary</h1>
          <p className="mt-1 text-sm text-ink-500">Manage day-by-day itinerary content shown on the tour page.</p>
        </div>
        <Button icon={Plus} onClick={openAddForm} disabled={!selectedTourId}>
          Add Day
        </Button>
      </div>

      <div className="mt-6 max-w-sm">
        <label className="text-sm font-medium text-ink-700">Tour</label>
        <select
          value={selectedTourId}
          onChange={(e) => setSelectedTourId(e.target.value)}
          className="mt-1.5 w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:border-amber-400 focus:outline-none"
        >
          {tours.map((tour) => (
            <option key={tour.tourId} value={tour.tourId}>
              {tour.title}
            </option>
          ))}
        </select>
      </div>

      {editingId && (
        <form onSubmit={handleSubmit} className="mt-6 rounded-card bg-white p-5 shadow-soft">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-base font-bold text-ink-900">
              {editingId === "new" ? "Add itinerary day" : "Edit itinerary day"}
            </h2>
            <button type="button" onClick={closeForm} className="text-ink-400 hover:text-ink-700">
              <X className="h-4 w-4" />
            </button>
          </div>
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-[120px_1fr]">
            <Input
              label="Day #"
              type="number"
              required
              value={form.dayNumber}
              onChange={(e) => setForm((f) => ({ ...f, dayNumber: e.target.value }))}
            />
            <Input
              label="Title"
              required
              value={form.title}
              onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            />
          </div>
          <div className="mt-4 flex flex-col gap-1.5">
            <label className="text-sm font-medium text-ink-700">Description</label>
            <textarea
              rows={3}
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              className="w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:border-amber-400 focus:outline-none"
            />
          </div>
          <div className="mt-4 flex justify-end gap-2">
            <Button variant="ghost" onClick={closeForm}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isSaving}>
              Save
            </Button>
          </div>
        </form>
      )}

      {status === "loading" && <Loader label="Loading itinerary..." />}

      {status === "succeeded" && days.length === 0 && (
        <div className="mt-8">
          <EmptyState title="No itinerary days yet" description="Add the first day using the button above." />
        </div>
      )}

      {status === "succeeded" && days.length > 0 && (
        <div className="mt-6 divide-y divide-ink-100 rounded-card bg-white shadow-soft">
          {days
            .slice()
            .sort((a, b) => a.dayNumber - b.dayNumber)
            .map((day) => (
              <div key={day.itineraryId} className="flex items-start justify-between gap-4 p-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-amber-600">Day {day.dayNumber}</p>
                  <p className="mt-1 font-display font-bold text-ink-900">{day.title}</p>
                  <p className="mt-1 text-sm text-ink-500">{day.description}</p>
                </div>
                <div className="flex shrink-0 gap-1">
                  <button
                    onClick={() => openEditForm(day)}
                    aria-label="Edit day"
                    className="rounded-lg p-2 text-ink-400 hover:bg-ink-50 hover:text-ink-800"
                  >
                    <Pencil className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(day)}
                    aria-label="Delete day"
                    className="rounded-lg p-2 text-ink-400 hover:bg-red-50 hover:text-red-600"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
        </div>
      )}
    </div>
  );
}
