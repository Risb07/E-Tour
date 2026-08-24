import { Star } from "lucide-react";

/** Read-only star rating display. `size` controls icon size in px. */
export default function RatingStars({ rating = 0, totalReviews, size = 16 }) {
  const rounded = Math.round(rating);

  return (
    <div className="flex items-center gap-1">
      <div className="flex" aria-label={`Rated ${rating.toFixed(1)} out of 5`}>
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            width={size}
            height={size}
            className={star <= rounded ? "fill-amber-400 text-amber-400" : "fill-ink-100 text-ink-200"}
          />
        ))}
      </div>
      {typeof totalReviews === "number" && (
        <span className="text-xs font-medium text-ink-500">
          {rating.toFixed(1)} ({totalReviews})
        </span>
      )}
    </div>
  );
}
