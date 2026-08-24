import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, ChevronDown, Sparkles } from "lucide-react";
import { useCategories } from "../hooks/useCategories";
import { useContent } from "../hooks/useContent";
import { searchTours } from "../services/tourService";
import { ROUTE_PATHS } from "../constants/routes";
import CategoryCard from "../components/ui/CategoryCard";
import TourCard from "../components/ui/TourCard";
import { SkeletonCard, SkeletonCategoryCard } from "../components/common/Skeleton";
import EmptyState from "../components/common/EmptyState";
import Button from "../components/common/Button";
import Input from "../components/common/Input";
import CrawlingTicker from "../components/domain/CrawlingTicker";
import { resolveMediaUrl } from "../utils/media";

export default function HomePage() {
  const { topLevelCategories, hasSubCategories, status: categoriesStatus } = useCategories();
  // All copy on this page comes from the `content` table. The hero image is a
  // static asset (below), so this page has no use for the hook's media lookup.
  const { text, group } = useContent();
  const [newTours, setNewTours] = useState([]);
  const [toursStatus, setToursStatus] = useState("loading");
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();

  // Content rows for this page. Keys are the contract with the `content`
  // table; anything absent simply isn't rendered.
  const heroImage = "/images/hero.jpg";
  const heroEyebrow = text("home.hero.eyebrow");
  const heroTitle = text("home.hero.title");
  const heroSubtitle = text("home.hero.subtitle");
  const searchPlaceholder = text("home.search.placeholder");
  const whyChooseUs = group("home.why.");
  // group() is a plain key-prefix match, so "home.faq." also picks up the
  // section's own heading rows (home.faq.eyebrow / home.faq.title). Those are
  // rendered by SectionHeading below - left in the list they'd show up as two
  // extra accordion rows labelled with their raw content key.
  const faqs = group("home.faq.").filter((row) => !isHeadingKey(row.contentKey));

  useEffect(() => {
    let cancelled = false;
    setToursStatus("loading");
    // "Newly added" = newest tours by ID, sorted desc. The backend has no
    // is_featured/is_trending flag on Tour, so rather than inventing one
    // client-side, this section is honestly labeled instead of pretending
    // to be editorially curated. See README "Backend dependencies".
    searchTours({ page: 0, size: 8, sortBy: "tourId", sortDir: "desc" })
      .then((data) => {
        if (!cancelled) setNewTours(data.content || []);
      })
      .catch(() => {
        if (!cancelled) setNewTours([]);
      })
      .finally(() => {
        if (!cancelled) setToursStatus("done");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function handleCategoryClick(category) {
    if (hasSubCategories(category.categoryId)) {
      navigate(ROUTE_PATHS.categorySubcategories(category.categoryId));
    } else {
      navigate(ROUTE_PATHS.tourListing(category.categoryId));
    }
  }

  function handleHeroSearch(event) {
    event.preventDefault();
    navigate(`${ROUTE_PATHS.SEARCH}${searchTerm ? `?tourName=${encodeURIComponent(searchTerm)}` : ""}`);
  }

  return (
    <div>
      {/* BRD 3.2 - crawling ticker, just below the top menu bar */}
      <CrawlingTicker />

      {/* Hero */}
      <section className="relative flex min-h-[560px] items-center overflow-hidden bg-ink-900">
        {/* Hero image comes from content.media_url (key: home.hero). With no
            row configured the gradient below stands on its own rather than
            pulling in a stock photo. */}
        {heroImage && (
          <img src={heroImage} alt="" className="absolute inset-0 h-full w-full object-cover" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-ink-900 via-ink-900/70 to-ink-900/30" />

        <div className="relative mx-auto flex w-full max-w-4xl flex-col items-center px-4 py-24 text-center sm:px-6">
          {heroEyebrow && (
            <span className="rounded-pill bg-white/10 px-4 py-1.5 text-xs font-semibold uppercase tracking-wide text-amber-300 backdrop-blur">
              {heroEyebrow}
            </span>
          )}
          {heroTitle && (
            <h1 className="mt-5 font-display text-4xl font-extrabold leading-tight text-white sm:text-5xl">
              {heroTitle}
            </h1>
          )}
          {heroSubtitle && <p className="mt-4 max-w-xl text-ink-100">{heroSubtitle}</p>}

          <form
            onSubmit={handleHeroSearch}
            className="mt-8 flex w-full max-w-xl flex-col gap-3 rounded-card bg-white p-3 shadow-lifted sm:flex-row"
          >
            <div className="flex-1 [&_label]:hidden">
              <Input
                label="Search"
                icon={Search}
                placeholder={searchPlaceholder || ""}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <Button type="submit" size="lg" className="sm:w-auto">
              Search tours
            </Button>
          </form>
        </div>

        <ChevronDown className="absolute bottom-6 left-1/2 h-6 w-6 -translate-x-1/2 animate-bounce text-white/70" />
      </section>

      {/* Categories - white band, alternating against the ink-50 page
          background so consecutive sections read as distinct. */}
      <section className="bg-white">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <SectionHeading eyebrow="Browse" title="Popular categories" />

        {categoriesStatus === "loading" && (
          <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-5">
            {Array.from({ length: 5 }).map((_, i) => (
              <SkeletonCategoryCard key={i} />
            ))}
          </div>
        )}

        {categoriesStatus === "succeeded" && topLevelCategories.length === 0 && (
          <EmptyState title="No categories yet" description="Check back soon - new destinations are being added." />
        )}

        {categoriesStatus === "succeeded" && topLevelCategories.length > 0 && (
          <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-5">
            {topLevelCategories.map((category) => (
              <CategoryCard key={category.categoryId} category={category} onClick={() => handleCategoryClick(category)} />
            ))}
          </div>
        )}
        </div>
      </section>

      {/* Newly added tours - back to the page background so it alternates
          against the white categories band above. */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeading eyebrow="Fresh off the press" title="Newly added tours" />

          {toursStatus === "loading" && (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {Array.from({ length: 4 }).map((_, i) => (
                <SkeletonCard key={i} />
              ))}
            </div>
          )}

          {toursStatus === "done" && newTours.length === 0 && (
            <EmptyState
              title="No tours published yet"
              description="Once tours are added, they'll show up here."
            />
          )}

          {toursStatus === "done" && newTours.length > 0 && (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {newTours.map((row) => (
                <TourCard
                  key={row.tourId}
                  tour={row}
                  imageUrl={row.imageUrl}
                  schedule={
                    row.departureDate
                      ? {
                          departureDate: row.departureDate,
                          returnDate: row.returnDate,
                          availableSeats: row.availableSeats,
                        }
                      : undefined
                  }
                  rating={row.totalReviews > 0 ? row.averageRating : undefined}
                  totalReviews={row.totalReviews}
                />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Why choose us - rows keyed home.why.* in the content table. The whole
          section disappears when none are configured. */}
      {whyChooseUs.length > 0 && (
        <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
          <SectionHeading eyebrow={text("home.why.eyebrow") || ""} title={text("home.why.title") || ""} />
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {whyChooseUs.map((item) => {
              const { title, body } = splitTitleBody(item.contentValue);
              return (
                <div key={item.contentId} className="rounded-card bg-white p-6 shadow-card">
                  <div className="flex h-11 w-11 items-center justify-center overflow-hidden rounded-full bg-amber-50 text-amber-600">
                    {item.mediaUrl ? (
                      <img src={resolveMediaUrl(item.mediaUrl)} alt="" className="h-full w-full object-cover" />
                    ) : (
                      <Sparkles className="h-5 w-5" />
                    )}
                  </div>
                  {title && <h3 className="mt-4 font-display text-base font-bold text-ink-900">{title}</h3>}
                  <p className="mt-1.5 text-sm text-ink-500">{body}</p>
                </div>
              );
            })}
          </div>
        </section>
      )}

      {/* FAQ - rows keyed home.faq.* */}
      {faqs.length > 0 && (
        <section className="bg-white py-16">
          <div className="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
            <SectionHeading
              eyebrow={text("home.faq.eyebrow") || ""}
              title={text("home.faq.title") || ""}
              centered
            />
            <div className="mt-8 flex flex-col divide-y divide-ink-100">
              {faqs.map((row) => {
                const { title, body } = splitTitleBody(row.contentValue);
                // A row with no "::" is body-only. Show it as the question
                // rather than falling back to the raw content key, which is a
                // database identifier and not something a visitor should read.
                return <FaqItem key={row.contentId} question={title || body} answer={title ? body : ""} />;
              })}
            </div>
          </div>
        </section>
      )}
    </div>
  );
}

function SectionHeading({ eyebrow, title, centered = false }) {
  return (
    <div className={`mb-8 ${centered ? "text-center" : ""}`}>
      <p className="text-xs font-bold uppercase tracking-wide text-amber-600">{eyebrow}</p>
      <h2 className="mt-1 font-display text-2xl font-bold text-ink-900 sm:text-3xl">{title}</h2>
    </div>
  );
}

function FaqItem({ question, answer }) {
  const [open, setOpen] = useState(false);
  // An entry with no answer has nothing to expand, so it renders as plain text
  // instead of a control that opens onto a blank space.
  if (!answer) {
    return (
      <div className="py-4">
        <p className="font-semibold text-ink-900">{question}</p>
      </div>
    );
  }
  return (
    <div className="py-4">
      <button
        onClick={() => setOpen((current) => !current)}
        className="flex w-full items-center justify-between text-left"
        aria-expanded={open}
      >
        <span className="font-semibold text-ink-900">{question}</span>
        <ChevronDown className={`h-4 w-4 shrink-0 text-ink-400 transition-transform ${open ? "rotate-180" : ""}`} />
      </button>
      {open && <p className="mt-2 text-sm text-ink-500 animate-fade-in">{answer}</p>}
    </div>
  );
}

/**
 * Keys a repeating group shares with its own section heading. `group()`
 * matches on key prefix alone, so these must be filtered out of the item list
 * or they render as extra, meaningless entries.
 */
const HEADING_SUFFIXES = ["eyebrow", "title", "subtitle", "heading"];

function isHeadingKey(contentKey) {
  const suffix = (contentKey || "").split(".").pop();
  return HEADING_SUFFIXES.includes(suffix);
}

/**
 * The `content` table has a single text column, but repeating blocks (FAQ
 * entries, feature cards) need a heading and a body. Convention: put both in
 * content_value separated by "::".
 *
 *   "Can I cancel a booking? :: Yes, from My Bookings in your dashboard."
 *
 * A value with no "::" is treated as body-only, so existing single-line rows
 * keep working.
 */
function splitTitleBody(value) {
  if (!value) return { title: null, body: "" };
  const idx = value.indexOf("::");
  if (idx === -1) return { title: null, body: value.trim() };
  return {
    title: value.slice(0, idx).trim(),
    body: value.slice(idx + 2).trim(),
  };
}
