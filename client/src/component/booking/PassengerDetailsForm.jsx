import { Plus } from "lucide-react";
import PassengerCard from "./PassengerCard";

function PassengerDetailsForm({ passengers, setPassengers, errors = {} }) {
  function handlePassengerChange(index, event) {
    const { name, value } = event.target;

    const updatedPassengers = [...passengers];

    updatedPassengers[index] = {
      ...updatedPassengers[index],
      [name]: value,
    };

    setPassengers(updatedPassengers);
  }

  function addPassenger() {
    setPassengers((prev) => [
      ...prev,
      {
        id: Date.now(),
        fullName: "",
        gender: "",
        dob: "",
        nationality: "Indian",
        idProofType: "",
        idProofNumber: "",
      },
    ]);
  }

  function removePassenger(index) {
    if (index === 0) return;

    const updatedPassengers = passengers.filter((_, i) => i !== index);

    setPassengers(updatedPassengers);
  }

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-md">
      {/* Header */}

      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">
            Passenger Details
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Enter details of every traveller included in this booking.
          </p>
        </div>

        <span className="rounded-full bg-blue-100 px-3 py-1 text-sm font-semibold text-blue-700">
          {passengers.length} Traveller
          {passengers.length > 1 ? "s" : ""}
        </span>
      </div>

      {/* Passenger Cards */}

      <div className="space-y-6">
        {passengers.map((passenger, index) => (
          <PassengerCard
            key={passenger.id}
            index={index}
            passenger={passenger}
            errors={errors[index] || {}}
            onChange={handlePassengerChange}
            onRemove={removePassenger}
            removable={index !== 0}
          />
        ))}
      </div>

      {/* Add Passenger */}

      <div className="mt-8">
        <button
          type="button"
          onClick={addPassenger}
          className="flex items-center gap-2 rounded-xl border border-blue-600 px-5 py-3 font-medium text-blue-600 transition hover:bg-blue-600 hover:text-white"
        >
          <Plus size={18} />
          Add Another Passenger
        </button>
      </div>

      {/* Information */}

      <div className="mt-6 rounded-xl border border-amber-200 bg-amber-50 p-4">
        <h4 className="font-semibold text-amber-700">Important</h4>

        <p className="mt-1 text-sm text-slate-600">
          Passenger names should exactly match the government ID that will be
          carried during the trip. Incorrect information may result in boarding
          or booking issues.
        </p>
      </div>
    </div>
  );
}

export default PassengerDetailsForm;
