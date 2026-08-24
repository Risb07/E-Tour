import { createContext, useReducer, useMemo, useCallback, useEffect } from "react";
import {
  bookingFlowReducer,
  initialBookingFlowState,
  BOOKING_FLOW_ACTIONS,
} from "../reducers/bookingFlowReducer";
import { createBooking, quoteBooking } from "../services/bookingService";
import { saveBookingFlow, loadBookingFlow, clearBookingFlow } from "../utils/bookingFlowStorage";

export const BookingFlowContext = createContext(null);

function initBookingFlowState() {
  const saved = loadBookingFlow();
  if (!saved) return initialBookingFlowState;

  const state = { ...initialBookingFlowState, ...saved };

  // A session stored before the tour page asked for adults and children
  // separately has numberOfPassengers but neither count, so the spread above
  // would leave the DEFAULTS (1 adult, 0 children) sitting next to a saved
  // headcount of, say, 3. The passenger step would then render one form for a
  // three-seat booking and the server would reject the mismatch.
  //
  // Treat a saved party with no declared split as all adults - the same
  // fallback the passenger page and the server already apply to bookings that
  // predate the split.
  if (saved.numberOfPassengers != null && saved.adultCount == null) {
    state.adultCount = saved.numberOfPassengers;
    state.childCount = 0;
  }
  return state;
}

export function BookingFlowProvider({ children }) {
  const [state, dispatch] = useReducer(bookingFlowReducer, undefined, initBookingFlowState);

  // Persist on every change so a refresh mid-flow (tour picked, passengers
  // entered, etc.) doesn't get bounced to Home by BookingStepLayout's
  // "!tour || !schedule" guard.
  useEffect(() => {
    if (state.tour && state.schedule) {
      saveBookingFlow(state);
    } else {
      clearBookingFlow();
    }
  }, [state]);

  // adultCount/childCount default from numberOfPassengers so any caller that
  // still starts a booking with only a total keeps working.
  const startBooking = useCallback((tour, schedule, numberOfPassengers, adultCount, childCount) => {
    dispatch({
      type: BOOKING_FLOW_ACTIONS.START,
      payload: {
        tour,
        schedule,
        numberOfPassengers,
        adultCount: adultCount ?? numberOfPassengers,
        childCount: childCount ?? 0,
      },
    });
  }, []);

  const setAddons = useCallback((addons) => {
    dispatch({ type: BOOKING_FLOW_ACTIONS.SET_ADDONS, payload: addons });
  }, []);

  const setPassengers = useCallback((passengers) => {
    dispatch({ type: BOOKING_FLOW_ACTIONS.SET_PASSENGERS, payload: passengers });
  }, []);

  const reset = useCallback(() => {
    clearBookingFlow();
    dispatch({ type: BOOKING_FLOW_ACTIONS.RESET });
  }, []);

  const bookingPayload = useMemo(
    () => ({
      scheduleId: state.schedule?.scheduleId,
      numberOfPassengers: state.numberOfPassengers,
      adultCount: state.adultCount,
      childCount: state.childCount,
      addons: state.selectedAddons.map((a) => ({ addonId: a.addonId, quantity: a.quantity })),
      passengers: state.passengers,
    }),
    [
      state.schedule,
      state.numberOfPassengers,
      state.adultCount,
      state.childCount,
      state.selectedAddons,
      state.passengers,
    ]
  );

  // BRD 3.7 "Done" summary - a read-only price preview using the exact same
  // age-banded pricing the real booking will use, so what's shown here is
  // what gets charged.
  const getQuote = useCallback(() => quoteBooking(bookingPayload), [bookingPayload]);

  // Booking + passengers are created together in one call now (not
  // booking-then-add-passenger) so the server can price by passenger age
  // at creation time - see TourPricingCalculator.
  const submitBooking = useCallback(async () => {
    dispatch({ type: BOOKING_FLOW_ACTIONS.SET_SUBMITTING, payload: true });
    try {
      const booking = await createBooking(bookingPayload);
      dispatch({ type: BOOKING_FLOW_ACTIONS.SET_BOOKING_RESULT, payload: booking });
      return booking;
    } catch (err) {
      dispatch({ type: BOOKING_FLOW_ACTIONS.SET_ERROR, payload: err.message });
      throw err;
    }
  }, [bookingPayload]);

  const estimatedTotal = useMemo(() => {
    if (!state.schedule) return 0;
    const base = Number(state.schedule.price) * state.numberOfPassengers;
    const addonsTotal = state.selectedAddons.reduce((sum, a) => sum + Number(a.price) * a.quantity, 0);
    return base + addonsTotal;
  }, [state.schedule, state.numberOfPassengers, state.selectedAddons]);

  const value = useMemo(
    () => ({ ...state, estimatedTotal, startBooking, setAddons, setPassengers, submitBooking, getQuote, reset }),
    [state, estimatedTotal, startBooking, setAddons, setPassengers, submitBooking, getQuote, reset]
  );

  return <BookingFlowContext.Provider value={value}>{children}</BookingFlowContext.Provider>;
}
