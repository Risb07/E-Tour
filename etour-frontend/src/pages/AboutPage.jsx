import { Link } from "react-router-dom";
import {
  Award,
  Compass,
  Eye,
  HeartHandshake,
  Leaf,
  MapPin,
  ShieldCheck,
  Sparkles,
  Target,
  Ticket,
  Users,
  Wallet,
} from "lucide-react";
import { useContent } from "../hooks/useContent";
import { ROUTE_PATHS } from "../constants/routes";
import Button from "../components/common/Button";

/**
 * "Our Story" - the company page linked from the footer.
 *
 * Static copy on purpose: none of this is operational data, so it doesn't
 * belong in the `content` table alongside things an admin edits per campaign.
 * Only the brand name is read from content, so the page can't disagree with
 * the header and footer about what the company is called.
 */

const STATS = [
  { icon: Award, value: "18+", label: "Years of experience" },
  { icon: Users, value: "50,000+", label: "Happy customers" },
  { icon: Ticket, value: "12,000+", label: "Tours completed" },
  { icon: MapPin, value: "120+", label: "Destinations covered" },
];

const WHY_CHOOSE_US = [
  {
    icon: ShieldCheck,
    title: "Transparent pricing",
    body: "Twin sharing, single occupancy, extra person and child fares are published up front. What you see on the tour page is what you pay at checkout.",
  },
  {
    icon: Compass,
    title: "Handpicked itineraries",
    body: "Every route is walked by our own team before it goes on sale, so the days are paced properly and the stays are ones we would book ourselves.",
  },
  {
    icon: HeartHandshake,
    title: "Support that answers",
    body: "A real person on the other end of the phone, before you book and while you travel - not a queue and a ticket number.",
  },
  {
    icon: Wallet,
    title: "No hidden charges",
    body: "Taxes and room supplements are shown on the summary before payment. Nothing is added after you have decided.",
  },
];

const VALUES = [
  {
    icon: HeartHandshake,
    title: "Travellers first",
    body: "If an itinerary would not be good enough for our own families, we do not sell it.",
  },
  {
    icon: ShieldCheck,
    title: "Say it plainly",
    body: "Clear inclusions, clear exclusions, clear costs. No asterisks doing the heavy lifting.",
  },
  {
    icon: Leaf,
    title: "Travel that gives back",
    body: "We work with local guides, local stays and local kitchens, so the money stays where the journey happens.",
  },
  {
    icon: Sparkles,
    title: "Keep getting better",
    body: "Every trip ends with a debrief. What did not work gets fixed before the next departure.",
  },
];

