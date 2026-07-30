import {
  Clock3,
  IndianRupee,
  BadgeCheck,
  Tag,
  FileText,
  Layers3,
} from "lucide-react";

export default function TourSummary({ tour }) {
  return (
    <section className="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
      {/* Header */}

      <div className="border-b border-slate-200 px-8 py-6">
        <div className="flex items-center gap-3">
          <FileText className="text-blue-600" size={28} />

          <div>
            <h2 className="text-2xl font-bold text-slate-800">
              About This Tour
            </h2>

            <p className="text-slate-500 text-sm">
              Tour overview and important information
            </p>
          </div>
        </div>
      </div>

      {/* Description */}

      <div className="px-8 pt-8">
        <p className="text-slate-600 leading-8 text-[16px]">
          {tour.description}
        </p>
      </div>

      {/* Info Grid */}

      <div className="grid md:grid-cols-2 grid-cols-2 lg:grid-cols-2 gap-5 p-8">
        {/* Duration */}

        <div className="rounded-2xl border border-slate-200 p-5 hover:border-blue-300 transition">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-blue-50">
              <Clock3 size={22} className="text-blue-600" />
            </div>

            <div>
              <p className="text-sm text-slate-500">Duration</p>

              <h4 className="font-semibold text-slate-800">
                {tour.durationDays} Days
              </h4>
            </div>
          </div>
        </div>

        {/* Base Price */}

        <div className="rounded-2xl border border-slate-200 p-5 hover:border-blue-300 transition">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-green-50">
              <IndianRupee size={22} className="text-green-600" />
            </div>

            <div>
              <p className="text-sm text-slate-500">Base Price</p>

              <h4 className="font-semibold text-slate-800">
                ₹{Number(tour.basePrice).toLocaleString("en-IN")}
              </h4>
            </div>
          </div>
        </div>

        {/* Tour Code */}

        <div className="rounded-2xl border border-slate-200 p-5 hover:border-blue-300 transition">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-orange-50">
              <Tag size={22} className="text-orange-500" />
            </div>

            <div>
              <p className="text-sm text-slate-500">Tour Code</p>

              <h4 className="font-semibold text-slate-800">{tour.tourCode}</h4>
            </div>
          </div>
        </div>

        {/* Status */}

        <div className="rounded-2xl border border-slate-200 p-5 hover:border-blue-300 transition">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-emerald-50">
              <BadgeCheck size={22} className="text-emerald-600" />
            </div>

            <div>
              <p className="text-sm text-slate-500">Status</p>

              <span
                className={`
                inline-flex
                rounded-full
                px-3
                py-1
                text-xs
                font-semibold

                ${
                  tour.status === "ACTIVE"
                    ? "bg-emerald-100 text-emerald-700"
                    : "bg-gray-100 text-gray-700"
                }
                `}
              >
                {tour.status}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Categories */}

      <div className="border-t border-slate-200 px-8 py-6">
        <div className="flex items-center gap-2 mb-4">
          <Layers3 size={20} className="text-blue-600" />

          <h3 className="font-semibold text-slate-800">Categories</h3>
        </div>

        {tour.categories?.length > 0 ? (
          <div className="flex flex-wrap gap-3">
            {tour.categories.map((category) => (
              <span
                key={category.categoryId}
                className="
                  rounded-full
                  bg-blue-50
                  px-4
                  py-2
                  text-sm
                  font-medium
                  text-blue-700
                "
              >
                {category.name}
              </span>
            ))}
          </div>
        ) : (
          <p className="text-slate-500">No categories assigned.</p>
        )}
      </div>
    </section>
  );
}
