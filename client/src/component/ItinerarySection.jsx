import { Route, Compass } from "lucide-react";
import ItineraryCard from "./ItineraryCard";

export default function ItinerarySection({ itinerary }) {
  if (!itinerary || itinerary.length === 0) {
    return (
      <section className="bg-white rounded-3xl border border-slate-200 shadow-sm p-8">
        {/* Header */}

        <div className="flex items-center gap-3 mb-6">
          <Route size={30} className="text-blue-600" />

          <div>
            <h2 className="text-2xl font-bold text-slate-800">
              Tour Itinerary
            </h2>

            <p className="text-slate-500">
              No itinerary has been added for this tour.
            </p>
          </div>
        </div>

        {/* Empty State */}

        <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-12 text-center">
          <Compass size={56} className="mx-auto text-slate-400" />

          <h3 className="mt-5 text-xl font-semibold text-slate-700">
            Itinerary Coming Soon
          </h3>

          <p className="mt-3 text-slate-500">
            The day-by-day travel plan will be available shortly.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section>
      {/* Section Header */}

      <div className="mb-10">
        <div className="flex items-center gap-3">
          <Route size={32} className="text-blue-600" />

          <div>
            <h2 className="text-3xl font-bold text-slate-800">
              Tour Itinerary
            </h2>

            <p className="mt-1 text-slate-500">
              Explore your journey day by day.
            </p>
          </div>
        </div>
      </div>

      {/* Timeline */}

      <div>
        {itinerary
          .sort((a, b) => a.dayNumber - b.dayNumber)
          .map((day, index) => (
            <ItineraryCard
              key={day.itineraryId}
              day={day}
              isLast={index === itinerary.length - 1}
            />
          ))}
      </div>

      {/* Travel Note */}

      <div className="mt-8 rounded-2xl border border-blue-100 bg-blue-50 p-6">
        <h3 className="font-semibold text-blue-900">Travel Note</h3>

        <p className="mt-2 text-sm leading-6 text-blue-700">
          The itinerary is tentative and may change due to weather conditions,
          operational requirements, or local circumstances while maintaining the
          overall tour experience.
        </p>
      </div>
    </section>
  );
}
