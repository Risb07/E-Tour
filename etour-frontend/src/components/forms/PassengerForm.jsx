import Input from "../common/Input";
import { formatCurrency } from "../../utils/format";
import { PASSENGER_TYPES } from "../../constants/enums";
import {
  occupancyOptionsFor,
  passengerTypeLabel,
  passengerTypeOf,
  priceForPassenger,
  withDob,
  withOccupancy,
} from "../../utils/passengerPricing";

const GENDER_OPTIONS = [
  { value: "M", label: "Male" },
  { value: "F", label: "Female" },
  { value: "O", label: "Other" },
];

// How each band is described next to the derived badge. There is no picker -
// date of birth decides the band (see passengerTypeOf), and the server
// derives it the same way.
const PASSENGER_TYPE_HINTS = {
  [PASSENGER_TYPES.ADULT]: "12 years and over",
  [PASSENGER_TYPES.CHILD]: "2 to 12 years",
  [PASSENGER_TYPES.INFANT]: "Under 2 · travels free",
};

export default function PassengerForm({
  index,
  passenger,
  onChange,
  errors = {},
  departureDate,
  // Admin-configured supplements for this tour, keyed by occupancy.
  // Absent = the tour has no room charges set up, so no prices are shown.
  roomCharges = {},
  // Active cost sheet + flat seat price, so each category can show what it
  // actually costs. Absent = prices are simply not shown.
  tourCost = null,
  fallbackPerSeatPrice,
  // The band this passenger was sold as on the tour page (ADULT or CHILD).
  // Shown next to the heading so the form matches what was chosen; null for
  // bookings made before the party split existed.
  expectedBand = null,
}) {
  function update(field, value) {
    onChange(index, { ...passenger, [field]: value });
  }

  // A new date of birth can move the passenger into a different band, which
  // in turn can invalidate the category they were holding - withDob does both
  // in one step so the options and the price below react immediately.
  function updateDob(dob) {
    onChange(index, withDob(passenger, dob, departureDate));
  }

  // Date of birth is the single source of truth for the band; there is
  // nothing to pick and nothing to override.
  const passengerType = passengerTypeOf(passenger, departureDate);
  const isInfant = passengerType === PASSENGER_TYPES.INFANT;
  const isPrimary = index === 0;

  const occupancyOptions = occupancyOptionsFor(passengerType);
  const pricingContext = { tourCost, roomCharges, fallbackPerSeatPrice, departureDate };
  const { price } = priceForPassenger(passenger, pricingContext);

  return (
    <div className="rounded-card bg-white p-5 shadow-soft">
      <div className="flex items-center justify-between gap-2">
        <h4 className="font-semibold text-ink-900">
          Passenger {index + 1}
          {expectedBand && (
            <span className="ml-1.5 font-normal text-ink-400">({passengerTypeLabel(expectedBand)})</span>
          )}
        </h4>
        <div className="flex items-center gap-2">
          {/* Derived from the date of birth below, never chosen. Shown so the
              traveller can see which band - and therefore which fare - their
              date of birth put them in. */}
          {passenger.dob && (
            <span
              className="rounded-pill bg-amber-50 px-2.5 py-1 text-[11px] font-semibold text-amber-700"
              title={`Based on age at departure · ${PASSENGER_TYPE_HINTS[passengerType]}`}
            >
              {passengerTypeLabel(passengerType)}
            </span>
          )}
          {price !== null && (
            <span className="rounded-pill bg-ink-50 px-2.5 py-1 text-[11px] font-semibold text-ink-700">
              {price === 0 ? "Free" : formatCurrency(price)}
            </span>
          )}
        </div>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          label="Full name"
          required
          value={passenger.fullName}
          onChange={(e) => update("fullName", e.target.value)}
          error={errors.fullName}
        />
        <div>
          <label className="text-sm font-medium text-ink-700">Gender</label>
          <select
            value={passenger.gender}
            onChange={(e) => update("gender", e.target.value)}
            className="mt-1.5 w-full rounded-xl border border-ink-200 px-3.5 py-2.5 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
          >
            <option value="">Select</option>
            {GENDER_OPTIONS.map((g) => (
              <option key={g.value} value={g.value}>
                {g.label}
              </option>
            ))}
          </select>
        </div>
        <Input
          label="Date of birth"
          type="date"
          required
          value={passenger.dob}
          onChange={(e) => updateDob(e.target.value)}
          error={errors.dob}
        />
        <Input
          label="Nationality (2-letter code)"
          placeholder="IN"
          value={passenger.nationality}
          onChange={(e) => update("nationality", e.target.value.toUpperCase().slice(0, 2))}
        />
        <Input
          label="ID proof type"
          placeholder="Passport / Aadhaar"
          value={passenger.idProofType}
          onChange={(e) => update("idProofType", e.target.value)}
        />
        <Input
          label="ID proof number"
          required
          value={passenger.idProofNumber}
          onChange={(e) => update("idProofNumber", e.target.value)}
          error={errors.idProofNumber}
        />
      </div>

      {/* Step 2: the occupancy category, scoped to the type chosen above.
          Adult options never render for a child and child options never
          render for an adult, so an invalid pairing cannot be selected. */}
      {occupancyOptions.length > 0 && (
        <div className="mt-4">
          <label className="text-sm font-medium text-ink-700">
            {passengerType === PASSENGER_TYPES.CHILD ? "Child occupancy" : "Room option"}{" "}
            <span className="text-rose-500">*</span>
          </label>
          {!passenger.dob && (
            <p className="mt-1 text-xs text-ink-400">
              Enter a date of birth above - these options update to match the passenger&apos;s age.
            </p>
          )}
          <div
            className={`mt-1.5 grid gap-2 ${
              occupancyOptions.length > 2 ? "grid-cols-1 sm:grid-cols-3" : "grid-cols-1 sm:grid-cols-2"
            }`}
          >
            {occupancyOptions.map((option) => {
              const selected = passenger.occupancy === option.value;
              const optionPrice = priceForPassenger(
                { ...passenger, occupancy: option.value },
                pricingContext
              ).price;
              return (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => onChange(index, withOccupancy(passenger, option.value))}
                  aria-pressed={selected}
                  className={`rounded-xl border px-3 py-2 text-left text-xs transition-colors ${
                    selected
                      ? "border-amber-400 bg-amber-50 text-ink-900"
                      : "border-ink-200 text-ink-600 hover:border-ink-300"
                  }`}
                >
                  <span className="block font-semibold">{option.label}</span>
                  <span className="block text-[11px] text-ink-400">{option.hint}</span>
                  {/* Only shown once this tour has prices to show - a blank
                      or ₹0 on every option would be noise. */}
                  {optionPrice !== null && optionPrice > 0 && (
                    <span className="mt-1 block font-semibold text-amber-700">
                      {formatCurrency(optionPrice)}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
          {errors.occupancy && <p className="mt-1.5 text-xs text-rose-600">{errors.occupancy}</p>}
        </div>
      )}

      {isInfant && (
        <p className="mt-4 rounded-xl bg-emerald-50 px-3 py-2 text-xs text-emerald-700">
          Infants travel free and do not occupy a bed, so there is no occupancy to choose.
        </p>
      )}

      {/* BRD 3.7 collects the primary passenger's address. Only shown for
          passenger 1 so a family doesn't retype the same address. */}
      {isPrimary && (
        <div className="mt-5 border-t border-ink-100 pt-5">
          <h5 className="text-xs font-bold uppercase tracking-wide text-ink-400">Contact address</h5>
          <div className="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <Input
                label="Address line 1"
                required
                value={passenger.addressLine1 || ""}
                onChange={(e) => update("addressLine1", e.target.value)}
                error={errors.addressLine1}
              />
            </div>
            <div className="sm:col-span-2">
              <Input
                label="Address line 2"
                placeholder="Optional"
                value={passenger.addressLine2 || ""}
                onChange={(e) => update("addressLine2", e.target.value)}
              />
            </div>
            <Input
              label="City"
              required
              value={passenger.city || ""}
              onChange={(e) => update("city", e.target.value)}
              error={errors.city}
            />
            <Input
              label="State"
              value={passenger.state || ""}
              onChange={(e) => update("state", e.target.value)}
            />
            <Input
              label="Country"
              value={passenger.country || ""}
              onChange={(e) => update("country", e.target.value)}
            />
            <Input
              label="Pincode"
              required
              value={passenger.pincode || ""}
              onChange={(e) => update("pincode", e.target.value)}
              error={errors.pincode}
            />
          </div>
        </div>
      )}
    </div>
  );
}
