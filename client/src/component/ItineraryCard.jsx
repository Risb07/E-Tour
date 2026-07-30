import { CalendarDays, MapPinned } from "lucide-react";

export default function ItineraryCard({ day, isLast }) {
  return (
    <div className="relative flex gap-6">
      {/* Timeline */}

      <div className="flex flex-col items-center">
        {/* Circle */}

        <div className="z-10 flex h-12 w-12 items-center justify-center rounded-full bg-blue-600 text-white shadow-lg">
          <CalendarDays size={20} />
        </div>

        {/* Vertical Line */}

        {!isLast && <div className="mt-2 h-full w-[2px] bg-slate-200"></div>}
      </div>

      {/* Card */}

      <div className="mb-8 flex-1 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg">
        {/* Day */}

        <span
          className="
            inline-flex
            rounded-full
            bg-blue-100
            px-4
            py-1
            text-sm
            font-semibold
            text-blue-700
          "
        >
          Day {day.dayNumber}
        </span>

        {/* Title */}

        <h3 className="mt-4 text-xl font-bold text-slate-800">{day.title}</h3>

        {/* Description */}

        <p className="mt-3 leading-7 text-slate-600">{day.description}</p>

        {/* Footer */}

        <div className="mt-6 flex items-center gap-2 text-sm text-slate-500">
          <MapPinned size={16} className="text-blue-600" />
          Planned Activity
        </div>
      </div>
    </div>
  );
}
