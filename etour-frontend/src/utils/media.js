import { API_BASE_URL } from "../config/env";

/**
 * Media URL helpers shared by the tour Video/Map tabs and the showcase
 * carousel, so the "is this a YouTube link?" logic lives in exactly one place.
 */

/**
 * Turns a stored media path into one the browser can actually load.
 *
 * The backend stores uploads as a ROOT-RELATIVE path ("/uploads/x.svg").
 * In the browser that resolves against the page origin - which in development
 * is Vite on :5173, not the API on :8080 - so every uploaded image, icon and
 * SVG 404s. Prefixing the API base URL fixes it in dev and stays correct in
 * production, where API_BASE_URL is the deployed API origin.
 *
 * Absolute URLs (http/https), data URIs and blob URLs are returned untouched,
 * so externally-hosted images keep working.
 *
 * @param {string|null|undefined} path
 * @returns {string|null} loadable URL, or null when there's nothing to show
 */
export function resolveMediaUrl(path) {
  if (!path) return null;

  const trimmed = String(path).trim();
  if (!trimmed) return null;

  // Already absolute, or an inline/blob source - leave it alone.
  if (/^(https?:)?\/\//i.test(trimmed) || /^(data|blob):/i.test(trimmed)) {
    return trimmed;
  }

  const base = (API_BASE_URL || "").replace(/\/+$/, "");
  const suffix = trimmed.startsWith("/") ? trimmed : `/${trimmed}`;
  return `${base}${suffix}`;
}

const YOUTUBE_HOSTS = ["youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be"];
const VIMEO_HOSTS = ["vimeo.com", "www.vimeo.com", "player.vimeo.com"];

function safeUrl(url) {
  try {
    return new URL(url, window.location.origin);
  } catch {
    return null;
  }
}

export function getVideoKind(url) {
  if (!url) return "unknown";
  const parsed = safeUrl(url);
  if (!parsed) return "file";
  if (YOUTUBE_HOSTS.includes(parsed.hostname)) return "youtube";
  if (VIMEO_HOSTS.includes(parsed.hostname)) return "vimeo";
  return "file";
}

/** Returns an embeddable URL for YouTube/Vimeo, or null for a direct file. */
export function toEmbedUrl(url) {
  const parsed = safeUrl(url);
  if (!parsed) return null;

  const kind = getVideoKind(url);

  if (kind === "youtube") {
    // Both youtu.be/<id> and youtube.com/watch?v=<id> forms.
    const id = parsed.hostname === "youtu.be"
      ? parsed.pathname.replace("/", "")
      : parsed.searchParams.get("v");
    return id ? `https://www.youtube-nocookie.com/embed/${id}` : null;
  }

  if (kind === "vimeo") {
    const id = parsed.pathname.split("/").filter(Boolean).pop();
    return id ? `https://player.vimeo.com/video/${id}` : null;
  }

  return null;
}

/** True when the URL points at a Google Maps page/embed. */
export function isGoogleMapsUrl(url) {
  const parsed = safeUrl(url);
  if (!parsed) return false;
  return parsed.hostname.includes("google.") && parsed.pathname.includes("/maps");
}

/**
 * Google Maps share links can't be iframed directly - only /maps/embed can.
 * Returns an embeddable URL when the admin already pasted an embed link,
 * otherwise null so the caller can fall back to an external link.
 */
export function toMapEmbedUrl(url) {
  const parsed = safeUrl(url);
  if (!parsed) return null;
  return parsed.pathname.startsWith("/maps/embed") ? url : null;
}

export function isPdf(url, mimeType) {
  if (mimeType?.includes("pdf")) return true;
  return Boolean(url && url.split("?")[0].toLowerCase().endsWith(".pdf"));
}

export function isImageUrl(url, mimeType) {
  if (mimeType?.startsWith("image/")) return true;
  return /\.(png|jpe?g|gif|webp|avif|svg)$/i.test((url || "").split("?")[0]);
}
