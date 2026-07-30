import { Star, MessageSquare, ThumbsUp } from "lucide-react";

export default function ReviewSummary({ reviewSummary }) {
  if (!reviewSummary) return null;

  const rating = Number(reviewSummary.averageRating || 0);

  const percentage = (rating / 5) * 100;

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
      {/* Header */}

      <div className="flex items-center gap-3">
        <Star size={30} className="fill-yellow-400 text-yellow-400" />

        <div>
          <h2 className="text-3xl font-bold text-slate-800">
            Traveler Reviews
          </h2>

          <p className="text-slate-500">
            Based on verified customer experiences
          </p>
        </div>
      </div>

      {/* Summary */}

      <div className="mt-8 grid gap-8 lg:grid-cols-2">
        {/* Left */}

        <div className="flex items-center gap-6">
          <div className="flex h-28 w-28 flex-col items-center justify-center rounded-full bg-blue-50">
            <span className="text-4xl font-bold text-blue-700">
              {rating.toFixed(1)}
            </span>

            <span className="text-sm text-slate-500">out of 5</span>
          </div>

          <div>
            <div className="flex">
              {[1, 2, 3, 4, 5].map((item) => (
                <Star
                  key={item}
                  size={22}
                  className={
                    item <= Math.round(rating)
                      ? "fill-yellow-400 text-yellow-400"
                      : "text-slate-300"
                  }
                />
              ))}
            </div>

            <p className="mt-3 text-slate-600">
              {reviewSummary.totalReviews} verified reviews
            </p>

            <div className="mt-3 flex items-center gap-2 text-emerald-600">
              <ThumbsUp size={18} />

              <span className="font-medium">Highly Recommended</span>
            </div>
          </div>
        </div>

        {/* Right */}

        <div>
          <div className="mb-2 flex justify-between">
            <span className="text-sm text-slate-500">Overall Rating</span>

            <span className="font-semibold">{rating.toFixed(1)} / 5</span>
          </div>

          <div className="h-4 overflow-hidden rounded-full bg-slate-200">
            <div
              className="h-full rounded-full bg-yellow-400 transition-all duration-500"
              style={{
                width: `${percentage}%`,
              }}
            />
          </div>

          <div className="mt-6 rounded-2xl bg-slate-50 p-5">
            <div className="flex items-center gap-2">
              <MessageSquare size={18} className="text-blue-600" />

              <span className="font-semibold text-slate-700">
                Customer Feedback
              </span>
            </div>

            <p className="mt-3 text-sm leading-6 text-slate-600">
              Travelers consistently appreciate this tour for its organization,
              itinerary, and overall experience. Ratings are collected from
              verified bookings.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
