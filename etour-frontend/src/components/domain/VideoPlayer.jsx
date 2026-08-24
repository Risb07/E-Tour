import { useState } from "react";
import { Play } from "lucide-react";
import { getVideoKind, toEmbedUrl, resolveMediaUrl } from "../../utils/media";

/**
 * Responsive video player supporting MP4/direct files, YouTube and Vimeo.
 *
 * Embeds are click-to-load: rendering N third-party iframes on mount is slow
 * and drops tracking cookies before the user has asked to watch anything.
 * The <video> element uses preload="none" for the same reason.
 */
export default function VideoPlayer({ url, title = "Tour video", poster }) {
  const [activated, setActivated] = useState(false);
  const kind = getVideoKind(url);
  const embedUrl = toEmbedUrl(url);

  if (!url) return null;

  // Direct file (MP4/WebM) - the native player is enough. An uploaded file is
  // stored root-relative, so it needs resolving against the API origin;
  // YouTube/Vimeo URLs are already absolute and pass through untouched.
  if (kind === "file" || !embedUrl) {
    const fileUrl = resolveMediaUrl(url);
    return (
      <video
        controls
        preload="none"
        poster={resolveMediaUrl(poster)}
        className="aspect-video w-full rounded-card bg-ink-900 shadow-soft"
      >
        <source src={fileUrl} />
        Your browser can&apos;t play this video.{" "}
        <a href={fileUrl} className="underline">
          Download it instead
        </a>
        .
      </video>
    );
  }

  if (!activated) {
    return (
      <button
        onClick={() => setActivated(true)}
        aria-label={`Play ${title}`}
        className="group relative flex aspect-video w-full items-center justify-center overflow-hidden rounded-card bg-ink-900 shadow-soft"
      >
        {poster && (
          <img
            src={resolveMediaUrl(poster)}
            alt=""
            loading="lazy"
            className="absolute inset-0 h-full w-full object-cover opacity-60"
          />
        )}
        <span className="relative flex h-14 w-14 items-center justify-center rounded-full bg-white/90 text-ink-900 transition-transform group-hover:scale-110">
          <Play className="ml-0.5 h-6 w-6 fill-current" />
        </span>
        <span className="absolute bottom-3 left-4 text-sm font-medium text-white">{title}</span>
      </button>
    );
  }

  return (
    <iframe
      src={`${embedUrl}?autoplay=1`}
      title={title}
      loading="lazy"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
      allowFullScreen
      className="aspect-video w-full rounded-card bg-ink-900 shadow-soft"
    />
  );
}
