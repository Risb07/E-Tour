import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Clock,
  Users,
  Utensils,
  Route as RouteIcon,
  Image as ImageIcon,
  FileText,
  Sparkles,
  Star,
  FileDown,
  Map as MapIcon,
  Video as VideoIcon,
  Ticket,
} from "lucide-react";
import {
  fetchTourDetails,
  fetchTourStayMeals,
  fetchTourContent,
  fetchTourMedia,
  fetchTourAddons,
} from "../services/tourService";
import { formatCurrency, formatDate } from "../utils/format";
import { TOUR_CONTENT_LABELS, PRICE_TYPE_LABELS } from "../constants/enums";
import { ROUTE_PATHS } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";
import { useBookingFlow } from "../hooks/useBookingFlow";
import { useToast } from "../hooks/useToast";
import { addToCart } from "../services/cartService";
import Breadcrumb from "../components/ui/Breadcrumb";
import Badge from "../components/ui/Badge";
import RatingStars from "../components/ui/RatingStars";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import ErrorState from "../components/common/ErrorState";
import ReviewsSection from "../components/domain/ReviewsSection";
import TourMapPanel from "../components/domain/TourMapPanel";
import VideoPlayer from "../components/domain/VideoPlayer";
import { resolveMediaUrl } from "../utils/media";

// BRD 3.5 left link panel - order matches the BRD's tab list. Cost and
// Journey are deliberately absent: the sidebar quote is the only price shown
// to a visitor, and journey legs duplicate the itinerary.
const SECTIONS = [
  { key: "overview", label: "Overview", icon: FileText },
  { key: "itinerary", label: "Itinerary", icon: RouteIcon },
  { key: "stay", label: "Stay & Meals", icon: Utensils },
  { key: "gallery", label: "Gallery", icon: ImageIcon },
  { key: "map", label: "Map", icon: MapIcon },
  { key: "video", label: "Video", icon: VideoIcon },
  { key: "info", label: "Good to know", icon: FileText },
  { key: "addons", label: "Add-ons", icon: Sparkles },
  { key: "reviews", label: "Reviews", icon: Star },
];

