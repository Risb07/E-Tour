/**
 * Tour picker used by tour-scoped admin pages (schedules, costs, media).
 * Matches the select styling used in AdminItineraryPage.
 */
export default function TourPicker({ tours, selectedTourId, onChange, placeholder = "Select a tour..." }) {
  return (
    <div className="max-w-sm">
      <label className="text-sm font-medium text-ink-700">Tour</label>
      <select
        value={selectedTourId}
        onChange={(e) => onChange(e.target.value)}
        className="mt-1.5 w-full cursor-pointer rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:border-amber-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
      >
        <option value="">{placeholder}</option>
        {tours.map((tour) => (
          <option key={tour.tourId} value={tour.tourId}>
            {tour.title}
          </option>
        ))}
      </select>
    </div>
  );
}
