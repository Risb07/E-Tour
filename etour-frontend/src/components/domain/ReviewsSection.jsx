import { useState } from "react";
import { Star, MessageSquareText } from "lucide-react";
import { addMyReview } from "../../services/reviewService";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import { ROLES } from "../../constants/roles";
import RatingStars from "../ui/RatingStars";
import Button from "../common/Button";
import EmptyState from "../common/EmptyState";

export default function ReviewsSection({ tourId, reviewSummary, initialReviews }) {
  const [reviews, setReviews] = useState(initialReviews || []);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { isAuthenticated, user } = useAuth();
  const { showToast } = useToast();

  async function handleSubmit(event) {
    event.preventDefault();
    if (!comment.trim()) {
      showToast("Please add a comment before submitting.", "error");
      return;
    }
    setIsSubmitting(true);
    try {
      const newReview = await addMyReview(tourId, { rating, comment });
      setReviews((current) => [newReview, ...current]);
      setComment("");
      setRating(5);
      showToast("Thanks for your review!", "success");
    } catch (err) {
      showToast(err.message || "Couldn't submit your review.", "error");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div>
      <div className="flex items-center gap-4 rounded-card bg-white p-5 shadow-soft">
        <div className="text-center">
          <p className="font-display text-3xl font-bold text-ink-900">
            {(reviewSummary?.averageRating || 0).toFixed(1)}
          </p>
          <RatingStars rating={reviewSummary?.averageRating || 0} size={14} />
        </div>
        <div className="text-sm text-ink-500">
          Based on {reviewSummary?.totalReviews || 0} {reviewSummary?.totalReviews === 1 ? "review" : "reviews"}
        </div>
      </div>

      {isAuthenticated && user.role === ROLES.CUSTOMER && (
        <form onSubmit={handleSubmit} className="mt-6 rounded-card bg-white p-5 shadow-soft">
          <h4 className="font-semibold text-ink-900">Write a review</h4>
          <div className="mt-3 flex gap-1">
            {[1, 2, 3, 4, 5].map((star) => (
              <button key={star} type="button" onClick={() => setRating(star)} aria-label={`Rate ${star} stars`}>
                <Star
                  className={`h-6 w-6 ${star <= rating ? "fill-amber-400 text-amber-400" : "fill-ink-100 text-ink-200"}`}
                />
              </button>
            ))}
          </div>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Share how your trip went..."
            rows={3}
            className="mt-3 w-full rounded-xl border border-ink-200 px-3.5 py-2.5 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
          />
          <Button type="submit" isLoading={isSubmitting} className="mt-3">
            Submit review
          </Button>
        </form>
      )}

      <div className="mt-6 flex flex-col gap-4">
        {reviews.length === 0 ? (
          <EmptyState icon={MessageSquareText} title="No reviews yet" description="Be the first to share your experience." />
        ) : (
          reviews.map((review) => (
            <div key={review.reviewId} className="rounded-card bg-white p-5 shadow-soft">
              <div className="flex items-center justify-between">
                <p className="font-semibold text-ink-900">{review.customer?.fullName || "Traveller"}</p>
                <RatingStars rating={review.rating} size={14} />
              </div>
              <p className="mt-2 text-sm text-ink-600">{review.comment}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
