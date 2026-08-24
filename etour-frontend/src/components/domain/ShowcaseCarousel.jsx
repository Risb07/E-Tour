import { useState, useEffect, useCallback } from "react";
import { ChevronLeft, ChevronRight, Maximize2, X, FileText } from "lucide-react";
import { isPdf, isImageUrl, getVideoKind, resolveMediaUrl } from "../../utils/media";
import VideoPlayer from "./VideoPlayer";

/**
 * BRD 3.1 showcase area. Plays whatever the admin uploaded: images, video
 * (MP4/YouTube/Vimeo), audio, or a PDF.
 *
 * Media type is inferred from the banner's own mediaType when present, and
 * otherwise from the URL - AdBanner has no mediaType column, so the URL is
 * the only signal available for existing rows.
 */
function classify(item) {
  const url = item.imageUrl || item.filePath || "";
  const explicit = (item.mediaType || "").toUpperCase();

  if (explicit === "VIDEO" || explicit === "AUDIO" || explicit === "PDF" || explicit === "IMAGE") {
    return explicit;
  }
  if (isPdf(url, item.mimeType)) return "PDF";
  if (/\.(mp3|wav|ogg|m4a)$/i.test(url.split("?")[0])) return "AUDIO";
  if (getVideoKind(url) !== "file" || /\.(mp4|webm|mov)$/i.test(url.split("?")[0])) return "VIDEO";
  if (isImageUrl(url, item.mimeType)) return "IMAGE";
  return "IMAGE";
}

export default function ShowcaseCarousel({ items = [] }) {
  const [index, setIndex] = useState(0);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const count = items.length;
  const go = useCallback((delta) => setIndex((i) => (i + delta + count) % count), [count]);

  // Arrow keys move between slides; Escape leaves fullscreen.
  useEffect(() => {
    function onKey(e) {
      if (e.key === "Escape") setIsFullscreen(false);
      if (count < 2) return;
      if (e.key === "ArrowLeft") go(-1);
      if (e.key === "ArrowRight") go(1);
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [go, count]);

  if (count === 0) {
    return (
      <div className="flex aspect-video w-full items-center justify-center rounded-2xl bg-white/5">
        <p className="text-ink-300">Every great journey starts with one honest plan.</p>
      </div>
    );
  }

  const current = items[index];
  const kind = classify(current);
  // Stored uploads are root-relative, which would resolve against the Vite
  // dev origin instead of the API. resolveMediaUrl fixes that and leaves
  // absolute/external URLs untouched.
  const url = resolveMediaUrl(current.imageUrl || current.filePath);

  const slide = <ShowcaseSlide kind={kind} url={url} title={current.title} />;

  return (
    <>
      <div className="relative w-full">
        <div className="overflow-hidden rounded-2xl shadow-lifted">{slide}</div>

        {kind !== "VIDEO" && (
          <button
            onClick={() => setIsFullscreen(true)}
            aria-label="View fullscreen"
            className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-black/50 text-white backdrop-blur transition-colors hover:bg-black/70"
          >
            <Maximize2 className="h-4 w-4" />
          </button>
        )}

        {count > 1 && (
          <>
            <CarouselButton side="left" onClick={() => go(-1)} />
            <CarouselButton side="right" onClick={() => go(1)} />
            <div className="mt-4 flex items-center justify-center gap-2">
              {items.map((item, i) => (
                <button
                  key={item.adId ?? item.mediaId ?? i}
                  onClick={() => setIndex(i)}
                  aria-label={`Go to slide ${i + 1}`}
                  aria-current={i === index ? "true" : undefined}
                  className={`h-2 rounded-full transition-all ${
                    i === index ? "w-6 bg-amber-400" : "w-2 bg-white/40 hover:bg-white/70"
                  }`}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {isFullscreen && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label={current.title || "Media preview"}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 p-4"
          onClick={() => setIsFullscreen(false)}
        >
          <button
            onClick={() => setIsFullscreen(false)}
            aria-label="Close preview"
            className="absolute right-5 top-5 flex h-10 w-10 items-center justify-center rounded-full bg-white/10 text-white hover:bg-white/20"
          >
            <X className="h-5 w-5" />
          </button>
          <div className="max-h-full w-full max-w-5xl" onClick={(e) => e.stopPropagation()}>
            <ShowcaseSlide kind={kind} url={url} title={current.title} fullscreen />
          </div>
        </div>
      )}
    </>
  );
}

function ShowcaseSlide({ kind, url, title, fullscreen = false }) {
  if (kind === "VIDEO") {
    return <VideoPlayer url={url} title={title || "Showcase video"} />;
  }

  if (kind === "AUDIO") {
    return (
      <div className="flex aspect-video w-full flex-col items-center justify-center gap-4 bg-ink-800 p-8">
        <p className="text-center font-display text-lg font-bold text-white">{title || "Listen"}</p>
        <audio controls preload="none" className="w-full max-w-md">
          <source src={url} />
          Your browser does not support audio playback.
        </audio>
      </div>
    );
  }

  if (kind === "PDF") {
    return (
      <div className={fullscreen ? "h-[85vh] w-full" : "aspect-video w-full"}>
        <object data={url} type="application/pdf" className="h-full w-full bg-white">
          {/* Fallback for browsers with no inline PDF viewer (common on mobile). */}
          <div className="flex h-full flex-col items-center justify-center gap-3 bg-ink-800 p-8 text-center">
            <FileText className="h-10 w-10 text-amber-400" />
            <p className="text-sm text-ink-200">This brochure can&apos;t be previewed here.</p>
            <a
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-pill bg-amber-500 px-5 py-2 text-sm font-semibold text-ink-900"
            >
              Open PDF
            </a>
          </div>
        </object>
      </div>
    );
  }

  return (
    <img
      src={url}
      alt={title || ""}
      loading="lazy"
      className={
        fullscreen
          ? "max-h-[85vh] w-full object-contain"
          : "max-h-[50vh] w-full object-cover"
      }
    />
  );
}

function CarouselButton({ side, onClick }) {
  const Icon = side === "left" ? ChevronLeft : ChevronRight;
  return (
    <button
      onClick={onClick}
      aria-label={side === "left" ? "Previous slide" : "Next slide"}
      className={`absolute top-1/2 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-black/50 text-white backdrop-blur transition-colors hover:bg-black/70 ${
        side === "left" ? "left-3" : "right-3"
      }`}
    >
      <Icon className="h-5 w-5" />
    </button>
  );
}
