import { Search, SlidersHorizontal, X } from "lucide-react";
import Input from "../common/Input";
import { useCategories } from "../../hooks/useCategories";
import { TOUR_CODE_LABELS } from "../../constants/enums";

/**
 * Controlled filter form. Owns no state itself - the parent (SearchPage)
 * holds filter state so it can sync with the URL query string, which is
 * what makes search results shareable/bookmarkable.
 */
export default function FilterPanel({ filters, onChange, onClear }) {
  const { topLevelCategories } = useCategories();

  // Guard the obvious mistake before it reaches the API (which would just
  // return nothing and look like "no tours match").
  const dateRangeInvalid =
    Boolean(filters.startDate) && Boolean(filters.endDate) && filters.endDate < filters.startDate;

  function update(field, value) {
    onChange({ ...filters, [field]: value });
  }

  return (
    <div className="flex flex-col gap-4 rounded-card bg-white p-5 shadow-card">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm font-bold text-ink-900">
          <SlidersHorizontal className="h-4 w-4" /> Filters
        </div>
        <button onClick={onClear} className="flex items-center gap-1 text-xs font-semibold text-ink-400 hover:text-red-500">
          <X className="h-3.5 w-3.5" /> Clear all
        </button>
      </div>

      <Input
        label="Search tours"
        icon={Search}
        placeholder="Try 'Kerala', 'Europe'..."
        value={filters.tourName || ""}
        onChange={(e) => update("tourName", e.target.value)}
      />

      <div>
        <label className="text-sm font-medium text-ink-700">Category</label>
        <select
          value={filters.categoryId || ""}
          onChange={(e) => update("categoryId", e.target.value)}
          className="mt-1.5 w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
        >
          <option value="">All categories</option>
          {topLevelCategories.map((category) => (
            <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="text-sm font-medium text-ink-700">Tour type</label>
        <select
          value={filters.tourCode || ""}
          onChange={(e) => update("tourCode", e.target.value)}
          className="mt-1.5 w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
        >
          <option value="">Any</option>
          {Object.entries(TOUR_CODE_LABELS).map(([code, label]) => (
            <option key={code} value={code}>
              {label}
            </option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Input
          label="Min price"
          type="number"
          placeholder="₹0"
          value={filters.minPrice || ""}
          onChange={(e) => update("minPrice", e.target.value)}
        />
        <Input
          label="Max price"
          type="number"
          placeholder="Any"
          value={filters.maxPrice || ""}
          onChange={(e) => update("maxPrice", e.target.value)}
        />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Input
          label="Min days"
          type="number"
          placeholder="1"
          value={filters.minDuration || ""}
          onChange={(e) => update("minDuration", e.target.value)}
        />
        <Input
          label="Max days"
          type="number"
          placeholder="Any"
          value={filters.maxDuration || ""}
          onChange={(e) => update("maxDuration", e.target.value)}
        />
      </div>

      {/* BRD 3.6 "Search on Period" - lists tours that both depart on/after
          the start date and return on/before the end date. */}
      <div>
        <p className="text-sm font-medium text-ink-700">Travel period</p>
        <p className="mt-0.5 text-xs text-ink-400">Shows tours that start and finish within these dates.</p>
        <div className="mt-2 grid grid-cols-2 gap-3">
          <Input
            label="From"
            type="date"
            value={filters.startDate || ""}
            max={filters.endDate || undefined}
            onChange={(e) => update("startDate", e.target.value)}
          />
          <Input
            label="To"
            type="date"
            value={filters.endDate || ""}
            min={filters.startDate || undefined}
            onChange={(e) => update("endDate", e.target.value)}
          />
        </div>
        {dateRangeInvalid && (
          <p className="mt-1.5 text-xs text-red-600">The end date must be on or after the start date.</p>
        )}
      </div>
    </div>
  );
}
