import { Compass, Mail, MapPin, Phone } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useToast } from "../../hooks/useToast";
import { useContent } from "../../hooks/useContent";
import { subscribeToNewsletter } from "../../services/newsletterService";
import { fetchNavMenu } from "../../services/navMenuService";
import Input from "../common/Input";
import Button from "../common/Button";

/**
 * Every string and link here comes from the database:
 *   - brand name, tagline, contact details, headings -> `content` table
 *   - quick links                                    -> `nav_menu_item` table
 * Anything the database doesn't provide is simply not rendered, rather than
 * falling back to invented copy.
 */
export default function Footer() {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [links, setLinks] = useState([]);
  const { showToast } = useToast();
  const { text, group } = useContent();

  useEffect(() => {
    fetchNavMenu()
      .then((data) => setLinks(Array.isArray(data) ? data : []))
      .catch(() => setLinks([]));
  }, []);

  const brand = text("site.brand");
  const tagline = text("footer.tagline");
  const phone = text("footer.contact.phone");
  const contactEmail = text("footer.contact.email");
  const address = text("footer.contact.address");
  const copyright = text("footer.copyright");
  const policies = group("footer.policy.");

  const hasContact = phone || contactEmail || address;

  async function handleSubscribe(event) {
    event.preventDefault();
    if (!email.trim()) {
      showToast("Please enter your email address.", "error");
      return;
    }
    setIsSubmitting(true);
    try {
      await subscribeToNewsletter({ email: email.trim() });
      showToast("Thanks for subscribing!", "success");
      setEmail("");
    } catch (err) {
      showToast(err.message || "Couldn't subscribe right now. Please try again.", "error");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <footer className="mt-20 bg-ink-900 text-ink-300">
      <div className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-10 md:grid-cols-4">
          <div>
            <div className="flex items-center gap-2 font-display text-xl font-bold text-white">
              <Compass className="h-6 w-6 text-amber-400" />
              {brand}
            </div>
            {tagline && <p className="mt-3 max-w-xs text-sm">{tagline}</p>}
          </div>

          {links.length > 0 && (
            <div>
              <h4 className="font-display text-sm font-bold uppercase tracking-wide text-white">
                {text("footer.links.heading")}
              </h4>
              <ul className="mt-4 flex flex-col gap-2 text-sm">
                {links.map((item) => (
                  <li key={item.navMenuItemId}>
                    <Link to={item.link} className="hover:text-amber-400">
                      {item.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {hasContact && (
            <div>
              <h4 className="font-display text-sm font-bold uppercase tracking-wide text-white">
                {text("footer.contact.heading")}
              </h4>
              <ul className="mt-4 flex flex-col gap-3 text-sm">
                {phone && (
                  <li className="flex items-center gap-2">
                    <Phone className="h-4 w-4 shrink-0 text-amber-400" /> {phone}
                  </li>
                )}
                {contactEmail && (
                  <li className="flex items-center gap-2">
                    <Mail className="h-4 w-4 shrink-0 text-amber-400" /> {contactEmail}
                  </li>
                )}
                {address && (
                  <li className="flex items-start gap-2">
                    <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-amber-400" /> {address}
                  </li>
                )}
              </ul>
            </div>
          )}

          <div>
            <h4 className="font-display text-sm font-bold uppercase tracking-wide text-white">
              {text("footer.newsletter.heading")}
            </h4>
            {text("footer.newsletter.blurb") && (
              <p className="mt-4 text-sm">{text("footer.newsletter.blurb")}</p>
            )}
            <form onSubmit={handleSubscribe} className="mt-3 flex flex-col gap-2">
              <div className="[&_label]:text-ink-300">
                <Input
                  label=""
                  type="email"
                  placeholder={text("footer.newsletter.placeholder") || ""}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <Button type="submit" size="sm" isLoading={isSubmitting}>
                {text("footer.newsletter.cta") || "Subscribe"}
              </Button>
            </form>
          </div>
        </div>

        <div className="mt-12 flex flex-wrap items-center justify-between gap-3 border-t border-ink-700 pt-6 text-xs text-ink-400">
          {copyright && <span>{copyright}</span>}
          {policies.length > 0 && (
            <ul className="flex flex-wrap gap-4">
              {policies.map((policy) => (
                <li key={policy.contentId}>
                  {policy.linkUrl ? (
                    <Link to={policy.linkUrl} className="hover:text-amber-400">
                      {policy.contentValue}
                    </Link>
                  ) : (
                    policy.contentValue
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </footer>
  );
}
