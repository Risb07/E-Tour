import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchRoomCharges } from "../services/roomChargeService";
import { fetchTourCostsForTour } from "../services/tourCostService";
import { useBookingFlow } from "../hooks/useBookingFlow";
import { useToast } from "../hooks/useToast";
import { ROUTE_PATHS } from "../constants/routes";
import { PASSENGER_TYPES } from "../constants/enums";
import { formatCurrency } from "../utils/format";
import {
  activeTourCost,
  expectedBandForSlot,
  hasPricing,
  isPassengerOccupancyValid,
  passengersTotal,
  passengerTypeOf,
  reconcilePassenger,
  slotMismatchMessage,
} from "../utils/passengerPricing";
import { fetchMyProfile } from "../services/customerService";
import BookingStepLayout from "../components/layout/BookingStepLayout";
import PassengerForm from "../components/forms/PassengerForm";
import Button from "../components/common/Button";

const emptyPassenger = () => ({
  fullName: "",
  gender: "",
  dob: "",
  nationality: "",
  idProofType: "",
  idProofNumber: "",
  needsExtraBed: true,
  // With no date of birth yet the passenger reads as an adult (the existing
  // rule for unknown dates), so the default twin-sharing fare is the right
  // starting point. Entering a date re-categorises them automatically.
  occupancy: "TWIN",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  country: "",
  pincode: "",
});

