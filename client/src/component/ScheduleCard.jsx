import {
  CalendarDays,
  IndianRupee,
  Users,
  CheckCircle2,
  ArrowRight,
} from "lucide-react";

export default function ScheduleCard({ schedule, selected, onSelect }) {
  const departure = new Date(schedule.departureDate);

  const returning = new Date(schedule.returnDate);

  const seats = schedule.availableSeats;

  const seatColor =
    seats > 20
      ? "text-emerald-600"
      : seats > 5
        ? "text-amber-600"
        : "text-red-600";

  const seatBadge =
    seats > 20 ? "bg-emerald-100" : seats > 5 ? "bg-amber-100" : "bg-red-100";

  return (
    <div
      onClick={() => onSelect(schedule)}
      className={`
        cursor-pointer
        rounded-3xl
        border-2
        transition-all
        duration-300
        p-5

        ${
          selected
            ? "border-blue-600 bg-blue-50 shadow-xl"
            : "border-slate-200 bg-white hover:border-blue-300 hover:shadow-lg"
        }
      `}
    >
      <div className="flex flex-col lg:flex-row justify-between gap-4">
        {/* Left */}

        <div className="space-y-5 flex-1">
          {/* Dates */}

          <div className="flex items-center gap-3">
            <CalendarDays className="text-blue-600" size={22} />

            <div>
              <h3 className="font-semibold text-slate-800">
                {departure.toLocaleDateString("en-IN", {
                  day: "numeric",
                  month: "short",
                  year: "numeric",
                })}

                {"  "}

                <ArrowRight size={15} className="inline mx-2" />

                {returning.toLocaleDateString("en-IN", {
                  day: "numeric",
                  month: "short",
                  year: "numeric",
                })}
              </h3>

              <p className="text-sm text-slate-500">Departure & Return</p>
            </div>
          </div>

          {/* Seats */}

          <div className="flex items-center gap-3">
            <Users className={seatColor} size={22} />

            <div>
              <h4 className={`font-semibold ${seatColor}`}>
                {schedule.availableSeats} Seats Available
              </h4>

              <p className="text-sm text-slate-500">Instant Confirmation</p>
            </div>
          </div>
        </div>

        {/* Right */}

        <div className="flex flex-col justify-between items-end">
          <div className="flex items-center text-2xl font-bold text-blue-600">
            <IndianRupee size={26} />

            {Number(schedule.price).toLocaleString("en-IN")}
          </div>

          <div className="flex items-center gap-3 mt-5">
            <span
              className={`
                rounded-full
                px-4
                py-2
                text-xs
                font-semibold

                ${seatBadge}
                ${seatColor}
              `}
            >
              {schedule.availableSeats > 20
                ? "Available"
                : schedule.availableSeats > 5
                  ? "Limited"
                  : "Few Seats"}
            </span>

            {selected && <CheckCircle2 className="text-blue-600" size={28} />}
          </div>
        </div>
      </div>
    </div>
  );
}
