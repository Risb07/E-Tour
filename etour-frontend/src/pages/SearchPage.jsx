import { useEffect, useState, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { PackageSearch, X } from "lucide-react";
import { searchTours } from "../services/tourService";
import { useDebounce } from "../hooks/useDebounce";
import FilterPanel from "../components/ui/FilterPanel";
import TourCard from "../components/ui/TourCard";
import Pagination from "../components/ui/Pagination";
import { SkeletonCard } from "../components/common/Skeleton";
import EmptyState from "../components/common/EmptyState";
import ErrorState from "../components/common/ErrorState";

const PAGE_SIZE = 9;

// Human-readable names for the active-filter chips.
const FILTER_LABELS = {
  tourName: "Name",
  categoryId: "Category",
  tourCode: "Code",
  minPrice: "Min price",
  maxPrice: "Max price",
  minDuration: "Min days",
  maxDuration: "Max days",
  startDate: "From",
  endDate: "To",
};

const EMPTY_FILTERS = {
  tourName: "",
  categoryId: "",
  tourCode: "",
  minPrice: "",
  maxPrice: "",
  minDuration: "",
  maxDuration: "",
  startDate: "",
  endDate: "",
};

function filtersFromParams(searchParams) {
  return Object.keys(EMPTY_FILTERS).reduce(
    (acc, key) => ({ ...acc, [key]: searchParams.get(key) || "" }),
    {}
  );
}

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState(() => filtersFromParams(searchParams));
  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [status, setStatus] = useState("loading");

  const debouncedTourName = useDebounce(filters.tourName, 400);

  // Keep the URL in sync so results are shareable/bookmarkable.
  useEffect(() => {
    const params = {};
    Object.entries(filters).forEach(([key, value]) => {
      if (value) params[key] = value;
    });
    setSearchParams(params, { replace: true });
  }, [filters, setSearchParams]);

  const runSearch = useCallback(() => {
    setStatus("loading");
    searchTours({ ...filters, tourName: debouncedTourName, page, size: PAGE_SIZE })
      .then((data) => {
        setResult(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }, [filters, debouncedTourName, page]);

  useEffect(() => {
    runSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    debouncedTourName,
    filters.categoryId,
    filters.tourCode,
    filters.minPrice,
    filters.maxPrice,
    filters.minDuration,
    filters.maxDuration,
    filters.startDate,
    filters.endDate,
    page,
  ]);

  function handleFiltersChange(next) {
    setFilters(next);
    setPage(0);
  }

  function handleClear() {
    setFilters(EMPTY_FILTERS);
    setPage(0);
  }

  // Chips for whatever is currently narrowing the results, so the active
  // filters stay visible after scrolling past the sidebar on mobile.
  const activeChips = Object.entries(filters)
    .filter(([, value]) => value !== "" && value != null)
    .map(([key, value]) => ({ key, label: `${FILTER_LABELS[key] || key}: ${value}` }));

  function clearFilter(key) {
    handleFiltersChange({ ...filters, [key]: "" });
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">Explore tours</h1>
      <p className="mt-1 text-sm text-ink-500" aria-live="polite">
        {status === "succeeded"
          ? `${result.totalElements} tour${result.totalElements === 1 ? "" : "s"} match your filters`
          : "Searching..."}
      </p>

      {activeChips.length > 0 && (
        <div className="mt-4 flex flex-wrap items-center gap-2">
          {activeChips.map((chip) => (
            <button
              key={chip.key}
              onClick={() => clearFilter(chip.key)}
              className="inline-flex items-center gap-1.5 rounded-pill bg-ink-100 py-1.5 pl-3 pr-2 text-xs font-medium text-ink-700 transition-colors hover:bg-ink-200"
            >
              {chip.label}
              <X className="h-3.5 w-3.5" aria-hidden="true" />
              <span className="sr-only">Remove filter</span>
            </button>
          ))}
          <button
            onClick={handleClear}
            className="rounded-pill px-3 py-1.5 text-xs font-semibold text-amber-600 transition-colors hover:bg-amber-50"
          >
            Clear all
          </button>
        </div>
      )}

      <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-[280px_1fr]">
        <aside className="lg:sticky lg:top-20 lg:max-h-[calc(100vh-6rem)] lg:self-start lg:overflow-y-auto">
          <FilterPanel filters={filters} onChange={handleFiltersChange} onClear={handleClear} />
        </aside>

        <div>
          {status === "loading" && (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <SkeletonCard key={i} />
              ))}
            </div>
          )}

          {status === "failed" && <ErrorState variant="server" onRetry={runSearch} />}

          {status === "succeeded" && result.content.length === 0 && (
            <EmptyState
              icon={PackageSearch}
              title="No tours match those filters"
              description="Try widening your price range or clearing a filter."
              primaryAction={{ label: "Clear filters", onClick: handleClear }}
            />
          )}

          {status === "succeeded" && result.content.length > 0 && (
            <>
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
                {/* The search endpoint returns a flattened result row that
                    already includes the nearest departure, thumbnail and
                    rating, so no per-card follow-up requests are needed. */}
                {result.content.map((row) => (
                  <TourCard
                    key={row.tourId}
                    tour={row}
                    imageUrl={row.imageUrl}
                    schedule={
                      row.departureDate
                        ? {
                            scheduleId: row.scheduleId,
                            departureDate: row.departureDate,
                            returnDate: row.returnDate,
                            availableSeats: row.availableSeats,
                            price: row.schedulePrice,
                          }
                        : undefined
                    }
                    rating={row.totalReviews > 0 ? row.averageRating : undefined}
                    totalReviews={row.totalReviews}
                  />
                ))}
              </div>
              <div className="mt-10">
                <Pagination page={page} totalPages={result.totalPages} onPageChange={setPage} />
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
