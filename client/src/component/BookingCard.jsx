import {
  CalendarDays,
  Clock3,
  IndianRupee,
  Users,
  BadgeCheck,
  ArrowRight,
} from "lucide-react";

export default function BookingCard({ tour, selectedSchedule, onBook }) {
  if (!selectedSchedule) {
    return (
      <div className="max-w-md w-full">
        <aside className="sticky top-24">
          <div className="rounded-3xl border border-slate-200 bg-white p-8 shadow-lg">
            <h2 className="text-xl font-bold text-slate-800">Book This Tour</h2>

            <p className="mt-4 text-slate-500">
              Please select a schedule to continue.
            </p>
          </div>
        </aside>
      </div>
    );
  }

  const departure = new Date(selectedSchedule.departureDate);

  const returning = new Date(selectedSchedule.returnDate);

  const seats = selectedSchedule.availableSeats;

  const soldOut = seats <= 0;

  return (
    <aside className="sticky top-24">
      <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl">
        {/* Header */}

        <div className="bg-gradient-to-r from-blue-600 to-blue-700 p-6 text-white">
          <p className="text-sm opacity-90">Starting From</p>

          <div className="mt-2 flex items-center text-4xl font-bold">
            <IndianRupee size={32} />

            {Number(selectedSchedule.price).toLocaleString("en-IN")}
          </div>

          <p className="mt-2 text-sm opacity-80">Per Traveller</p>
        </div>

        {/* Body */}

        <div className="space-y-5 p-6">
          {/* Tour */}

          <div>
            <h3 className="font-semibold text-slate-800">{tour.title}</h3>

            <p className="text-sm text-slate-500">{tour.tourCode}</p>
          </div>

          <hr />

          {/* Departure */}

          <div className="flex justify-between">
            <div className="flex gap-3">
              <CalendarDays className="text-blue-600" size={20} />

              <div>
                <p className="text-sm text-slate-500">Departure</p>

                <p className="font-medium">
                  {departure.toLocaleDateString("en-IN", {
                    day: "numeric",
                    month: "short",
                    year: "numeric",
                  })}
                </p>
              </div>
            </div>
          </div>

          {/* Return */}

          <div className="flex justify-between">
            <div className="flex gap-3">
              <ArrowRight className="text-blue-600" size={20} />

              <div>
                <p className="text-sm text-slate-500">Return</p>

                <p className="font-medium">
                  {returning.toLocaleDateString("en-IN", {
                    day: "numeric",
                    month: "short",
                    year: "numeric",
                  })}
                </p>
              </div>
            </div>
          </div>

          {/* Duration */}

          <div className="flex justify-between">
            <div className="flex gap-3">
              <Clock3 className="text-blue-600" size={20} />

              <div>
                <p className="text-sm text-slate-500">Duration</p>

                <p className="font-medium">{tour.durationDays} Days</p>
              </div>
            </div>
          </div>

          {/* Seats */}

          <div className="flex justify-between">
            <div className="flex gap-3">
              <Users className="text-blue-600" size={20} />

              <div>
                <p className="text-sm text-slate-500">Available Seats</p>

                <p
                  className={`font-semibold ${
                    seats > 10
                      ? "text-emerald-600"
                      : seats > 0
                        ? "text-orange-600"
                        : "text-red-600"
                  }`}
                >
                  {seats}
                </p>
              </div>
            </div>
          </div>

          {/* Status */}

          <div className="flex justify-between">
            <div className="flex gap-3">
              <BadgeCheck className="text-blue-600" size={20} />

              <div>
                <p className="text-sm text-slate-500">Status</p>

                <span
                  className={`
                  rounded-full
                  px-3
                  py-1
                  text-xs
                  font-semibold

                  ${
                    soldOut
                      ? "bg-red-100 text-red-700"
                      : "bg-emerald-100 text-emerald-700"
                  }
                  `}
                >
                  {soldOut ? "Sold Out" : "Available"}
                </span>
              </div>
            </div>
          </div>

          <hr />

          {/* CTA */}

          <button
            disabled={soldOut}
            onClick={() => onBook(selectedSchedule)}
            className={`
              w-full
              rounded-xl
              py-4
              font-semibold
              transition

              ${
                soldOut
                  ? "cursor-not-allowed bg-slate-300 text-slate-500"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }
            `}
          >
            {soldOut ? "Sold Out" : "Book Now"}
          </button>

          <button
            className="
              w-full
              rounded-xl
              border
              border-blue-600
              py-3
              font-semibold
              text-blue-600
              transition
              hover:bg-blue-50
            "
          >
            Enquire Now
          </button>

          <p className="text-center text-xs text-slate-500">
            Secure booking • Instant confirmation
          </p>
        </div>
      </div>
    </aside>
  );
}
