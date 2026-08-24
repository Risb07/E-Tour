import { useEffect, useState } from "react";
import { Plus, Trash2, X } from "lucide-react";
import { fetchAllTours } from "../../services/tourService";
import {
  fetchStayMeals,
  addStayMeal,
  deleteStayMeal,
  fetchLocations,
} from "../../services/stayMealService";
import { useToast } from "../../hooks/useToast";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import Loader from "../../components/common/Loader";
import EmptyState from "../../components/common/EmptyState";
import Badge from "../../components/ui/Badge";

const EMPTY_FORM = {
  dayNumber: "",
  hotelName: "",
  locationId: "",
  breakfast: false,
  lunch: false,
  dinner: false,
};

/**
 * BRD "Stay & Meals" tab - one row per day of the tour, naming the hotel and
 * which meals are included. The backend exposes add and delete only (there is
 * no update endpoint), so editing a day means deleting it and adding it again.
 */
export default function AdminStayMealsPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [locations, setLocations] = useState([]);
  const [entries, setEntries] = useState([]);
  const [status, setStatus] = useState("loading");
  const [isFormOpen, setIsFormOpen] = useState(false);
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
    // The location list is optional decoration on the form - a deployment with
    // no locations seeded simply doesn't get the dropdown.
    fetchLocations()
      .then((data) => setLocations(Array.isArray(data) ? data : []))
      .catch(() => setLocations([]));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function loadEntries(tourId) {
    if (!tourId) return;
    setStatus("loading");
    fetchStayMeals(tourId)
      .then((data) => {
        setEntries(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(() => {
    if (selectedTourId) loadEntries(selectedTourId);
  }, [selectedTourId]);

  function openAddForm() {
    // Default to the next unused day so adding a run of days is one field less
    // of typing each time.
    const nextDay = entries.reduce((max, e) => Math.max(max, e.dayNumber || 0), 0) + 1;
    setForm({ ...EMPTY_FORM, dayNumber: nextDay });
    setIsFormOpen(true);
  }

  function closeForm() {
    setIsFormOpen(false);
    setForm(EMPTY_FORM);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setIsSaving(true);
    try {
      await addStayMeal(selectedTourId, {
        dayNumber: Number(form.dayNumber),
        hotelName: form.hotelName || null,
        locationId: form.locationId ? Number(form.locationId) : null,
        breakfast: form.breakfast,
        lunch: form.lunch,
        dinner: form.dinner,
      });
      showToast("Stay & meal entry added.", "success");
      closeForm();
      loadEntries(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't save stay & meal entry.", "error");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete(entry) {
    const label = entry.hotelName || entry.locationName || "this stay";
    if (!window.confirm(`Delete Day ${entry.dayNumber} - "${label}"?`)) return;
    try {
      await deleteStayMeal(selectedTourId, entry.stayMealId);
      showToast("Stay & meal entry deleted.", "success");
      loadEntries(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't delete stay & meal entry.", "error");
    }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Stay &amp; Meals</h1>
          <p className="mt-1 text-sm text-ink-500">
            Manage the hotel and included meals shown on the tour page, day by day.
          </p>
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

      {isFormOpen && (
        <form onSubmit={handleSubmit} className="mt-6 rounded-card bg-white p-5 shadow-soft">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-base font-bold text-ink-900">Add stay &amp; meals</h2>
            <button type="button" onClick={closeForm} className="text-ink-400 hover:text-ink-700">
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-[120px_1fr]">
            <Input
              label="Day #"
              type="number"
              min={1}
              required
              value={form.dayNumber}
              onChange={(e) => setForm((f) => ({ ...f, dayNumber: e.target.value }))}
            />
            <Input
              label="Hotel name"
              value={form.hotelName}
              onChange={(e) => setForm((f) => ({ ...f, hotelName: e.target.value }))}
              placeholder="e.g. Hotel Sea Breeze"
            />
          </div>

          {locations.length > 0 && (
            <div className="mt-4 flex max-w-sm flex-col gap-1.5">
              <label className="text-sm font-medium text-ink-700">Location</label>
              <select
                value={form.locationId}
                onChange={(e) => setForm((f) => ({ ...f, locationId: e.target.value }))}
                className="w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:border-amber-400 focus:outline-none"
              >
                <option value="">No location</option>
                {locations.map((loc) => (
                  <option key={loc.locationId} value={loc.locationId}>
                    {loc.locationName}
                    {loc.stateProvince ? `, ${loc.stateProvince}` : ""}
                  </option>
                ))}
              </select>
            </div>
          )}

          <fieldset className="mt-4">
            <legend className="text-sm font-medium text-ink-700">Meals included</legend>
            <div className="mt-2 flex flex-wrap gap-5">
              {[
                { key: "breakfast", label: "Breakfast" },
                { key: "lunch", label: "Lunch" },
                { key: "dinner", label: "Dinner" },
              ].map((meal) => (
                <label key={meal.key} className="flex items-center gap-2 text-sm text-ink-700">
                  <input
                    type="checkbox"
                    checked={form[meal.key]}
                    onChange={(e) => setForm((f) => ({ ...f, [meal.key]: e.target.checked }))}
                    className="h-4 w-4 rounded border-ink-300 text-amber-500 focus:ring-amber-400"
                  />
                  {meal.label}
                </label>
              ))}
            </div>
          </fieldset>

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

      {status === "loading" && <Loader label="Loading stay & meals..." />}

      {status === "succeeded" && entries.length === 0 && (
        <div className="mt-8">
          <EmptyState
            title="No stay & meal entries yet"
            description="Add the first day using the button above."
          />
        </div>
      )}

      {status === "succeeded" && entries.length > 0 && (
        <div className="mt-6 divide-y divide-ink-100 rounded-card bg-white shadow-soft">
          {entries
            .slice()
            .sort((a, b) => a.dayNumber - b.dayNumber)
            .map((entry) => (
              <div key={entry.stayMealId} className="flex items-start justify-between gap-4 p-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-amber-600">
                    Day {entry.dayNumber}
                  </p>
                  <p className="mt-1 font-display font-bold text-ink-900">
                    {entry.hotelName || entry.locationName || "Stay TBD"}
                  </p>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {entry.breakfast && <Badge variant="success">Breakfast</Badge>}
                    {entry.lunch && <Badge variant="success">Lunch</Badge>}
                    {entry.dinner && <Badge variant="success">Dinner</Badge>}
                    {!entry.breakfast && !entry.lunch && !entry.dinner && (
                      <Badge variant="info">No meals included</Badge>
                    )}
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(entry)}
                  aria-label="Delete stay & meal entry"
                  className="shrink-0 rounded-lg p-2 text-ink-400 hover:bg-red-50 hover:text-red-600"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
        </div>
      )}
    </div>
  );
}
