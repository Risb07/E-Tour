import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import BookingSummary from "../component/booking/BookingSummary";
import BookingContactForm from "../component/booking/BookingContactForm";
import PassengerDetailsForm from "../component/booking/PassengerDetailsForm";

function BookingPage() {
  const { tourId } = useParams();
  const navigate = useNavigate();

  const [tour, setTour] = useState(null);

  const [loading, setLoading] = useState(false);

  const [customer, setCustomer] = useState({
    fullName: "",
    email: "",
    phone: "",
  });

  const [passengers, setPassengers] = useState([
    {
      id: 1,
      fullName: "",
      gender: "",
      dob: "",
      nationality: "Indian",
      idProofType: "",
      idProofNumber: "",
    },
  ]);

  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchTour();
  }, [tourId]);

  async function fetchTour() {
    try {
      const response = await fetch(`http://localhost:8080/api/tours/${tourId}`);

      if (!response.ok) {
        throw new Error("Failed to fetch tour");
      }

      const data = await response.json();

      setTour(data);
    } catch (error) {
      console.error(error);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();

    const payload = {
      customer,
      passengers,
    };

    console.log(payload);

    try {
      setLoading(true);

      const response = await fetch("http://localhost:8080/api/bookings", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error("Booking Failed");
      }

      const booking = await response.json();

      console.log(booking);

      navigate(`/payment/${booking.bookingId}`);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  if (!tour) {
    return (
      <div className="flex h-screen items-center justify-center">
        Loading Tour...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}

      <div className="border-b bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
          <div>
            <h1 className="text-3xl font-bold text-slate-900">
              Complete Your Booking
            </h1>

            <p className="mt-1 text-slate-500">
              Fill traveller details to continue.
            </p>
          </div>
        </div>
      </div>

      {/* Content */}

      <form
        onSubmit={handleSubmit}
        className="mx-auto grid max-w-7xl gap-8 px-6 py-8 lg:grid-cols-3"
      >
        {/* LEFT */}

        <div className="space-y-8 lg:col-span-2">
          <BookingContactForm
            customer={customer}
            setCustomer={setCustomer}
            errors={errors.customer || {}}
          />

          <PassengerDetailsForm
            passengers={passengers}
            setPassengers={setPassengers}
            errors={errors.passengers || []}
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-blue-600 py-4 text-lg font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-400"
          >
            {loading ? "Processing Booking..." : "Continue To Payment"}
          </button>
        </div>

        {/* RIGHT */}

        <div>
          <BookingSummary tour={tour} passengerCount={passengers.length} />
        </div>
      </form>
    </div>
  );
}

export default BookingPage;
