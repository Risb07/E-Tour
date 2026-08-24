import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { PackageSearch } from "lucide-react";
import { searchTours } from "../services/tourService";
import { useCategories } from "../hooks/useCategories";
import Breadcrumb from "../components/ui/Breadcrumb";
import TourCard from "../components/ui/TourCard";
import Pagination from "../components/ui/Pagination";
import { SkeletonCard } from "../components/common/Skeleton";
import EmptyState from "../components/common/EmptyState";
import ErrorState from "../components/common/ErrorState";

const PAGE_SIZE = 9;

export default function TourListingPage() {
  const { categoryId } = useParams();
  const { getCategoryById } = useCategories();
  const category = getCategoryById(categoryId);

  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");
    searchTours({ categoryId, page, size: PAGE_SIZE })
      .then((data) => {
        if (!cancelled) {
          setResult(data);
          setStatus("succeeded");
        }
      })
      .catch(() => {
        if (!cancelled) setStatus("failed");
      });
    return () => {
      cancelled = true;
    };
  }, [categoryId, page]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <Breadcrumb items={[{ label: category?.categoryName || "Tours" }]} />
      <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">
            {category?.categoryName || "Tours"}
          </h1>
          {status === "succeeded" && (
            <p className="mt-1 text-sm text-ink-500">{result.totalElements} tours found</p>
          )}
        </div>
      </div>

      {status === "loading" && (
        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {status === "failed" && (
        <div className="mt-8">
          <ErrorState variant="server" onRetry={() => setPage((p) => p)} />
        </div>
      )}

      {status === "succeeded" && result.content.length === 0 && (
        <div className="mt-8">
          <EmptyState
            icon={PackageSearch}
            title="No tours in this category yet"
            description="Check back soon, or explore other categories."
          />
        </div>
      )}

      {status === "succeeded" && result.content.length > 0 && (
        <>
          <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {/* Search returns a flattened row with the nearest departure,
                thumbnail and rating already included - no per-card fetches. */}
            {result.content.map((row) => (
              <TourCard
                key={row.tourId}
                tour={row}
                imageUrl={row.imageUrl}
                schedule={
                  row.departureDate
                    ? {
                        departureDate: row.departureDate,
                        returnDate: row.returnDate,
                        availableSeats: row.availableSeats,
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
  );
}