export default function PassengerDetailsPage() {
  const navigate = useNavigate();
  const { numberOfPassengers, adultCount, childCount, passengers, schedule, tour, setPassengers } =
    useBookingFlow();
  const [roomCharges, setRoomCharges] = useState({});
  const [tourCosts, setTourCosts] = useState([]);

  // The party was fixed on the tour page - this step never asks again, it
  // just renders exactly the forms that were bought.
  const adults = adultCount ?? numberOfPassengers;
  const children = childCount ?? 0;
  const totalForms = adults + children;

  // Admin-configured room supplements for this tour, so each option shows its
  // real price. Degrades to no prices when none are configured.
  useEffect(() => {
    if (!tour?.tourId) return;
    fetchRoomCharges(tour.tourId)
      .then((rows) =>
        setRoomCharges(
          Object.fromEntries((rows || []).map((r) => [r.occupancy, r.charge]))
        )
      )
      .catch(() => setRoomCharges({}));
  }, [tour?.tourId]);

  // The tour's cost sheet - twin / single / extra person / child rates - so
  // each occupancy category can show its own price and the total can update
  // as soon as a selection changes. The server still quotes the real price on
  // the next step; this is a live preview, not a substitute.
  useEffect(() => {
    if (!tour?.tourId) return;
    fetchTourCostsForTour(tour.tourId)
      .then((rows) => setTourCosts(rows || []))
      .catch(() => setTourCosts([]));
  }, [tour?.tourId]);

  const { showToast } = useToast();

  const [formPassengers, setFormPassengers] = useState(() =>
    passengers.length === totalForms
      ? passengers.map((p) => reconcilePassenger(p, schedule?.departureDate))
      : Array.from({ length: totalForms }, emptyPassenger)
  );
  const [errors, setErrors] = useState([]);

  // Passenger 1 is the person booking, so start their name from the profile
  // already on file. Only the lead passenger, only when still blank, and
  // fully editable afterwards - a silent overwrite of something the user
  // typed would be worse than not prefilling at all.
  useEffect(() => {
    let cancelled = false;
    fetchMyProfile()
      .then((profile) => {
        if (cancelled || !profile?.fullName) return;
        setFormPassengers((current) =>
          current.map((p, i) => (i === 0 && !p.fullName?.trim() ? { ...p, fullName: profile.fullName } : p))
        );
      })
      .catch(() => {
        // Prefill is a convenience - the field is editable either way.
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const pricingContext = useMemo(
    () => ({
      tourCost: activeTourCost(tourCosts, schedule?.departureDate),
      roomCharges,
      fallbackPerSeatPrice: schedule?.price,
      departureDate: schedule?.departureDate,
    }),
    [tourCosts, roomCharges, schedule?.departureDate, schedule?.price]
  );

  // Recomputed from the passenger list on every render, so it self-corrects
  // whenever a date of birth moves someone into a different band, a category
  // changes, or a passenger is added or removed.
  const runningTotal = useMemo(
    () => passengersTotal(formPassengers, pricingContext),
    [formPassengers, pricingContext]
  );
  const showTotal = hasPricing(pricingContext);

  function updatePassenger(index, updated) {
    setFormPassengers((current) => current.map((p, i) => (i === index ? updated : p)));
  }

  function validate() {
    const nextErrors = formPassengers.map((p, i) => {
      const rowErrors = {
        fullName: p.fullName.trim() ? "" : "Required",
        // The date of birth must land in the band this slot was sold as -
        // this is what stops an adult seat being filled with a newborn and
        // priced at zero.
        dob: p.dob ? slotMismatchMessage(p, i, adults, schedule?.departureDate) : "Required",
        idProofNumber: p.idProofNumber.trim() ? "" : "Required",
        // The form only offers categories valid for the band the date of
        // birth implies, and re-checks on every date change - so this is the
        // save-time backstop that makes an invalid passenger unsaveable.
        occupancy: isPassengerOccupancyValid(p, schedule?.departureDate)
          ? ""
          : passengerTypeOf(p, schedule?.departureDate) === PASSENGER_TYPES.INFANT
            ? "Infants travel free and cannot have an occupancy"
            : "Choose an occupancy category for this passenger's age",
      };
      // Address is only collected from the primary passenger (BRD 3.7).
      if (i === 0) {
        rowErrors.addressLine1 = p.addressLine1?.trim() ? "" : "Required";
        rowErrors.city = p.city?.trim() ? "" : "Required";
        rowErrors.pincode = p.pincode?.trim() ? "" : "Required";
      }
      return rowErrors;
    });
    setErrors(nextErrors);
    return nextErrors.every((e) => Object.values(e).every((msg) => !msg));
  }

  function handleContinue() {
    if (!validate()) {
      showToast("Please complete all required passenger fields.", "error");
      return;
    }
    setPassengers(formPassengers);
    navigate(ROUTE_PATHS.BOOKING_SUMMARY);
  }

  return (
    <BookingStepLayout step={1} title="Passenger details">
      {/* The party was set on the tour page; this is a reminder, not a control. */}
      <p className="mb-4 text-sm text-ink-500">
        {adults} adult{adults === 1 ? "" : "s"}
        {children > 0 && <>, {children} child{children === 1 ? "" : "ren"}</>} · {totalForms} passenger
        {totalForms === 1 ? "" : "s"}
      </p>

      <div className="flex flex-col gap-4">
        {formPassengers.map((passenger, index) => (
          <PassengerForm
            key={index}
            index={index}
            passenger={passenger}
            onChange={updatePassenger}
            errors={errors[index] || {}}
            departureDate={schedule?.departureDate}
            roomCharges={roomCharges}
            tourCost={pricingContext.tourCost}
            fallbackPerSeatPrice={pricingContext.fallbackPerSeatPrice}
            expectedBand={expectedBandForSlot(index, adults)}
          />
        ))}
      </div>

      {/* Live total of the individual passenger prices above. Confirmed
          against the server on the next step before any payment. */}
      {showTotal && (
        <div className="mt-4 flex items-center justify-between rounded-card bg-white p-5 shadow-soft">
          <div>
            <p className="text-sm font-semibold text-ink-900">Passenger total</p>
            <p className="text-xs text-ink-400">
              {formPassengers.length} passenger(s) · excludes add-ons and GST
            </p>
          </div>
          <span className="font-display text-xl font-bold text-ink-900">
            {formatCurrency(runningTotal)}
          </span>
        </div>
      )}

      <div className="mt-8 flex justify-between">
        <Button variant="ghost" onClick={() => navigate(-1)}>
          Back
        </Button>
        <Button onClick={handleContinue} size="lg">
          Review booking
        </Button>
      </div>
    </BookingStepLayout>
  );
}