export default function TourDetailsPage() {
  const { tourId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { showToast } = useToast();
  const { startBooking } = useBookingFlow();

  const [details, setDetails] = useState(null);
  const [stayMeals, setStayMeals] = useState([]);
  const [content, setContent] = useState([]);
  const [media, setMedia] = useState([]);
  const [addons, setAddons] = useState([]);
  const [status, setStatus] = useState("loading");

  const [activeTab, setActiveTab] = useState("overview");
  const [selectedScheduleId, setSelectedScheduleId] = useState("");
  // The party is chosen as adults + children, not a single total. The total
  // is derived from them so the two can never disagree.
  const [adultCount, setAdultCount] = useState(1);
  const [childCount, setChildCount] = useState(0);
  const passengerCount = adultCount + childCount;
  const [isAddingToCart, setIsAddingToCart] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");

    // All tab data is fetched in parallel on mount - switching sections is
    // then instant and costs no further requests.
    Promise.all([
      fetchTourDetails(tourId),
      fetchTourStayMeals(tourId),
      fetchTourContent(tourId),
      fetchTourMedia(tourId),
      fetchTourAddons(tourId),
    ])
      .then(([detailsRes, stayRes, contentRes, mediaRes, addonsRes]) => {
        if (cancelled) return;
        setDetails(detailsRes);
        setStayMeals(stayRes);
        setContent(contentRes);
        setMedia(mediaRes);
        setAddons(addonsRes);
        setSelectedScheduleId(detailsRes.schedules?.[0]?.scheduleId || "");
        setStatus("succeeded");
      })
      .catch(() => {
        if (!cancelled) setStatus("failed");
      });

    return () => {
      cancelled = true;
    };
  }, [tourId]);

  if (status === "loading") return <Loader label="Loading tour..." />;
  if (status === "failed" || !details) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16">
        <ErrorState variant="server" message="We couldn't load this tour. It may have been removed." />
      </div>
    );
  }

  const { tour, schedules, itinerary, reviewSummary } = details;
  const selectedSchedule = schedules.find((s) => s.scheduleId === Number(selectedScheduleId));

  // BRD - "Brochure" link on the tour page. Only rendered when an admin has
  // actually uploaded brochure media, so there's never a dead button.
  const brochure = media.find((m) => m.tabContext === "BROCHURE") || null;
  const heroImage = resolveMediaUrl(media.find((m) => m.mediaType === "IMAGE")?.filePath);

  // Every booking needs a lead traveller. Without this a party of children
  // could be booked and, since infants are free, priced at zero.
  const hasAdult = adultCount >= 1;

  function handleBookNow() {
    if (!isAuthenticated) {
      navigate(ROUTE_PATHS.LOGIN, { state: { from: ROUTE_PATHS.tourDetails(tourId) } });
      return;
    }
    if (!selectedSchedule) {
      showToast("Please select a departure date first.", "error");
      return;
    }
    if (!hasAdult) {
      showToast("At least one adult is required for every booking.", "error");
      return;
    }
    if (passengerCount > selectedSchedule.availableSeats) {
      showToast(`Only ${selectedSchedule.availableSeats} seats left on this date.`, "error");
      return;
    }
    startBooking(tour, selectedSchedule, passengerCount, adultCount, childCount);
    navigate(ROUTE_PATHS.bookingForm(tourId));
  }

  async function handleAddToCart() {
    if (!isAuthenticated) {
      navigate(ROUTE_PATHS.LOGIN, { state: { from: ROUTE_PATHS.tourDetails(tourId) } });
      return;
    }
    if (!selectedSchedule) {
      showToast("Please select a departure date first.", "error");
      return;
    }
    if (!hasAdult) {
      showToast("At least one adult is required for every booking.", "error");
      return;
    }
    setIsAddingToCart(true);
    try {
      await addToCart({
        scheduleId: selectedSchedule.scheduleId,
        numberOfPassengers: passengerCount,
        adultCount,
        childCount,
        addons: [],
      });
      showToast("Added to cart.", "success");
      navigate(ROUTE_PATHS.CART);
    } catch (err) {
      showToast(err.message || "Couldn't add this to your cart.", "error");
    } finally {
      setIsAddingToCart(false);
    }
  }

  return (
    <div>
      <div className="relative h-72 bg-ink-800 sm:h-96">
        {/* Hero is the tour's own first IMAGE from tour_media (media[0] could
            be a video or map, which would render broken). If the tour has no
            image, the gradient carries the header on its own - no stock photo. */}
        {heroImage && (
          <img src={heroImage} alt={tour.title} className="h-full w-full object-cover" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-ink-900/80 to-transparent" />
        <div className="absolute inset-x-0 bottom-0 mx-auto max-w-7xl px-4 pb-6 sm:px-6 lg:px-8">
          <Breadcrumb items={[{ label: tour.title }]} />
          <h1 className="mt-2 font-display text-2xl font-bold text-white sm:text-4xl">{tour.title}</h1>
          <div className="mt-2 flex flex-wrap items-center gap-4 text-sm text-white/90">
            <span className="flex items-center gap-1.5">
              <Clock className="h-4 w-4" /> {tour.durationDays} days
            </span>
            {reviewSummary?.totalReviews > 0 && (
              <RatingStars rating={reviewSummary.averageRating || 0} totalReviews={reviewSummary.totalReviews} />
            )}
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[220px_1fr_340px]">
          {/* BRD 3.5 left link panel. Sticky on desktop; on mobile it
              collapses into a horizontally scrollable strip so the same
              markup serves both without a second nav implementation. */}
          <nav aria-label="Tour sections" className="lg:sticky lg:top-20 lg:self-start">
            <ul className="flex gap-1 overflow-x-auto pb-2 lg:flex-col lg:overflow-visible lg:pb-0">
              {SECTIONS.map((section) => {
                const isActive = activeTab === section.key;
                return (
                  <li key={section.key} className="shrink-0 lg:shrink">
                    <button
                      onClick={() => setActiveTab(section.key)}
                      aria-current={isActive ? "true" : undefined}
                      className={[
                        "flex w-full items-center gap-2 rounded-xl px-3.5 py-2.5 text-sm font-semibold transition-colors",
                        isActive
                          ? "bg-amber-50 text-amber-700 lg:border-l-2 lg:border-amber-500 lg:rounded-l-none"
                          : "text-ink-500 hover:bg-ink-50 hover:text-ink-900",
                      ].join(" ")}
                    >
                      <section.icon className="h-4 w-4 shrink-0" />
                      <span className="whitespace-nowrap">{section.label}</span>
                    </button>
                  </li>
                );
              })}
            </ul>

            {/* BRD: the left panel carries a prominent Book Tour link. */}
            <div className="mt-4 hidden lg:block">
              <Button onClick={handleBookNow} disabled={schedules.length === 0} icon={Ticket} className="w-full">
                Book Tour
              </Button>
            </div>
          </nav>

          <div className="min-w-0">
            <div className="animate-fade-in">
              {activeTab === "overview" && (
                <div className="prose prose-sm max-w-none text-ink-600">
                  <p className="whitespace-pre-line">{tour.description}</p>
                  {tour.categories?.length > 0 && (
                    <div className="mt-4 flex flex-wrap gap-2">
                      {tour.categories.map((c) => (
                        <Badge key={c.categoryId} variant="info">
                          {c.categoryName}
                        </Badge>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {activeTab === "itinerary" && <ItineraryTimeline itinerary={itinerary} />}
              {activeTab === "stay" && <StayMealsList stayMeals={stayMeals} />}
              {activeTab === "gallery" && <GalleryGrid media={media} />}
              {activeTab === "map" && <TourMapPanel media={media} />}
              {activeTab === "video" && <VideoList media={media} />}
              {activeTab === "addons" && <AddonsList addons={addons} />}
              {activeTab === "info" && <InfoTabs content={content} />}
              {activeTab === "reviews" && (
                <ReviewsSection tourId={Number(tourId)} reviewSummary={reviewSummary} initialReviews={details.reviews} />
              )}
            </div>
          </div>

          <aside className="lg:sticky lg:top-20 lg:self-start">
            <div className="rounded-card bg-white p-6 shadow-lifted">
              <p className="text-xs uppercase tracking-wide text-ink-400">Starting from</p>
              <p className="font-display text-3xl font-bold text-ink-900">{formatCurrency(tour.basePrice)}</p>
              <p className="text-xs text-ink-400">per person</p>

              <div className="mt-5 flex flex-col gap-4">
                <div>
                  <label className="text-sm font-medium text-ink-700">Departure date</label>
                  {schedules.length === 0 ? (
                    <p className="mt-1.5 text-sm text-ink-400">No upcoming departures scheduled.</p>
                  ) : (
                    <select
                      value={selectedScheduleId}
                      onChange={(e) => setSelectedScheduleId(e.target.value)}
                      className="mt-1.5 w-full rounded-xl border border-ink-200 px-3.5 py-2.5 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
                    >
                      {schedules.map((s) => (
                        <option key={s.scheduleId} value={s.scheduleId}>
                          {formatDate(s.departureDate)} · {s.availableSeats} seats · {formatCurrency(s.price)}
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                {/* Adults and children are chosen here rather than a single
                    total, so the passenger step knows the mix up front and a
                    party with no adult can be refused before booking starts. */}
                <div>
                  <label className="text-sm font-medium text-ink-700">Adults</label>
                  <div className="mt-1.5 flex items-center gap-3">
                    <button
                      onClick={() => setAdultCount((c) => Math.max(0, c - 1))}
                      aria-label="One fewer adult"
                      className="flex h-9 w-9 items-center justify-center rounded-full border border-ink-200 text-ink-600 hover:bg-ink-50"
                    >
                      −
                    </button>
                    <span className="flex items-center gap-1.5 text-sm font-semibold text-ink-900">
                      <Users className="h-4 w-4" /> {adultCount}
                    </span>
                    <button
                      onClick={() => setAdultCount((c) => c + 1)}
                      aria-label="One more adult"
                      className="flex h-9 w-9 items-center justify-center rounded-full border border-ink-200 text-ink-600 hover:bg-ink-50"
                    >
                      +
                    </button>
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-ink-700">Children</label>
                  <div className="mt-1.5 flex items-center gap-3">
                    <button
                      onClick={() => setChildCount((c) => Math.max(0, c - 1))}
                      aria-label="One fewer child"
                      className="flex h-9 w-9 items-center justify-center rounded-full border border-ink-200 text-ink-600 hover:bg-ink-50"
                    >
                      −
                    </button>
                    <span className="flex items-center gap-1.5 text-sm font-semibold text-ink-900">
                      <Users className="h-4 w-4" /> {childCount}
                    </span>
                    <button
                      onClick={() => setChildCount((c) => c + 1)}
                      aria-label="One more child"
                      className="flex h-9 w-9 items-center justify-center rounded-full border border-ink-200 text-ink-600 hover:bg-ink-50"
                    >
                      +
                    </button>
                  </div>
                  <p className="mt-1.5 text-xs text-ink-400">
                    {passengerCount} passenger{passengerCount === 1 ? "" : "s"} in total
                  </p>
                  {adultCount < 1 && (
                    <p className="mt-1.5 text-xs text-rose-600">
                      At least one adult is required for every booking.
                    </p>
                  )}
                </div>

                {selectedSchedule && (
                  <div className="flex items-center justify-between border-t border-ink-100 pt-4 text-sm">
                    <span className="text-ink-500">Estimated total</span>
                    <span className="font-display text-lg font-bold text-ink-900">
                      {formatCurrency(Number(selectedSchedule.price) * passengerCount)}
                    </span>
                  </div>
                )}

                <Button
                  onClick={handleBookNow}
                  size="lg"
                  disabled={schedules.length === 0 || !hasAdult}
                  className="w-full"
                >
                  Book now
                </Button>
                <Button
                  onClick={handleAddToCart}
                  variant="outline"
                  size="lg"
                  isLoading={isAddingToCart}
                  disabled={schedules.length === 0 || !hasAdult}
                  className="w-full"
                >
                  Add to cart
                </Button>

                {brochure && (
                  <a
                    href={resolveMediaUrl(brochure.filePath)}
                    target="_blank"
                    rel="noopener noreferrer"
                    download
                    className="flex w-full items-center justify-center gap-2 rounded-full border-2 border-ink-200 px-6 py-3 text-sm font-semibold text-ink-700 transition-colors hover:border-amber-400 hover:text-amber-600"
                  >
                    <FileDown className="h-4 w-4" /> Download brochure
                  </a>
                )}
              </div>
            </div>
          </aside>
        </div>
      </div>

      <div className="fixed inset-x-0 bottom-0 z-30 border-t border-ink-100 bg-white p-4 shadow-lifted lg:hidden">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-xs text-ink-400">From</p>
            <p className="font-display text-lg font-bold text-ink-900">{formatCurrency(tour.basePrice)}</p>
          </div>
          <Button onClick={handleBookNow} disabled={schedules.length === 0}>
            Book now
          </Button>
        </div>
      </div>
      <div className="h-20 lg:hidden" />
    </div>
  );
}

function ItineraryTimeline({ itinerary }) {
  if (itinerary.length === 0) return <EmptyTab message="Itinerary details coming soon." />;
  return (
    <ol className="flex flex-col gap-6">
      {itinerary
        .slice()
        .sort((a, b) => a.dayNumber - b.dayNumber)
        .map((day) => (
          <li key={day.itineraryId} className="flex gap-4">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-amber-50 text-sm font-bold text-amber-600">
              {day.dayNumber}
            </div>
            <div>
              <h4 className="font-semibold text-ink-900">{day.title}</h4>
              <p className="mt-1 text-sm text-ink-500">{day.description}</p>
            </div>
          </li>
        ))}
    </ol>
  );
}

function StayMealsList({ stayMeals }) {
  if (stayMeals.length === 0) return <EmptyTab message="Stay & meal details coming soon." />;
  return (
    <div className="flex flex-col gap-3">
      {stayMeals
        .slice()
        .sort((a, b) => a.dayNumber - b.dayNumber)
        .map((entry) => (
          <div key={entry.stayMealId} className="rounded-card bg-white p-4 shadow-soft">
            <p className="text-sm font-semibold text-ink-900">
              Day {entry.dayNumber} · {entry.hotelName || entry.locationName || "Stay TBD"}
            </p>
            <div className="mt-2 flex gap-2">
              {entry.breakfast && <Badge variant="success">Breakfast</Badge>}
              {entry.lunch && <Badge variant="success">Lunch</Badge>}
              {entry.dinner && <Badge variant="success">Dinner</Badge>}
              {!entry.breakfast && !entry.lunch && !entry.dinner && <Badge variant="info">No meals included</Badge>}
            </div>
          </div>
        ))}
    </div>
  );
}

/** BRD 3.5 "Video" tab - MP4 / YouTube / Vimeo, lazy-loaded. */
function VideoList({ media }) {
  const videos = media.filter((m) => m.mediaType === "VIDEO");
  if (videos.length === 0) return <EmptyTab message="No videos have been added for this tour yet." />;

  const poster = media.find((m) => m.mediaType === "IMAGE")?.filePath;

  return (
    <div className="flex flex-col gap-5">
      {videos.map((item, index) => (
        <VideoPlayer
          key={item.mediaId}
          url={item.filePath}
          poster={poster}
          title={`Tour video ${videos.length > 1 ? index + 1 : ""}`.trim()}
        />
      ))}
    </div>
  );
}

function GalleryGrid({ media }) {
  const images = media.filter((m) => m.mediaType === "IMAGE");
  if (images.length === 0) return <EmptyTab message="Photos coming soon." />;
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {images.map((item) => (
        <img
          key={item.mediaId}
          src={resolveMediaUrl(item.filePath)}
          alt=""
          loading="lazy"
          className="aspect-square w-full rounded-xl object-cover transition-transform hover:scale-105"
        />
      ))}
    </div>
  );
}

function AddonsList({ addons }) {
  if (addons.length === 0) return <EmptyTab message="No optional add-ons for this tour." />;
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
      {addons.map((addon) => (
        <div key={addon.addonId} className="rounded-card bg-white p-4 shadow-soft">
          <div className="flex items-start justify-between gap-2">
            <h4 className="font-semibold text-ink-900">{addon.addonName}</h4>
            <span className="whitespace-nowrap text-sm font-bold text-amber-600">
              {formatCurrency(addon.price)}
            </span>
          </div>
          <p className="mt-1 text-xs text-ink-500">{addon.description}</p>
          <p className="mt-1 text-[11px] uppercase tracking-wide text-ink-400">
            {PRICE_TYPE_LABELS[addon.priceType]}
          </p>
        </div>
      ))}
    </div>
  );
}

function InfoTabs({ content }) {
  if (content.length === 0) return <EmptyTab message="Additional information coming soon." />;
  return (
    <div className="flex flex-col gap-6">
      {content.map((entry) => (
        <div key={entry.tourContentId}>
          <h4 className="font-display text-base font-bold text-ink-900">
            {TOUR_CONTENT_LABELS[entry.contentType] || entry.contentType}
          </h4>
          <p className="mt-1.5 whitespace-pre-line text-sm text-ink-600">{entry.contentText}</p>
        </div>
      ))}
    </div>
  );
}

function EmptyTab({ message }) {
  return <p className="py-10 text-center text-sm text-ink-400">{message}</p>;
}

