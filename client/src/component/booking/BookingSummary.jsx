import React from "react";
import {
  MapPin,
  CalendarDays,
  Users,
  ShieldCheck,
  BadgeCheck,
  CreditCard,
} from "lucide-react";

const BookingSummary = ({ tour, passengerCount }) => {
  if (!tour) return null;

  const tourCost = tour.basePrice * passengerCount;
  const gst = Math.round(tourCost * 0.05);
  const convenienceFee = 199;
  const discount = passengerCount >= 2 ? 1000 : 0;

  const grandTotal = tourCost + gst + convenienceFee - discount;

  return (
    <aside className="sticky top-6">
      <div className="rounded-2xl border bg-white shadow-lg overflow-hidden">
        {/* Tour Image */}
        <img
          src={tour.image}
          alt={tour.title}
          className="h-48 w-full object-cover"
        />

        <div className="p-6 space-y-6">
          {/* Tour */}
          <div>
            <h2 className="text-xl font-bold text-slate-900">{tour.title}</h2>

            <div className="flex items-center text-sm text-slate-500 mt-2">
              <MapPin size={16} className="mr-1" />
              {tour.location}
            </div>
          </div>

          {/* Dates */}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-slate-500">Departure</p>

              <div className="flex items-center mt-1">
                <CalendarDays size={16} className="mr-2 text-blue-600" />

                <span className="font-medium">{tour.departureDate}</span>
              </div>
            </div>

            <div>
              <p className="text-xs text-slate-500">Return</p>

              <div className="flex items-center mt-1">
                <CalendarDays size={16} className="mr-2 text-blue-600" />

                <span className="font-medium">{tour.returnDate}</span>
              </div>
            </div>
          </div>

          {/* Info */}

          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="rounded-xl bg-slate-100 p-3">
              <p className="text-xs text-slate-500">Duration</p>

              <p className="font-semibold">{tour.durationDays} Days</p>
            </div>

            <div className="rounded-xl bg-slate-100 p-3">
              <p className="text-xs text-slate-500">Travelers</p>

              <p className="font-semibold">{passengerCount}</p>
            </div>

            <div className="rounded-xl bg-slate-100 p-3">
              <p className="text-xs text-slate-500">Seats</p>

              <p className="font-semibold text-green-600">
                {tour.availableSeats}
              </p>
            </div>
          </div>

          {/* Divider */}

          <hr />

          {/* Price */}

          <div>
            <h3 className="font-semibold text-lg mb-4">Price Details</h3>

            <div className="space-y-3">
              <PriceRow label="Tour Cost" value={tourCost} />

              <PriceRow label="GST (5%)" value={gst} />

              <PriceRow label="Convenience Fee" value={convenienceFee} />

              <PriceRow label="Discount" value={-discount} green />
            </div>

            <hr className="my-4" />

            <div className="flex justify-between">
              <span className="font-bold text-lg">Grand Total</span>

              <span className="font-bold text-blue-600 text-xl">
                ₹{grandTotal.toLocaleString()}
              </span>
            </div>
          </div>

          {/* Coupon */}

          <div>
            <label className="text-sm font-medium">Coupon Code</label>

            <div className="flex mt-2">
              <input
                placeholder="Enter Coupon"
                className="flex-1 rounded-l-xl border px-3 h-11 outline-none focus:ring-2 focus:ring-blue-500"
              />

              <button className="bg-blue-600 text-white px-5 rounded-r-xl hover:bg-blue-700 transition">
                Apply
              </button>
            </div>
          </div>

          {/* Cancellation */}

          <div className="rounded-xl bg-green-50 border border-green-200 p-4">
            <h4 className="font-semibold text-green-700">Free Cancellation</h4>

            <p className="text-sm text-slate-600 mt-1">
              Cancel up to 5 days before departure for a full refund.
            </p>
          </div>

          {/* Security */}

          <div className="grid grid-cols-2 gap-3">
            <Badge icon={<ShieldCheck size={18} />} text="Secure Payment" />

            <Badge
              icon={<CreditCard size={18} />}
              text="Instant Confirmation"
            />

            <Badge icon={<BadgeCheck size={18} />} text="Verified Tours" />

            <Badge icon={<Users size={18} />} text="24x7 Support" />
          </div>
        </div>
      </div>
    </aside>
  );
};

const PriceRow = ({ label, value, green }) => (
  <div className="flex justify-between text-sm">
    <span className="text-slate-600">{label}</span>

    <span className={`font-semibold ${green ? "text-green-600" : ""}`}>
      {value < 0 ? "-" : ""}₹{Math.abs(value).toLocaleString()}
    </span>
  </div>
);

const Badge = ({ icon, text }) => (
  <div className="flex items-center gap-2 rounded-xl border bg-slate-50 p-3 text-sm">
    <span className="text-blue-600">{icon}</span>

    <span className="font-medium">{text}</span>
  </div>
);

export default BookingSummary;
