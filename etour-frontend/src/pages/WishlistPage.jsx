import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Heart, Clock, Trash2 } from "lucide-react";
import { useWishlist } from "../hooks/useWishlist";
import { useToast } from "../hooks/useToast";
import { formatCurrency } from "../utils/format";
import { TOUR_CODE_LABELS } from "../constants/enums";
import { ROUTE_PATHS } from "../constants/routes";
import Badge from "../components/ui/Badge";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";
import ErrorState from "../components/common/ErrorState";

export default function WishlistPage() {
  const { items, status, toggle, reload } = useWishlist();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [removingId, setRemovingId] = useState(null);

  async function handleRemove(tourId) {
    setRemovingId(tourId);
    try {
      await toggle(tourId);
      showToast("Removed from your wishlist.", "success");
    } catch (err) {
      showToast(err.message || "Couldn't remove this tour.", "error");
    } finally {
      setRemovingId(null);
    }
  }

  if (status === "loading") return <Loader label="Loading your wishlist..." />;

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">My Wishlist</h1>
      <p className="mt-1 text-sm text-ink-500">Tours you have saved for later.</p>

      {status === "failed" && <ErrorState variant="server" onRetry={reload} />}

      {status !== "failed" && items.length === 0 && (
        <EmptyState
          icon={Heart}
          title="Nothing saved yet"
          description="Tap the heart on any tour to save it here."
          primaryAction={{ label: "Explore tours", onClick: () => navigate(ROUTE_PATHS.SEARCH) }}
        />
      )}

      {items.length > 0 && (
        <div className="mt-6 flex flex-col gap-3">
          {items.map((item) => (
            <div
              key={item.wishlistItemId}
              className="flex flex-wrap items-center justify-between gap-4 rounded-card bg-white p-5 shadow-soft"
            >
              <div>
                <Link
                  to={ROUTE_PATHS.tourDetails(item.tourId)}
                  className="font-semibold text-ink-900 hover:text-amber-600"
                >
                  {item.tourTitle}
                </Link>
                <div className="mt-1.5 flex flex-wrap items-center gap-3 text-xs text-ink-500">
                  {item.tourCode && (
                    <Badge variant="accent">{TOUR_CODE_LABELS[item.tourCode] || item.tourCode}</Badge>
                  )}
                  <span className="flex items-center gap-1">
                    <Clock className="h-3.5 w-3.5" /> {item.durationDays} days
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <span className="font-display text-lg font-bold text-ink-900">
                  {formatCurrency(item.basePrice)}
                </span>
                <Link to={ROUTE_PATHS.tourDetails(item.tourId)}>
                  <Button size="sm">View details</Button>
                </Link>
                <Button
                  variant="ghost"
                  size="sm"
                  icon={Trash2}
                  isLoading={removingId === item.tourId}
                  onClick={() => handleRemove(item.tourId)}
                >
                  Remove
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
