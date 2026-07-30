import React from "react";
import TourCard from "./TourCard";

const tours = [
  {
    id: 1,
    title: "Goa Beach Escape",
    category: "Beach",
    location: "Goa",
    rating: 4.8,
    duration: "5 Days / 4 Nights",
    price: 15999,
    description:
      "Enjoy golden beaches, nightlife, luxury resorts and thrilling water sports.",
    image:
      "https://images.unsplash.com/photo-1518509562904-e7ef99cdcc86?w=800",
  },
  {
    id: 2,
    title: "Manali Adventure",
    category: "Adventure",
    location: "Himachal Pradesh",
    rating: 4.9,
    duration: "6 Days / 5 Nights",
    price: 18999,
    description:
      "Snow-covered mountains, river rafting, paragliding and scenic valleys.",
    image:
      "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800",
  },
  {
    id: 3,
    title: "Kashmir Paradise",
    category: "Nature",
    location: "Srinagar",
    rating: 4.7,
    duration: "7 Days / 6 Nights",
    price: 24999,
    description:
      "Experience Dal Lake, Gulmarg, Sonmarg and the heavenly beauty of Kashmir.",
    image:
      "https://images.unsplash.com/photo-1605648916319-cf082f7523c4?w=800",
  },
  {
    id: 4,
    title: "Kerala Backwaters",
    category: "Relax",
    location: "Alleppey",
    rating: 4.9,
    duration: "5 Days / 4 Nights",
    price: 21999,
    description:
      "Stay in luxurious houseboats and cruise through Kerala's beautiful backwaters.",
    image:
      "https://images.unsplash.com/photo-1582972236019-ea9b4f8ce4b9?w=800",
  },
  {
    id: 5,
    title: "Rajasthan Heritage",
    category: "Heritage",
    location: "Jaipur",
    rating: 4.6,
    duration: "4 Days / 3 Nights",
    price: 14999,
    description:
      "Explore royal forts, palaces, local culture and the vibrant Pink City.",
    image:
      "https://images.unsplash.com/photo-1599661046289-e31897846e41?w=800",
  },
  {
    id: 6,
    title: "Leh Ladakh Expedition",
    category: "Road Trip",
    location: "Ladakh",
    rating: 5.0,
    duration: "8 Days / 7 Nights",
    price: 32999,
    description:
      "An unforgettable road trip through mountains, lakes and high-altitude passes.",
    image:
      "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800",
  },
];

const Tours = () => {
  return (
    <section className="min-h-screen bg-slate-50 py-16">
      <div className="max-w-7xl mx-auto px-6">
        {/* Heading */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold text-slate-800">
            Explore Tours
          </h1>

          <p className="mt-4 text-gray-500 text-lg">
            Discover the best destinations and unforgettable experiences.
          </p>
        </div>

        {/* Cards */}
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {tours.map((tour) => (
            <TourCard key={tour.id} tour={tour} />
          ))}
        </div>
      </div>
    </section>
  );
};

export default Tours;