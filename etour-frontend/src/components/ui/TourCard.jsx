import { Calendar, Clock, Users, Heart, ImageOff } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import Badge from "./Badge";
import RatingStars from "./RatingStars";
import { formatCurrency, formatDate } from "../../utils/format";
import { TOUR_CODE_LABELS } from "../../constants/enums";
import { ROUTE_PATHS } from "../../constants/routes";
import { resolveMediaUrl } from "../../utils/media";
import { useWishlist } from "../../hooks/useWishlist";
import { useToast } from "../../hooks/useToast";

/**
 * Premium tour card for listing grids. `tour` is a raw Tour entity from
 * the backend (title, basePrice, tourCode, durationDays, ...); `schedule`
 * is optional - when present (e.g. nearest upcoming departure) it shows
 * departure date and seats, per the BRD's Tour Listing requirements.
 */
export default function TourCard({ tour, schedule, rating, totalReviews, imageUrl: imageUrlProp }) {
  const { isSaved, toggle, canUseWishlist } = useWishlist();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [isToggling, setIsToggling] = useState(false);
  const isFavorite = isSaved(tour.tourId);
  // Comes from tour_media in the database. No stock-photo fallback: if a tour
  // has no image uploaded, we show a neutral placeholder so it's obvious the
  // media is missing rather than disguising it with an unrelated photo.
  const imageUrl = resolveMediaUrl(imageUrlProp || tour.imageUrl);

  async function handleToggleWishlist(event) {
    event.preventDefault();
    event.stopPropagation();

    // Saving requires an account - send them to log in rather than failing
    // silently or pretending it saved.
    if (!canUseWishlist) {
      navigate(ROUTE_PATHS.LOGIN, { state: { from: ROUTE_PATHS.tourDetails(tour.tourId) } });
      return;
    }

    setIsToggling(true);
    try {
      const nowSaved = await toggle(tour.tourId);
      showToast(nowSaved ? "Saved to your wishlist." : "Removed from your wishlist.", "success");
    } catch (err) {
      showToast(err.message || "Couldn't update your wishlist.", "error");
    } finally {
      setIsToggling(false);
    }
  }

  return (
    // h-full makes every card fill its grid cell, so a row stays level no
    // matter how much text each card carries.
    <article className="group flex h-full flex-col overflow-hidden rounded-card bg-white shadow-card transition-all duration-300 hover:-translate-y-1 hover:shadow-lifted">
      {/* Fixed 4:3 ratio rather than a pixel height - images stay consistent
          across every breakpoint instead of stretching on wide screens. */}
      <div className="relative aspect-[4/3] overflow-hidden bg-ink-100">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={tour.title}
            loading="lazy"
            decoding="async"
            className="h-full w-full object-cover transition-transform duration-500 ease-out group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-ink-300">
            <ImageOff className="h-8 w-8" aria-hidden="true" />
            <span className="sr-only">No image available for this tour</span>
          </div>
        )}

        <button
          onClick={handleToggleWishlist}
          disabled={isToggling}
          aria-label={isFavorite ? "Remove from wishlist" : "Add to wishlist"}
          aria-pressed={isFavorite}
          className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-white/95 text-ink-600 shadow-soft backdrop-blur transition-all duration-200 hover:scale-105 hover:text-red-500 disabled:opacity-60 disabled:hover:scale-100"
        >
          <Heart
            className={`h-4 w-4 transition-colors ${isFavorite ? "fill-red-500 text-red-500" : ""}`}
            aria-hidden="true"
          />
        </button>

        {tour.tourCode && (
          <div className="absolute left-3 top-3">
            <Badge variant="accent">{TOUR_CODE_LABELS[tour.tourCode] || tour.tourCode}</Badge>
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-2.5 p-4">
        <Link
          to={ROUTE_PATHS.tourDetails(tour.tourId)}
          className="line-clamp-2 min-h-[2.75rem] font-display text-base font-bold leading-snug text-ink-900 transition-colors hover:text-amber-600"
        >
          {tour.title}
        </Link>

        {typeof rating === "number" && <RatingStars rating={rating} totalReviews={totalReviews} />}

        {tour.categories?.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {tour.categories.slice(0, 2).map((c) => (
              <Badge key={c.categoryId} variant="info">
                {c.categoryName}
              </Badge>
            ))}
          </div>
        )}

        <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-ink-500">
          <span className="flex items-center gap-1">
            <Clock className="h-3.5 w-3.5" /> {tour.durationDays}D
          </span>
          {schedule && (
            <>
              {/* BRD 3.6 results show both start and end date. */}
              <span className="flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5" />
                {formatDate(schedule.departureDate)}
                {schedule.returnDate && <> &rarr; {formatDate(schedule.returnDate)}</>}
              </span>
              {typeof schedule.availableSeats === "number" && (
                <span className="flex items-center gap-1">
                  <Users className="h-3.5 w-3.5" /> {schedule.availableSeats} left
                </span>
              )}
            </>
          )}
        </div>

        {tour.description && (
          <p className="line-clamp-2 text-sm text-ink-500">{tour.description}</p>
        )}

        {/* mt-auto pins the price row to the bottom, so it lines up across a
            row of cards even when the text above differs in length. */}
        <div className="mt-auto flex items-end justify-between gap-3 border-t border-ink-100 pt-3">
          <div>
            <p className="text-[11px] uppercase tracking-wide text-ink-400">From</p>
            <p className="font-display text-lg font-bold leading-tight text-ink-900">
              {formatCurrency(tour.basePrice)}
            </p>
          </div>
          <Link
            to={ROUTE_PATHS.tourDetails(tour.tourId)}
            className="rounded-pill bg-ink-900 px-4 py-2 text-xs font-semibold text-white transition-colors duration-200 hover:bg-amber-500 hover:text-ink-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 focus-visible:ring-offset-2 group-hover:bg-amber-500 group-hover:text-ink-900"
          >
            View details
          </Link>
        </div>
      </div>
    </article>
  );
}

