// Drives the multi-step Booking Flow: Tour Details -> Booking Form ->
// Passenger Details -> Booking Summary -> Confirmation. A reducer is the
// right fit here (vs local state per page) because state accumulates
// across route transitions and every step needs to read what earlier
// steps collected.
export const BOOKING_FLOW_ACTIONS = {
  START: "START", // tour + schedule chosen
  SET_ADDONS: "SET_ADDONS",
  SET_PASSENGERS: "SET_PASSENGERS",
  SET_SUBMITTING: "SET_SUBMITTING",
  SET_BOOKING_RESULT: "SET_BOOKING_RESULT",
  SET_ERROR: "SET_ERROR",
  RESET: "RESET",
};

export const initialBookingFlowState = {
  tour: null,
  schedule: null,
  numberOfPassengers: 1,
  // Party composition chosen on the tour page. numberOfPassengers is their
  // sum; both are carried so the passenger step knows how many adult and
  // child forms to render without asking again.
  adultCount: 1,
  childCount: 0,
  selectedAddons: [], // [{ addonId, quantity, name, price }]
  passengers: [], // [{ fullName, gender, dob, nationality, idProofType, idProofNumber }]
  isSubmitting: false,
  bookingResult: null, // BookingResponse once created
  error: null,
};

export function bookingFlowReducer(state, action) {
  switch (action.type) {
    case BOOKING_FLOW_ACTIONS.START:
      return {
        ...initialBookingFlowState,
        tour: action.payload.tour,
        schedule: action.payload.schedule,
        numberOfPassengers: action.payload.numberOfPassengers,
        adultCount: action.payload.adultCount,
        childCount: action.payload.childCount,
      };
    case BOOKING_FLOW_ACTIONS.SET_ADDONS:
      return { ...state, selectedAddons: action.payload };
    case BOOKING_FLOW_ACTIONS.SET_PASSENGERS:
      return { ...state, passengers: action.payload };
    case BOOKING_FLOW_ACTIONS.SET_SUBMITTING:
      return { ...state, isSubmitting: action.payload, error: null };
    case BOOKING_FLOW_ACTIONS.SET_BOOKING_RESULT:
      return { ...state, isSubmitting: false, bookingResult: action.payload };
    case BOOKING_FLOW_ACTIONS.SET_ERROR:
      return { ...state, isSubmitting: false, error: action.payload };
    case BOOKING_FLOW_ACTIONS.RESET:
      return { ...initialBookingFlowState };
    default:
      return state;
  }
}
