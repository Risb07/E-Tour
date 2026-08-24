import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Check } from "lucide-react";
import { fetchTourAddons } from "../services/tourService";
import { formatCurrency, formatDate } from "../utils/format";
import { PRICE_TYPE_LABELS } from "../constants/enums";
import { ROUTE_PATHS } from "../constants/routes";
import { useBookingFlow } from "../hooks/useBookingFlow";
import BookingStepLayout from "../components/layout/BookingStepLayout";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";

export default function BookingFormPage() {
  const { tourId } = useParams();
  const navigate = useNavigate();
  const { tour, schedule, numberOfPassengers, selectedAddons, setAddons } = useBookingFlow();
  const [addons, setAvailableAddons] = useState([]);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    fetchTourAddons(tourId)
      .then((data) => setAvailableAddons(data))
      .catch(() => setAvailableAddons([]))
      .finally(() => setStatus("done"));
  }, [tourId]);

  function toggleAddon(addon) {
    const exists = selectedAddons.find((a) => a.addonId === addon.addonId);
    if (exists) {
      setAddons(selectedAddons.filter((a) => a.addonId !== addon.addonId));
    } else {
      setAddons([
        ...selectedAddons,
        { addonId: addon.addonId, name: addon.addonName, price: addon.price, quantity: 1 },
      ]);
    }
  }

  function updateQuantity(addonId, quantity) {
    setAddons(selectedAddons.map((a) => (a.addonId === addonId ? { ...a, quantity: Math.max(1, quantity) } : a)));
  }

  if (status === "loading") return <Loader label="Loading add-ons..." />;

  return (
    <BookingStepLayout step={0} title={`Book: ${tour?.title}`}>
      <div className="rounded-card bg-white p-5 shadow-soft">
        <p className="text-sm text-ink-500">
          {formatDate(schedule?.departureDate)} · {numberOfPassengers} passenger(s) ·{" "}
          {formatCurrency(schedule?.price)} each
        </p>
      </div>

      <h3 className="mt-6 font-semibold text-ink-900">Optional add-ons</h3>
      {addons.length === 0 ? (
        <p className="mt-2 text-sm text-ink-400">No add-ons available for this tour.</p>
      ) : (
        <div className="mt-3 flex flex-col gap-3">
          {addons.map((addon) => {
            const selected = selectedAddons.find((a) => a.addonId === addon.addonId);
            return (
              <div
                key={addon.addonId}
                className={`flex items-center justify-between rounded-card border p-4 transition-colors ${
                  selected ? "border-amber-400 bg-amber-50" : "border-ink-100 bg-white"
                }`}
              >
                <button onClick={() => toggleAddon(addon)} className="flex flex-1 items-center gap-3 text-left">
                  <span
                    className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-md border-2 ${
                      selected ? "border-amber-500 bg-amber-500 text-white" : "border-ink-300"
                    }`}
                  >
                    {selected && <Check className="h-3.5 w-3.5" />}
                  </span>
                  <span>
                    <span className="block text-sm font-semibold text-ink-900">{addon.addonName}</span>
                    <span className="block text-xs text-ink-500">
                      {formatCurrency(addon.price)} {PRICE_TYPE_LABELS[addon.priceType]}
                    </span>
                  </span>
                </button>
                {selected && (
                  <input
                    type="number"
                    min={1}
                    value={selected.quantity}
                    onChange={(e) => updateQuantity(addon.addonId, Number(e.target.value))}
                    className="w-16 rounded-lg border border-ink-200 px-2 py-1.5 text-center text-sm"
                  />
                )}
              </div>
            );
          })}
        </div>
      )}

      <div className="mt-8 flex justify-end">
        <Button onClick={() => navigate(ROUTE_PATHS.BOOKING_PASSENGERS)} size="lg">
          Continue to passenger details
        </Button>
      </div>
    </BookingStepLayout>
  );
}