export default function AboutPage() {
  const { text } = useContent();
  const brand = text("site.brand") || "TourIndia Travels";

  return (
    <div>
      {/* ---------- Hero ---------- */}
      <section className="relative flex min-h-[420px] items-center overflow-hidden bg-ink-900">
        <img
          src="/images/hero.jpg"
          alt=""
          aria-hidden="true"
          className="absolute inset-0 h-full w-full object-cover opacity-40"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-ink-900 via-ink-900/70 to-transparent" />
        <div className="relative mx-auto w-full max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
          <span className="inline-flex items-center gap-2 rounded-pill bg-white/10 px-3.5 py-1.5 text-xs font-semibold uppercase tracking-wide text-amber-300">
            <Sparkles className="h-3.5 w-3.5" /> Our story
          </span>
          <h1 className="mt-5 max-w-3xl font-display text-4xl font-extrabold leading-tight text-white sm:text-5xl">
            Eighteen years of showing people the India we grew up in
          </h1>
          <p className="mt-4 max-w-2xl text-base text-white/80 sm:text-lg">
            {brand} started with one minibus and a hand-drawn route map. We still plan every journey
            the same way: on the ground, in person, one road at a time.
          </p>
        </div>
      </section>

      {/* ---------- Stats ---------- */}
      <section className="bg-white">
        <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <dl className="grid grid-cols-2 gap-6 lg:grid-cols-4">
            {STATS.map((stat) => (
              <div key={stat.label} className="rounded-card bg-ink-50 p-6 text-center">
                <stat.icon className="mx-auto h-6 w-6 text-amber-500" aria-hidden="true" />
                <dd className="mt-3 font-display text-3xl font-extrabold text-ink-900">{stat.value}</dd>
                <dt className="mt-1 text-sm text-ink-500">{stat.label}</dt>
              </div>
            ))}
          </dl>
        </div>
      </section>

      {/* ---------- Company story ---------- */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-10 lg:grid-cols-2 lg:items-center">
          <div>
            <span className="text-xs font-bold uppercase tracking-wide text-amber-600">How we started</span>
            <h2 className="mt-1 font-display text-2xl font-bold text-ink-900 sm:text-3xl">
              A family business that never stopped travelling
            </h2>
            <div className="mt-5 flex flex-col gap-4 text-sm leading-relaxed text-ink-600">
              <p>
                In 2008 we ran a single circuit through the Western Ghats for a handful of families who
                could not find a tour that moved at a human pace. There were no brochures. People came
                back, and then they sent their neighbours.
              </p>
              <p>
                That word of mouth is still the business. We grew from one route to more than a hundred
                and twenty destinations, but the test never changed: would we put our own parents on
                this bus, in this hotel, on this schedule?
              </p>
              <p>
                Today a small team of planners, guides and drivers builds every itinerary the same way -
                by going there first, staying in the rooms, eating in the kitchens and timing the drives
                before a single seat goes on sale.
              </p>
            </div>
          </div>

          <div className="overflow-hidden rounded-card shadow-lifted">
            <img
              src="/images/hero.jpg"
              alt="Travellers on one of our guided tours"
              className="h-full w-full object-cover"
            />
          </div>
        </div>
      </section>

      {/* ---------- Mission & Vision ---------- */}
      <section className="bg-white">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <div className="rounded-card bg-ink-50 p-8">
              <Target className="h-7 w-7 text-amber-500" aria-hidden="true" />
              <h2 className="mt-4 font-display text-xl font-bold text-ink-900">Our mission</h2>
              <p className="mt-3 text-sm leading-relaxed text-ink-600">
                To make well-planned travel ordinary rather than expensive. Honest prices, itineraries
                that respect the traveller&apos;s time, and support that picks up the phone - so that
                booking a holiday stops being the stressful part of taking one.
              </p>
            </div>

            <div className="rounded-card bg-ink-50 p-8">
              <Eye className="h-7 w-7 text-amber-500" aria-hidden="true" />
              <h2 className="mt-4 font-display text-xl font-bold text-ink-900">Our vision</h2>
              <p className="mt-3 text-sm leading-relaxed text-ink-600">
                To be the operator Indian families recommend without being asked - known less for the
                size of the catalogue than for the fact that nothing goes wrong, and that when it does,
                somebody fixes it before you have to ask.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- Why choose us ---------- */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <span className="text-xs font-bold uppercase tracking-wide text-amber-600">Why travel with us</span>
        <h2 className="mt-1 font-display text-2xl font-bold text-ink-900 sm:text-3xl">
          Why choose {brand}
        </h2>

        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {WHY_CHOOSE_US.map((item) => (
            <div key={item.title} className="rounded-card bg-white p-6 shadow-card">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-50">
                <item.icon className="h-5 w-5 text-amber-600" aria-hidden="true" />
              </span>
              <h3 className="mt-4 font-display text-base font-bold text-ink-900">{item.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-ink-500">{item.body}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ---------- Values ---------- */}
      <section className="bg-white">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
          <span className="text-xs font-bold uppercase tracking-wide text-amber-600">What we stand for</span>
          <h2 className="mt-1 font-display text-2xl font-bold text-ink-900 sm:text-3xl">Our values</h2>

          <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2">
            {VALUES.map((value) => (
              <div key={value.title} className="flex gap-4 rounded-card bg-ink-50 p-6">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white">
                  <value.icon className="h-5 w-5 text-amber-600" aria-hidden="true" />
                </span>
                <div>
                  <h3 className="font-display text-base font-bold text-ink-900">{value.title}</h3>
                  <p className="mt-1.5 text-sm leading-relaxed text-ink-500">{value.body}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- Call to action ---------- */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="rounded-card bg-ink-900 px-6 py-12 text-center sm:px-12">
          <h2 className="font-display text-2xl font-bold text-white sm:text-3xl">
            Ready to plan your next trip?
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-sm text-ink-300">
            Browse our departures, or tell us where you would like to go and we will put together
            something that fits.
          </p>
          <div className="mt-7 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link to={ROUTE_PATHS.HOME}>
              <Button size="lg" icon={Compass}>
                Explore tours
              </Button>
            </Link>
            <Link to={ROUTE_PATHS.CONTACT}>
              <Button size="lg" variant="outline">
                Talk to us
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
