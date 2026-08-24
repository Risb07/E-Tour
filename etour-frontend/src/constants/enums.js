// Mirrors backend enums exactly - label/color maps live here so no
// component has to hardcode a switch statement on a status string.
export const BOOKING_STATUS_LABELS = {
  PENDING: { label: "Pending", variant: "warning" },
  CONFIRMED: { label: "Confirmed", variant: "success" },
  CANCELLED: { label: "Cancelled", variant: "danger" },
  COMPLETED: { label: "Completed", variant: "info" },
};

export const TOUR_CODE_LABELS = {
  ADV: "Adventure",
  INT: "International",
  DEV: "Domestic",
  DOM: "Domestic",
};

export const TOUR_STATUS_LABELS = {
  ACTIVE: { label: "Active", variant: "success" },
  INACTIVE: { label: "Inactive", variant: "warning" },
  DRAFT: { label: "Draft", variant: "info" },
};

export const PRICE_TYPE_LABELS = {
  PER_PERSON: "per person",
  PER_ROOM: "per room",
  PER_BOOKING: "per booking",
};

// Mirrors the backend PassengerType enum. The traveller picks this
// explicitly on the booking form; date of birth only supplies the default.
export const PASSENGER_TYPES = {
  ADULT: "ADULT",
  CHILD: "CHILD",
  INFANT: "INFANT",
};

export const PASSENGER_TYPE_LABELS = {
  ADULT: "Adult",
  CHILD: "Child",
  INFANT: "Infant",
};

// Mirrors the backend Occupancy enum, including the two child categories.
export const OCCUPANCY = {
  TWIN: "TWIN",
  SINGLE: "SINGLE",
  TRIPLE: "TRIPLE",
  EXTRA_BED: "EXTRA_BED",
  CHILD_WITH_BED: "CHILD_WITH_BED",
  CHILD_WITHOUT_BED: "CHILD_WITHOUT_BED",
};

export const OCCUPANCY_LABELS = {
  TWIN: "Twin sharing",
  SINGLE: "Single occupancy",
  TRIPLE: "Triple occupancy",
  EXTRA_BED: "Extra person",
  CHILD_WITH_BED: "Child with bed",
  CHILD_WITHOUT_BED: "Child without bed",
};

/**
 * Which occupancy categories each passenger type may choose, in the order the
 * booking form presents them. This is the list the UI renders from, so adult
 * options can never appear for a child and vice versa.
 *
 * TRIPLE is deliberately absent: it prices identically to EXTRA_BED and is
 * kept in the enum only so existing bookings and room-charge rows still
 * resolve.
 *
 * Infants have no options - they are free and occupy no bed.
 */
export const OCCUPANCY_OPTIONS_BY_TYPE = {
  ADULT: [
    { value: "TWIN", label: "Twin sharing", hint: "2 per room · default tour cost" },
    { value: "SINGLE", label: "Single occupancy", hint: "Room to yourself" },
    { value: "EXTRA_BED", label: "Extra person", hint: "Additional adult sharing" },
  ],
  CHILD: [
    { value: "CHILD_WITH_BED", label: "Child with bed", hint: "Child gets their own bed" },
    { value: "CHILD_WITHOUT_BED", label: "Child without bed", hint: "Shares a bed with an adult" },
  ],
  INFANT: [],
};

export const TOUR_CONTENT_LABELS = {
  PASSPORT_VISA: "Passport & Visa",
  WEATHER: "Weather",
  DOS_DONTS: "Do's & Don'ts",
  TERMS_CONDITIONS: "Terms & Conditions",
};
