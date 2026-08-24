import { useEffect, useMemo, useState } from "react";
import { Trash2, MessageSquareText } from "lucide-react";
import { fetchAllReviews, deleteReviewAsAdmin } from "../../services/reviewService";
import { fetchAllTours } from "../../services/tourService";
import { useToast } from "../../hooks/useToast";
import Loader from "../../components/common/Loader";
import EmptyState from "../../components/common/EmptyState";
import ErrorState from "../../components/common/ErrorState";
import RatingStars from "../../components/ui/RatingStars";
import Badge from "../../components/ui/Badge";
import TourPicker from "../../components/admin/TourPicker";

/**
 * Review moderation. Reviews are written by customers on the tour page - an
 * admin cannot author one here, only read and remove. Deleting a review also
 * changes the tour's average rating, since that is derived from these rows.
 */
export default function AdminReviewsPage() {
  const [reviews, setReviews] = useState([]);
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [status, setStatus] = useState("loading");
  const { showToast } = useToast();

  function load() {
    setStatus("loading");
    fetchAllReviews()
      .then((data) => {
        setReviews(Array.isArray(data) ? data : []);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(() => {
    load();
    fetchAllTours()
      .then(setTours)
      .catch(() => setTours([]));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Filtering client-side: the review set is small and already loaded, so a
  // per-tour request would only add a round trip.
  const visible = useMemo(() => {
    if (!selectedTourId) return reviews;
    return reviews.filter((r) => String(r.tour?.tourId) === String(selectedTourId));
  }, [reviews, selectedTourId]);

  // Review.tour is serialised with @JsonIgnoreProperties({"title", ...}) so the
  // payload stays lean for the public tour page - the title has to come from
  // the tour list this page already loads for its picker.
  const tourTitleById = useMemo(
    () => new Map(tours.map((t) => [String(t.tourId), t.title])),
    [tours]
  );

  async function handleDelete(review) {
    const who = review.customer?.fullName || "this traveller";
    if (!window.confirm(`Delete the ${review.rating}-star review from ${who}? This also updates the tour's average rating.`)) {
      return;
    }
    try {
      await deleteReviewAsAdmin(review.reviewId);
      showToast("Review deleted.", "success");
      load();
    } catch (err) {
      showToast(err.message || "Couldn't delete this review.", "error");
    }
  }

  return (
    <div>
      <div>
        <h1 className="font-display text-2xl font-bold text-ink-900">Reviews</h1>
        <p className="mt-1 text-sm text-ink-500">
          Customer reviews shown on the tour page. Reviews are written by travellers - moderate them here by removing
          any that break your guidelines.
        </p>
      </div>

      <div className="mt-6">
        <TourPicker
          tours={tours}
          selectedTourId={selectedTourId}
          onChange={setSelectedTourId}
          placeholder="All tours"
        />
      </div>

      {status === "loading" && <Loader label="Loading reviews..." />}

      {status === "failed" && (
        <div className="mt-8">
          <ErrorState variant="server" message="We couldn't load reviews." />
        </div>
      )}

      {status === "succeeded" && (
        <>
          <p className="mt-6 text-sm text-ink-500">
            {visible.length} review{visible.length === 1 ? "" : "s"}
            {selectedTourId ? " for this tour" : " across all tours"}
          </p>

          {visible.length === 0 ? (
            <EmptyState
              icon={MessageSquareText}
              title="No reviews yet"
              description={
                selectedTourId
                  ? "No traveller has reviewed this tour yet."
                  : "Reviews appear here once travellers start writing them."
              }
            />
          ) : (
            <div className="mt-4 flex flex-col gap-3">
              {visible.map((review) => {
                const tourTitle = tourTitleById.get(String(review.tour?.tourId));
                return (
                <div key={review.reviewId} className="rounded-card bg-white p-5 shadow-soft">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <p className="font-semibold text-ink-900">
                          {review.customer?.fullName || "Traveller"}
                        </p>
                        <RatingStars rating={review.rating} size={14} />
                      </div>
                      {tourTitle && (
                        <Badge variant="info" className="mt-2">
                          {tourTitle}
                        </Badge>
                      )}
                      <p className="mt-2 text-sm text-ink-600">{review.comment}</p>
                    </div>
                    <button
                      onClick={() => handleDelete(review)}
                      aria-label="Delete review"
                      className="shrink-0 rounded-lg p-2 text-ink-400 hover:bg-red-50 hover:text-red-600"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}
