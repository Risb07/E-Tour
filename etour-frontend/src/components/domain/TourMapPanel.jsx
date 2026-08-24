import { MapPin, ExternalLink } from "lucide-react";
import { isImageUrl, toMapEmbedUrl } from "../../utils/media";
import { resolveMediaUrl } from "../../utils/media";

/**
 * BRD 3.5 "Map" tab. MAP-type media can be any of three things depending on
 * what the admin uploaded:
 *   - a Google Maps *embed* URL  -> iframe it
 *   - a route image              -> render it
 *   - any other map link         -> offer it as an external link
 *
 * Ordinary Google Maps share links can't be iframed (they refuse framing), so
 * those deliberately fall through to the external-link case rather than
 * rendering a blank box.
 */
export default function TourMapPanel({ media }) {
  const maps = (media || []).filter((m) => m.mediaType === "MAP");

  if (maps.length === 0) {
    return (
      <div className="flex flex-col items-center gap-2 py-12 text-center">
        <MapPin className="h-8 w-8 text-ink-300" />
        <p className="text-sm text-ink-400">No route map has been added for this tour yet.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-5">
      {maps.map((item) => {
        const embedUrl = toMapEmbedUrl(item.filePath);

        if (embedUrl) {
          return (
            <iframe
              key={item.mediaId}
              src={embedUrl}
              title="Tour route map"
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
              allowFullScreen
              className="aspect-video w-full rounded-card border-0 shadow-soft"
            />
          );
        }

        if (isImageUrl(item.filePath, item.mimeType)) {
          return (
            <img
              key={item.mediaId}
              src={resolveMediaUrl(item.filePath)}
              alt="Tour route map"
              loading="lazy"
              className="w-full rounded-card object-contain shadow-soft"
            />
          );
        }

        return (
          <a
            key={item.mediaId}
            href={resolveMediaUrl(item.filePath)}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center justify-between gap-3 rounded-card bg-white p-4 shadow-soft transition-colors hover:text-amber-600"
          >
            <span className="flex items-center gap-2 text-sm font-medium text-ink-900">
              <MapPin className="h-4 w-4 text-amber-500" /> Open route map
            </span>
            <ExternalLink className="h-4 w-4 text-ink-400" />
          </a>
        );
      })}
    </div>
  );
}
