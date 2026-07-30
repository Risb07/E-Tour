export default function LoadingSkeleton() {
  return (
    <div className="max-w-7xl mx-auto px-4 py-8 animate-pulse">
      {/* Hero */}
      <div className="grid xl:grid-cols-[2fr_420px] gap-6">
        <div className="lg:col-span-2">
          <div className="h-[420px] rounded-3xl bg-slate-200" />
        </div>

        <div className="space-y-5">
          <div className="h-8 w-24 bg-slate-200 rounded-full"></div>

          <div className="h-12 bg-slate-200 rounded"></div>

          <div className="h-5 w-48 bg-slate-200 rounded"></div>

          <div className="h-5 w-40 bg-slate-200 rounded"></div>

          <div className="space-y-3">
            <div className="h-4 bg-slate-200 rounded"></div>
            <div className="h-4 bg-slate-200 rounded"></div>
            <div className="h-4 w-2/3 bg-slate-200 rounded"></div>
          </div>
        </div>
      </div>

      {/* Main Layout */}

      <div className="grid xl:grid-cols-[2fr_420px] gap-8 mt-12">
        {/* Left */}

        <div className="lg:col-span-2 space-y-8">
          {/* Summary */}

          <div className="bg-white rounded-2xl shadow p-6">
            <div className="h-8 w-48 bg-slate-200 rounded mb-5"></div>

            <div className="space-y-3">
              <div className="h-4 bg-slate-200 rounded"></div>
              <div className="h-4 bg-slate-200 rounded"></div>
              <div className="h-4 bg-slate-200 rounded"></div>
              <div className="h-4 w-3/4 bg-slate-200 rounded"></div>
            </div>
          </div>

          {/* Schedule */}

          <div className="space-y-5">
            <div className="h-8 w-56 bg-slate-200 rounded"></div>

            {[1, 2, 3].map((item) => (
              <div key={item} className="bg-white rounded-2xl shadow p-5">
                <div className="flex justify-between">
                  <div className="space-y-3">
                    <div className="h-5 w-40 bg-slate-200 rounded"></div>
                    <div className="h-4 w-52 bg-slate-200 rounded"></div>
                  </div>

                  <div className="space-y-3">
                    <div className="h-6 w-24 bg-slate-200 rounded"></div>
                    <div className="h-10 w-28 bg-slate-200 rounded-full"></div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Itinerary */}

          <div className="space-y-6">
            <div className="h-8 w-44 bg-slate-200 rounded"></div>

            {[1, 2, 3].map((day) => (
              <div key={day} className="flex gap-4">
                <div className="w-12 h-12 rounded-full bg-slate-200"></div>

                <div className="flex-1 space-y-3">
                  <div className="h-5 w-48 bg-slate-200 rounded"></div>

                  <div className="h-4 bg-slate-200 rounded"></div>

                  <div className="h-4 w-3/4 bg-slate-200 rounded"></div>
                </div>
              </div>
            ))}
          </div>

          {/* Reviews */}

          <div className="space-y-5">
            <div className="h-8 w-44 bg-slate-200 rounded"></div>

            {[1, 2].map((review) => (
              <div key={review} className="bg-white shadow rounded-2xl p-5">
                <div className="flex gap-4">
                  <div className="w-14 h-14 rounded-full bg-slate-200"></div>

                  <div className="flex-1">
                    <div className="h-5 w-40 bg-slate-200 rounded mb-3"></div>

                    <div className="h-4 w-24 bg-slate-200 rounded mb-4"></div>

                    <div className="space-y-2">
                      <div className="h-4 bg-slate-200 rounded"></div>

                      <div className="h-4 w-3/4 bg-slate-200 rounded"></div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Booking Card */}

        <div>
          <div className="sticky top-24 bg-white shadow-xl rounded-3xl p-6">
            <div className="h-10 w-40 bg-slate-200 rounded mb-6"></div>

            <div className="space-y-4">
              <div className="h-4 bg-slate-200 rounded"></div>

              <div className="h-4 bg-slate-200 rounded"></div>

              <div className="h-4 bg-slate-200 rounded"></div>

              <div className="h-12 bg-slate-200 rounded-full mt-8"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
