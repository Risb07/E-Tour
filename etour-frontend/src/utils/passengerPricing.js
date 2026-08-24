import {
  OCCUPANCY,
  OCCUPANCY_LABELS,
  OCCUPANCY_OPTIONS_BY_TYPE,
  PASSENGER_TYPES,
  PASSENGER_TYPE_LABELS,
} from "../constants/enums";

/**
 * Client-side mirror of the backend TourPricingCalculator.
 *
 * The server remains authoritative - BookingSummaryPage still quotes the real
 * price before payment. This module exists so the passenger form can show a
 * per-person price and a running total that update the instant a type or
 * occupancy changes, without a round trip per keystroke.
 *
 * Every rule here (age bands, category-to-column mapping, null fallbacks) is
 * deliberately identical to TourPricingCalculator. If one side changes, the
 * other must too, or the form will disagree with the quote.
 */

// Same boundaries the backend has always used.
const INFANT_MAX_AGE = 2;
const CHILD_MAX_AGE = 12;

/** Age as of the departure date - the date that decides the fare band. */
export function ageAt(dob, onDateIso) {
  if (!dob || !onDateIso) return null;
  const birth = new Date(dob);
  const onDate = new Date(onDateIso);
  if (Number.isNaN(birth.getTime()) || Number.isNaN(onDate.getTime())) return null;

  let age = onDate.getFullYear() - birth.getFullYear();
  const monthDiff = onDate.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && onDate.getDate() < birth.getDate())) {
    age--;
  }
  return age;
}

/**
 * The passenger's band, derived from their date of birth at the departure
 * date. This is the ONLY way a passenger gets categorised - there is no
 * manual override anywhere in the flow, and the server derives it the same
 * way rather than trusting whatever the client sends.
 *
 * An unknown date of birth reads as adult, matching the long-standing
 * server-side guard for incomplete data.
 */
export function derivePassengerType(dob, departureDate) {
  const age = ageAt(dob, departureDate);
  if (age === null) return PASSENGER_TYPES.ADULT;
  if (age < INFANT_MAX_AGE) return PASSENGER_TYPES.INFANT;
  if (age < CHILD_MAX_AGE) return PASSENGER_TYPES.CHILD;
  return PASSENGER_TYPES.ADULT;
}

/**
 * The band for a passenger object. Everything downstream - which occupancy
 * options to render, which rate to apply, whether a saved combination is
 * legal - goes through here, so there is exactly one place that decides.
 */
export function passengerTypeOf(passenger, departureDate) {
  return derivePassengerType(passenger?.dob, departureDate);
}

export function occupancyOptionsFor(passengerType) {
  return OCCUPANCY_OPTIONS_BY_TYPE[passengerType] || [];
}

/**
 * The one rule behind requirement 6: an adult can only hold an adult
 * category, a child only a child category, an infant none at all.
 */
export function isOccupancyValidFor(passengerType, occupancy) {
  if (passengerType === PASSENGER_TYPES.INFANT) return !occupancy;
  if (!occupancy) return false;
  return occupancyOptionsFor(passengerType).some((o) => o.value === occupancy);
}

/**
 * The save-time check: does this passenger's category match the band their
 * date of birth puts them in? Used by both passenger pages so an invalid
 * combination can never be submitted, whatever left it in that state.
 */
export function isPassengerOccupancyValid(passenger, departureDate) {
  return isOccupancyValidFor(passengerTypeOf(passenger, departureDate), passenger?.occupancy);
}

/**
 * Which band the passenger in this position was SOLD as. Slots are positional
 * and mirror how the forms are rendered: the first `adultCount` are the adult
 * slots, the rest are child slots.
 *
 * Returns null when nothing was declared (a booking made before the tour page
 * asked for the split), in which case there is no slot to hold anyone to.
 */
export function expectedBandForSlot(index, adultCount) {
  if (adultCount === null || adultCount === undefined) return null;
  return index < adultCount ? PASSENGER_TYPES.ADULT : PASSENGER_TYPES.CHILD;
}

/**
 * Does this passenger's date of birth match the slot they were sold in?
 * Returns an empty string when it does, or a message explaining the conflict.
 *
 * An infant in a child slot is accepted - both are simply "not an adult". An
 * adult date of birth in a child slot is not, because the party being priced
 * would no longer be the party that was chosen on the tour page. This is also
 * what stops "1 adult" being booked and then filled in with a newborn.
 */
export function slotMismatchMessage(passenger, index, adultCount, departureDate) {
  const expected = expectedBandForSlot(index, adultCount);
  // No declared slot, or no date yet - the "Required" rule covers the latter.
  if (!expected || !passenger?.dob) return "";

  const actual = passengerTypeOf(passenger, departureDate);
  if (expected === PASSENGER_TYPES.ADULT && actual !== PASSENGER_TYPES.ADULT) {
    return "Booked as an adult - enter an adult's date of birth, or change the party on the tour page.";
  }
  if (expected === PASSENGER_TYPES.CHILD && actual === PASSENGER_TYPES.ADULT) {
    return "Booked as a child - enter a child's date of birth, or change the party on the tour page.";
  }
  return "";
}

/** The category a passenger of this type gets before they pick one. */
export function defaultOccupancyFor(passengerType) {
  if (passengerType === PASSENGER_TYPES.INFANT) return null;
  const [first] = occupancyOptionsFor(passengerType);
  return first ? first.value : null;
}

/**
 * Applies a new date of birth and brings the passenger back into a legal
 * state: recategorise from the new date, and if the category they were
 * holding no longer belongs to that band, drop it for the band's default.
 * A still-valid category survives the change untouched.
 *
 * This is what makes the occupancy options and the price react the moment a
 * date is picked, without anything else having to remember to re-check.
 */
