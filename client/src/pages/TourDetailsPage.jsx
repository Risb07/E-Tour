import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import TourHero from "../component/TourHero";
import TourSummary from "../component/TourSummary";
import ScheduleSection from "../component/ScheduleSection";
import BookingCard from "../component/BookingCard";
import ItinerarySection from "../component/ItinerarySection";
import ReviewSummary from "../component/ReviewSummary";
import ReviewSection from "../component/ReviewSection";
import LoadingSkeleton from "../component/LoadingSkeleton";

export default function TourDetailsPage() {
  const { tourId } = useParams();
  const navigate = useNavigate();

  const [tourDetails, setTourDetails] = useState(null);
  const [selectedSchedule, setSelectedSchedule] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchTourDetails();
  }, [tourId]);

  async function fetchTourDetails() {
    try {
      setLoading(true);
      setError("");

      // Temporary JWT token
      const token =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZW1vQGdtYWlsLmNvbSIsImlhdCI6MTc4NTM5NDM2NywiZXhwIjoxNzg1NDgwNzY3fQ.BLROYmAAWjbfeA7Dh64tPKgJJfST19XoWLlwwengHSk";

      const tourId = 1; 

      const response = await fetch(
        `http://localhost:8080/api/tours/${tourId}/details`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (!response.ok) {
        throw new Error("Unable to load tour.");
      }

      const data = await response.json();

      console.log("API Response:", data);

      setTourDetails(data);

      if (data.schedules?.length > 0) {
        setSelectedSchedule(data.schedules[0]);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleBooking(schedule) {
    navigate(`/booking/${tourDetails.tour.tourId}/${schedule.scheduleId}`);
  }

  if (loading) {
    return <LoadingSkeleton />;
  }

  if (error) {
    return (
      <div className="container mx-auto px-6 py-20">
        <div className="rounded-3xl border border-red-200 bg-red-50 p-10 text-center">
          <h2 className="text-3xl font-bold text-red-600">
            Something went wrong
          </h2>

          <p className="mt-4 text-red-500">{error}</p>

          <button
            onClick={fetchTourDetails}
            className="mt-8 rounded-xl bg-red-600 px-6 py-3 text-white hover:bg-red-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  if (!tourDetails) return null;

  console.log(tourDetails);

  return (
    <main className="bg-slate-50 min-h-screen">
      {/* Hero */}

      <TourHero
        tour={tourDetails.tour}
        reviewSummary={tourDetails.reviewSummary}
      />

      <div className="mx-auto max-w-[1600px] px-8 py-16">
        <div className="grid gap-10 lg:grid-cols-3">
          {/* LEFT */}

          <div className="space-y-14 lg:col-span-2">
            <TourSummary tour={tourDetails.tour} />

            <ScheduleSection
              schedules={tourDetails.schedules}
              selectedSchedule={selectedSchedule}
              setSelectedSchedule={setSelectedSchedule}
            />

            <ItinerarySection itinerary={tourDetails.itinerary} />

            <ReviewSummary reviewSummary={tourDetails.reviewSummary} />

            <ReviewSection reviews={tourDetails.reviews} />
          </div>

          {/* RIGHT */}

          <div>
            <BookingCard
              tour={tourDetails.tour}
              selectedSchedule={selectedSchedule}
              onBook={handleBooking}
            />
          </div>
        </div>
      </div>
    </main>
  );
}
