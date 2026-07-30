import { Star, CalendarDays, UserCircle2 } from "lucide-react";

export default function ReviewCard({ review }) {
  const reviewDate = new Date(review.reviewDate);

  const fullName = review.user
    ? `${review.user.firstName} ${review.user.lastName}`
    : "Anonymous";

  return (
    <article className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg">
      {/* Header */}

      <div className="flex items-start justify-between">
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-full bg-blue-100">
            <UserCircle2 size={34} className="text-blue-600" />
          </div>

          <div>
            <h3 className="text-lg font-semibold text-slate-800">{fullName}</h3>

            <div className="mt-1 flex items-center gap-2 text-sm text-slate-500">
              <CalendarDays size={15} />

              {reviewDate.toLocaleDateString("en-IN", {
                day: "numeric",
                month: "short",
                year: "numeric",
              })}
            </div>
          </div>
        </div>

        {/* Rating */}

        <div className="flex items-center gap-1">
          {[1, 2, 3, 4, 5].map((star) => (
            <Star
              key={star}
              size={18}
              className={
                star <= review.rating
                  ? "fill-yellow-400 text-yellow-400"
                  : "text-slate-300"
              }
            />
          ))}
        </div>
      </div>

      {/* Review */}

      <div className="mt-6">
        <p className="leading-7 text-slate-600">{review.comment}</p>
      </div>

      {/* Footer */}

      <div className="mt-6 flex items-center justify-between border-t border-slate-200 pt-4">
        <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700">
          Verified Traveler
        </span>

        <span className="text-sm font-medium text-blue-600">
          {review.rating}/5
        </span>
      </div>
    </article>
  );
}
