import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchBookingById, finalizeBookingPassengers } from "../services/bookingService";
import { fetchRoomCharges } from "../services/roomChargeService";
import { fetchTourCostsForTour } from "../services/tourCostService";
import { useToast } from "../hooks/useToast";
import { formatDate } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import { PASSENGER_TYPES } from "../constants/enums";
import {
  activeTourCost,
  expectedBandForSlot,
  isPassengerOccupancyValid,
  passengerTypeOf,
  slotMismatchMessage,
} from "../utils/passengerPricing";
import { fetchMyProfile } from "../services/customerService";
import PassengerForm from "../components/forms/PassengerForm";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";

const emptyPassenger = () => ({
  fullName: "",
  gender: "",
  dob: "",
  nationality: "",
  idProofType: "",
  idProofNumber: "",
  needsExtraBed: true,
  // Same starting point as the direct booking flow: with no date of birth yet
  // the passenger reads as an adult, so the default twin-sharing fare applies
  // until a date re-categorises them.
  occupancy: "TWIN",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  country: "",
  pincode: "",
});

/**
 * Finishes a cart-originated booking (created with just a headcount and an
 * ESTIMATED total): collects real passenger details, sends them to
 * PATCH /api/bookings/{id}/passengers to lock in the true age-banded price,
 * then hands off to the shared payment page. Mirrors the direct booking flow,
 * just against an already-existing bookingId instead of BookingFlowContext.
 */
export default function CartCheckoutPage() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [booking, setBooking] = useState(null);
  const [passengers, setPassengers] = useState([]);
  const [status, setStatus] = useState("loading");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState([]);
  const [roomCharges, setRoomCharges] = useState({});
  const [tourCosts, setTourCosts] = useState([]);

  useEffect(() => {
    fetchBookingById(bookingId)
      .then((data) => {
        setBooking(data);
        if (data.passengersFinalized) {
          // Passengers were already saved (e.g. the user came back after
          // abandoning payment) - skip straight to the payment page.
          navigate(ROUTE_PATHS.bookingPayment(bookingId), { replace: true });
          return;
        }
        setPassengers(Array.from({ length: data.numberOfPassengers }, emptyPassenger));
        setStatus("done");

        // Passenger 1 is the person booking - start their name from the
        // profile on file, only while it's still blank, and fully editable.
        fetchMyProfile()
          .then((profile) => {
            if (!profile?.fullName) return;
            setPassengers((current) =>
              current.map((p, i) =>
                i === 0 && !p.fullName?.trim() ? { ...p, fullName: profile.fullName } : p
              )
            );
          })
          .catch(() => {
            // Prefill is a convenience - the field is editable either way.
          });

        // Room supplements and the tour's cost sheet, so each occupancy
        // category shows its real price - same as the direct booking flow.
        if (data.tourId) {
          fetchRoomCharges(data.tourId)
            .then((rows) =>
              setRoomCharges(Object.fromEntries((rows || []).map((r) => [r.occupancy, r.charge])))
            )
            .catch(() => setRoomCharges({}));
          fetchTourCostsForTour(data.tourId)
            .then((rows) => setTourCosts(rows || []))
            .catch(() => setTourCosts([]));
        }
      })
      .catch(() => setStatus("failed"));
  }, [bookingId, navigate]);

  function updatePassenger(index, updated) {
    setPassengers((current) => current.map((p, i) => (i === index ? updated : p)));
  }

  function validate() {
    const nextErrors = passengers.map((p, i) => {
      const rowErrors = {
        fullName: p.fullName.trim() ? "" : "Required",
        // Must land in the band this slot was sold as - the same rule the
        // direct booking flow applies, so a cart booking can't reach zero
        // either. Cart items added before the split carry no adultCount, and
        // slotMismatchMessage then has no slot to enforce.
        dob: p.dob ? slotMismatchMessage(p, i, booking?.adultCount, booking?.departureDate) : "Required",
        idProofNumber: p.idProofNumber.trim() ? "" : "Required",
        // Save-time backstop: the category must match the band the date of
        // birth implies, the same rule the form renders its options from.
        occupancy: isPassengerOccupancyValid(p, booking?.departureDate)
          ? ""
          : passengerTypeOf(p, booking?.departureDate) === PASSENGER_TYPES.INFANT
            ? "Infants travel free and cannot have an occupancy"
            : "Choose an occupancy category for this passenger's age",
      };
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

  /**
   * Saves passenger details (which locks in the real age-banded price), then
   * hands off to the shared payment page - the cart path and the direct
   * booking path use exactly the same checkout screen.
   */
  async function handleFinalize() {
    if (!validate()) {
      showToast("Please complete all required passenger fields.", "error");
      return;
    }
    setIsSubmitting(true);
    try {
      await finalizeBookingPassengers(bookingId, passengers);
      navigate(ROUTE_PATHS.bookingPayment(bookingId));
    } catch (err) {
      showToast(err.message || "Couldn't save passenger details.", "error");
      setIsSubmitting(false);
    }
  }

  if (status === "loading") return <Loader label="Loading your booking..." />;
  if (status === "failed" || !booking) {
    return <p className="mx-auto max-w-2xl px-4 py-16 text-center text-sm text-ink-500">Could not load this booking.</p>;
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-2xl font-bold text-ink-900">{booking.tourTitle}</h1>
      <p className="mt-1 text-sm text-ink-500">
        Departs {formatDate(booking.departureDate)} · {booking.numberOfPassengers} passenger(s)
      </p>

      <div className="mt-6 flex flex-col gap-4">
        {passengers.map((passenger, index) => (
          <PassengerForm
            key={index}
            index={index}
            passenger={passenger}
            onChange={updatePassenger}
            errors={errors[index] || {}}
            departureDate={booking.departureDate}
            roomCharges={roomCharges}
            tourCost={activeTourCost(tourCosts, booking.departureDate)}
            fallbackPerSeatPrice={undefined}
            expectedBand={expectedBandForSlot(index, booking.adultCount)}
          />
        ))}
        <Button onClick={handleFinalize} size="lg" isLoading={isSubmitting} className="self-end">
          Continue to payment
        </Button>
      </div>
    </div>
  );
}
