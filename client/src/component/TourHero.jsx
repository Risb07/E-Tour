import {
  CalendarDays,
  Clock3,
  IndianRupee,
  Star,
  BadgeCheck,
  MapPin,
} from "lucide-react";

export default function TourHero({ tour, reviewSummary }) {
  const startingPrice = tour.basePrice;

  return (
    <section className="relative">
      {/* Hero Image */}

      <div className="relative h-[430px] overflow-hidden rounded-3xl">
        <img
          src={
            tour.image ||
            `https://images.unsplash.com/featured/?${encodeURIComponent(
              tour.title,
            )}`
          }
          alt={tour.title}
          className="h-full w-full object-cover"
        />

        {/* Overlay */}

        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/35 to-transparent" />

        {/* Status */}

        <div className="absolute left-8 top-8 flex gap-3">
          <span className="rounded-full bg-white/90 px-5 py-2 text-sm font-semibold text-slate-700 backdrop-blur">
            {tour.tourCode}
          </span>

          <span
            className={`rounded-full px-5 py-2 text-sm font-semibold

            ${
              tour.status === "ACTIVE"
                ? "bg-emerald-500 text-white"
                : "bg-gray-500 text-white"
            }

            `}
          >
            {tour.status}
          </span>
        </div>

        {/* Bottom Content */}

        <div className="absolute bottom-0 left-0 right-0 p-8 text-white">
          <div className="max-w-5xl">
            <h1 className="text-5xl font-bold leading-tight">{tour.title}</h1>

            <p className="mt-4 max-w-3xl text-lg text-slate-200">
              {tour.description}
            </p>

            {/* Stats */}

            <div className="mt-8 flex flex-wrap gap-6">
              <div className="flex items-center gap-2">
                <Star size={18} className="fill-yellow-400 text-yellow-400" />

                <span className="font-semibold">
                  {reviewSummary.averageRating.toFixed(1)}
                </span>

                <span className="text-slate-300">
                  ({reviewSummary.totalReviews} Reviews)
                </span>
              </div>

              <div className="flex items-center gap-2">
                <Clock3 size={18} />

                <span>{tour.durationDays} Days</span>
              </div>

              <div className="flex items-center gap-2">
                <MapPin size={18} />

                <span>Destination</span>
              </div>

              <div className="flex items-center gap-2">
                <BadgeCheck size={18} />

                <span>Verified Tour</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Floating Summary Card */}

      <div className="mt-0 px-6">
        <div className="mx-auto grid max-w-[1600px] gap-6 rounded-3xl bg-white p-8 shadow-2xl lg:grid-cols-4">
          {/* Price */}

          <div>
            <p className="text-sm text-gray-500">Starting From</p>

            <div className="mt-2 flex items-center text-3xl font-bold text-blue-600">
              <IndianRupee size={28} />

              {startingPrice.toLocaleString()}
            </div>
          </div>

          {/* Duration */}

          <div>
            <p className="text-sm text-gray-500">Duration</p>

            <div className="mt-2 flex items-center gap-2 font-semibold">
              <Clock3 size={18} />
              {tour.durationDays} Days
            </div>
          </div>

          {/* Category */}

          <div>
            <p className="text-sm text-gray-500">Category</p>

            <div className="mt-2 font-semibold">{tour.tourCode}</div>
          </div>

          {/* CTA */}

          <div className="flex items-center justify-end">
            <button
              className="
              rounded-xl
              bg-blue-600
              px-8
              py-4
              font-semibold
              text-white
              transition
              hover:bg-blue-700
              "
            >
              Book This Tour
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
