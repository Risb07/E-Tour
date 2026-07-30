import { MessageSquareText } from "lucide-react";
import ReviewCard from "./ReviewCard";

export default function ReviewSection({ reviews }) {
  if (!reviews || reviews.length === 0) {
    return (
      <section className="bg-white rounded-3xl border border-slate-200 shadow-sm p-8">
        {/* Header */}

        <div className="flex items-center gap-3 mb-6">
          <MessageSquareText size={30} className="text-blue-600" />

          <div>
            <h2 className="text-2xl font-bold text-slate-800">
              Customer Reviews
            </h2>

            <p className="text-slate-500">No reviews yet.</p>
          </div>
        </div>

        {/* Empty State */}

        <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-12 text-center">
          <MessageSquareText size={60} className="mx-auto text-slate-400" />

          <h3 className="mt-5 text-xl font-semibold text-slate-700">
            Be the First Reviewer
          </h3>

          <p className="mt-3 text-slate-500">
            This tour hasn't received any reviews yet. Book this tour and share
            your experience!
          </p>
        </div>
      </section>
    );
  }

  return (
    <section>
      {/* Header */}

      <div className="mb-10 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <MessageSquareText size={32} className="text-blue-600" />

          <div>
            <h2 className="text-3xl font-bold text-slate-800">
              Customer Reviews
            </h2>

            <p className="mt-1 text-slate-500">
              Read experiences shared by our travelers.
            </p>
          </div>
        </div>

        <span className="rounded-full bg-blue-100 px-4 py-2 text-sm font-semibold text-blue-700">
          {reviews.length} Reviews
        </span>
      </div>

      {/* Review Grid */}

      <div className="grid gap-6 grid-cols-1 xl:grid-cols-2">
        {reviews.map((review) => (
          <ReviewCard key={review.reviewId} review={review} />
        ))}
      </div>
    </section>
  );
}
