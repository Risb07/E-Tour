import { CalendarRange } from "lucide-react";
import ScheduleCard from "./ScheduleCard";

export default function ScheduleSection({
  schedules,
  selectedSchedule,
  setSelectedSchedule,
}) {
  if (!schedules || schedules.length === 0) {
    return (
      <section className="bg-white rounded-3xl shadow-sm border border-slate-200 p-8">
        <div className="flex items-center gap-3 mb-6">
          <CalendarRange size={28} className="text-blue-600" />

          <div>
            <h2 className="text-2xl font-bold text-slate-800">
              Available Schedules
            </h2>

            <p className="text-slate-500">No departures available.</p>
          </div>
        </div>

        <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-10 text-center">
          <CalendarRange size={52} className="mx-auto text-slate-400" />

          <h3 className="mt-5 text-lg font-semibold text-slate-700">
            No Schedule Found
          </h3>

          <p className="mt-2 text-slate-500">
            This tour currently has no available departure dates.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section>
      {/* Header */}

      <div className="mb-8">
        <div className="flex items-center gap-3">
          <CalendarRange size={30} className="text-blue-600" />

          <div>
            <h2 className="text-3xl font-bold text-slate-800">
              Available Schedules
            </h2>

            <p className="text-slate-500 mt-1">
              Select a departure date to continue with your booking.
            </p>
          </div>
        </div>
      </div>

      {/* Schedule Cards */}

      <div className="space-y-5">
        {schedules.map((schedule) => (
          <ScheduleCard
            key={schedule.scheduleId}
            schedule={schedule}
            selected={selectedSchedule?.scheduleId === schedule.scheduleId}
            onSelect={setSelectedSchedule}
          />
        ))}
      </div>

      {/* Footer */}

      <div className="mt-8 rounded-2xl bg-blue-50 border border-blue-100 p-5">
        <h4 className="font-semibold text-blue-900">Booking Information</h4>

        <ul className="mt-3 space-y-2 text-sm text-blue-700">
          <li>• Prices shown are per traveller.</li>

          <li>• Seats are updated in real time.</li>

          <li>• Select a schedule before proceeding to booking.</li>
        </ul>
      </div>
    </section>
  );
}