export function withDob(passenger, dob, departureDate) {
  const passengerType = derivePassengerType(dob, departureDate);
  const occupancy = isOccupancyValidFor(passengerType, passenger.occupancy)
    ? passenger.occupancy
    : defaultOccupancyFor(passengerType);
  return withOccupancy({ ...passenger, dob }, occupancy);
}

/**
 * Re-runs the same correction without changing the date - for passengers
 * restored from session storage, or seeded before a departure date was known,
 * whose stored category may no longer match their date of birth.
 */
export function reconcilePassenger(passenger, departureDate) {
  return withDob(passenger, passenger.dob, departureDate);
}

/**
 * Sets the occupancy and keeps the legacy `needsExtraBed` flag in step with
 * it. The flag is no longer what prices a child - the category is - but it is
 * still persisted and still used by older clients, so letting the two drift
 * apart would leave a misleading row in the database.
 */
export function withOccupancy(passenger, occupancy) {
  const next = { ...passenger, occupancy };
  if (occupancy === OCCUPANCY.CHILD_WITH_BED) next.needsExtraBed = true;
  if (occupancy === OCCUPANCY.CHILD_WITHOUT_BED) next.needsExtraBed = false;
  return next;
}

/**
 * The tour cost sheet in force for a departure - the same row the backend
 * picks (active, departure date inside the validity window, newest first).
 * Falls back to any active sheet so a tour with an unset validity window
 * still shows prices rather than nothing.
 */
export function activeTourCost(costs, departureDate) {
  if (!costs || costs.length === 0) return null;

  const active = costs.filter((c) => c.status === 1);
  const pool = active.length > 0 ? active : costs;

  if (departureDate) {
    const inWindow = pool.filter(
      (c) =>
        (!c.validFrom || c.validFrom <= departureDate) && (!c.validTo || c.validTo >= departureDate)
    );
    if (inWindow.length > 0) {
      return inWindow.reduce((newest, c) => (c.costId > newest.costId ? c : newest));
    }
  }
  return pool.reduce((newest, c) => (c.costId > newest.costId ? c : newest));
}

function num(value) {
  return value === null || value === undefined || value === "" ? null : Number(value);
}

/**
 * Maps an occupancy category to its TourCost column, applying the same null
 * fallbacks as the server:
 *   Twin sharing      -> basePrice          (the default tour cost)
 *   Single occupancy  -> singlePersonCost   (falls back to twin)
 *   Extra person      -> extraPersonCost    (falls back to twin)
 *   Child with bed    -> childWithBedCost   (falls back to half the seat price)
 *   Child without bed -> childWithoutBedCost
 *   Infant            -> free
 *
 * Returns null when there is nothing to price from at all, so callers can
 * hide prices rather than render a confident ₹0.
 */
export function categoryRate(tourCost, passengerType, occupancy, fallbackPerSeatPrice) {
  if (passengerType === PASSENGER_TYPES.INFANT) return 0;

  const fallback = num(fallbackPerSeatPrice);
  const halfFallback = fallback === null ? null : fallback / 2;

  if (!tourCost) {
    // No cost sheet configured: flat seat price for adults, half for children.
    return passengerType === PASSENGER_TYPES.CHILD ? halfFallback : fallback;
  }

  const twin = num(tourCost.basePrice) ?? fallback;

  switch (occupancy) {
    case OCCUPANCY.SINGLE:
      return num(tourCost.singlePersonCost) ?? twin;
    case OCCUPANCY.TRIPLE:
    case OCCUPANCY.EXTRA_BED:
      return num(tourCost.extraPersonCost) ?? twin;
    case OCCUPANCY.CHILD_WITH_BED:
      return num(tourCost.childWithBedCost) ?? halfFallback;
    case OCCUPANCY.CHILD_WITHOUT_BED:
      return num(tourCost.childWithoutBedCost) ?? halfFallback;
    case OCCUPANCY.TWIN:
    default:
      return twin;
  }
}

/**
 * What one passenger costs: their category rate plus any admin-configured
 * room supplement for that category (normally zero for twin sharing).
 *
 * @returns {{passengerType: string, occupancy: string|null, categoryRate: number|null,
 *   roomCharge: number, price: number|null}}
 */
export function priceForPassenger(
  passenger,
  { tourCost, roomCharges = {}, fallbackPerSeatPrice, departureDate } = {}
) {
  const passengerType = passengerTypeOf(passenger, departureDate);
  const occupancy = isOccupancyValidFor(passengerType, passenger.occupancy)
    ? passenger.occupancy
    : defaultOccupancyFor(passengerType);

  const rate = categoryRate(tourCost, passengerType, occupancy, fallbackPerSeatPrice);
  const supplement = occupancy ? Number(roomCharges[occupancy] ?? 0) : 0;

  return {
    passengerType,
    occupancy,
    categoryRate: rate,
    roomCharge: supplement,
    price: rate === null ? null : rate + supplement,
  };
}

/**
 * Booking total = the sum of every passenger's price. Recomputing from the
 * passenger list (rather than tracking a running figure) is what makes the
 * total self-correcting when a passenger is added, removed, or moved into a
 * different band by a date-of-birth edit.
 */
export function passengersTotal(passengers, context) {
  return (passengers || []).reduce((sum, p) => {
    const { price } = priceForPassenger(p, context);
    return sum + (price ?? 0);
  }, 0);
}

/** True once there is at least one price to show. */
export function hasPricing(context) {
  return Boolean(context?.tourCost) || num(context?.fallbackPerSeatPrice) !== null;
}

export function passengerTypeLabel(type) {
  return PASSENGER_TYPE_LABELS[type] || type || "";
}

export function occupancyLabel(occupancy) {
  return OCCUPANCY_LABELS[occupancy] || "";
}
