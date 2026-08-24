import { Clock, Mail, MapPin, MessageSquare, Phone } from "lucide-react";
import { useContent } from "../hooks/useContent";
import ContactForm from "../components/forms/ContactForm";

/**
 * Contact page.
 *
 * The company's own details come from the `content` table using the same keys
 * the Footer already reads, so there is one source of truth for the phone
 * number and address rather than a second copy that can drift. Anything the
 * database doesn't provide simply isn't rendered - the same rule the Footer
 * follows.
 */
export default function ContactPage() {
  const { text } = useContent();

  const brand = text("site.brand") || "TourIndia Travels";
  const phone = text("footer.contact.phone");
  const email = text("footer.contact.email");
  const address = text("footer.contact.address");

  const details = [
    phone && {
      icon: Phone,
      label: "Phone",
      value: phone,
      href: `tel:${phone.replace(/[^\d+]/g, "")}`,
    },
    email && {
      icon: Mail,
      label: "Email",
      value: email,
      href: `mailto:${email}`,
    },
    address && {
      icon: MapPin,
      label: "Address",
      value: address,
    },
    {
      icon: Clock,
      label: "Working hours",
      value: "Mon - Sat, 9:30 am - 6:30 pm IST",
    },
  ].filter(Boolean);

  return (
    <div>
      {/* ---------- Hero ---------- */}
      <section className="relative flex min-h-[320px] items-center overflow-hidden bg-ink-900">
        <img
          src="/images/hero.jpg"
          alt=""
          aria-hidden="true"
          className="absolute inset-0 h-full w-full object-cover opacity-30"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-ink-900 via-ink-900/70 to-transparent" />
        <div className="relative mx-auto w-full max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
          <span className="inline-flex items-center gap-2 rounded-pill bg-white/10 px-3.5 py-1.5 text-xs font-semibold uppercase tracking-wide text-amber-300">
            <MessageSquare className="h-3.5 w-3.5" /> Contact us
          </span>
          <h1 className="mt-5 max-w-3xl font-display text-3xl font-extrabold leading-tight text-white sm:text-4xl">
            Talk to someone who has been there
          </h1>
          <p className="mt-4 max-w-2xl text-base text-white/80">
            Questions about a departure, a custom itinerary, or a booking you have already made -
            write in and a real person will get back to you.
          </p>
        </div>
      </section>

      {/* ---------- Details + form ---------- */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[360px_1fr]">
          {/* Company contact information */}
          <aside className="flex flex-col gap-4">
            <div className="rounded-card bg-white p-6 shadow-card">
              <h2 className="font-display text-lg font-bold text-ink-900">{brand}</h2>
              <p className="mt-1 text-sm text-ink-500">
                We answer every enquiry within one working day.
              </p>

              <dl className="mt-6 flex flex-col gap-5">
                {details.map((detail) => (
                  <div key={detail.label} className="flex gap-3.5">
                    <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-50">
                      <detail.icon className="h-4.5 w-4.5 text-amber-600" aria-hidden="true" />
                    </span>
                    <div>
                      <dt className="text-xs font-bold uppercase tracking-wide text-ink-400">
                        {detail.label}
                      </dt>
                      <dd className="mt-0.5 text-sm text-ink-700">
                        {detail.href ? (
                          <a href={detail.href} className="hover:text-amber-600">
                            {detail.value}
                          </a>
                        ) : (
                          detail.value
                        )}
                      </dd>
                    </div>
                  </div>
                ))}
              </dl>
            </div>

            <div className="rounded-card bg-ink-50 p-6">
              <h3 className="font-display text-sm font-bold text-ink-900">Already booked with us?</h3>
              <p className="mt-1.5 text-sm leading-relaxed text-ink-500">
                Quote your order number in the subject line and we&apos;ll pull up your booking before
                we reply.
              </p>
            </div>
          </aside>

          {/* Enquiry form */}
          <ContactForm />
        </div>
      </section>
    </div>
  );
}
