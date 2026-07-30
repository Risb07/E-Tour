import React from "react";
import { MapPin, Star } from "lucide-react";

const TourCard = ({ tour }) => {
  return (
    <div className="overflow-hidden bg-white rounded-2xl shadow-md hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 border border-gray-100">
      {/* Image */}
      <div className="relative h-60 overflow-hidden">
        <img
          src={tour.image}
          alt={tour.title}
          className="w-full h-full object-cover hover:scale-110 transition-transform duration-500"
        />

        <span className="absolute top-4 left-4 bg-indigo-600 text-white text-xs font-semibold px-3 py-1 rounded-full">
          {tour.category}
        </span>

        <div className="absolute top-4 right-4 bg-white px-2 py-1 rounded-full flex items-center gap-1 shadow">
          <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
          <span className="text-sm font-semibold">{tour.rating}</span>
        </div>
      </div>

      {/* Body */}
      <div className="p-5 flex flex-col gap-3">
        <h2 className="text-xl font-bold text-gray-800">{tour.title}</h2>

        <div className="flex items-center gap-2 text-gray-500">
          <MapPin className="w-4 h-4 text-indigo-600" />
          <span>{tour.location}</span>
        </div>

        <p className="text-sm text-gray-600 line-clamp-3">
          {tour.description}
        </p>

        <div className="flex justify-between items-center mt-4">
          <div>
            <p className="text-2xl font-bold text-indigo-600">
              ₹{tour.price.toLocaleString()}
            </p>
            <p className="text-sm text-gray-500">
              {tour.duration}
            </p>
          </div>

          <button className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2 rounded-lg transition-all">
            Book Now
          </button>
        </div>
      </div>
    </div>
  );
};

export default TourCard;