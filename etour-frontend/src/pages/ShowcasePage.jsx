import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchAdBanners } from "../services/adBannerService";
import { ROUTE_PATHS } from "../constants/routes";
import Loader from "../components/common/Loader";
import ShowcaseCarousel from "../components/domain/ShowcaseCarousel";
import { resolveMediaUrl } from "../utils/media";

/**
 * BRD 3.1 - Showcase page: the site's actual entry point. Plays/shows
 * TourIndia's advertisements (banners, managed entirely through the
 * database - never hard-coded) with corner banner slots and a "Skip info"
 * button that jumps straight to Home.
 */
// Corner ad slots (BRD 3.1 banners 1-4). Kept module-level so the carousel
// can exclude them from the main showcase area.
const CORNER_LAYOUT = [
  { position: "TOP_LEFT", className: "left-4 top-4" },
  { position: "TOP_RIGHT", className: "right-4 top-4" },
  { position: "BOTTOM_LEFT", className: "left-4 bottom-24" },
  { position: "BOTTOM_RIGHT", className: "right-4 bottom-24" },
];
const CORNER_POSITIONS = CORNER_LAYOUT.map((c) => c.position);

export default function ShowcasePage() {
  const navigate = useNavigate();
  const [banners, setBanners] = useState([]);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    fetchAdBanners()
      .then(setBanners)
      .catch(() => setBanners([]))
      .finally(() => setStatus("done"));
  }, []);

  function goHome() {
    navigate(ROUTE_PATHS.HOME);
  }

  function bannerAt(position) {
    return banners.find((b) => b.position === position);
  }

  const corners = CORNER_LAYOUT;

  // Everything positioned SHOWCASE plays in the carousel; if an admin hasn't
  // tagged anything that way, fall back to whatever banners exist so the page
  // is never empty.
  const showcaseItems = banners.filter((b) => b.position === "SHOWCASE");
  const carouselItems = showcaseItems.length > 0
    ? showcaseItems
    : banners.filter((b) => !CORNER_POSITIONS.includes(b.position));

  if (status === "loading") return <Loader label="Loading..." />;

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-ink-900 text-white">
      <div className="absolute inset-x-0 top-0 flex items-center justify-center gap-2 py-6">
        <span className="font-display text-2xl font-bold tracking-wide">eTour</span>
        <span className="text-xs uppercase tracking-widest text-ink-300">TourIndia Travels</span>
      </div>

      {corners.map((corner) => {
        const banner = bannerAt(corner.position);
        if (!banner) return null;
        return (
          <a
            key={corner.position}
            href={banner.linkUrl || "#"}
            className={`absolute hidden h-24 w-40 overflow-hidden rounded-lg border border-white/10 shadow-lifted sm:block ${corner.className}`}
          >
            <img src={resolveMediaUrl(banner.imageUrl)} alt={banner.title} className="h-full w-full object-cover" />
          </a>
        );
      })}

      <div className="relative mx-auto flex max-w-2xl flex-col items-center px-6 text-center">
        <ShowcaseCarousel items={carouselItems} />

        <button
          onClick={goHome}
          className="mt-10 rounded-pill bg-amber-500 px-8 py-3 text-sm font-bold uppercase tracking-wide text-ink-900 transition-colors hover:bg-amber-400"
        >
          Continue
        </button>
        <button onClick={goHome} className="mt-4 text-sm font-medium text-ink-300 underline hover:text-white">
          Skip info
        </button>
      </div>

      <div className="absolute bottom-4 flex flex-col items-center gap-2 text-xs text-ink-400">
        <p>Contact Us · Site map · Career</p>
        <p>&copy; {new Date().getFullYear()} TourIndia Travels Pvt Ltd. All rights reserved.</p>
      </div>
    </div>
  );
}
