import { useId, useState } from "react";
import { ImageOff } from "lucide-react";
import { resolveMediaUrl } from "../../utils/media";

/**
 * URL field with a live thumbnail preview under the input. Images are
 * referenced by URL/path (no upload endpoint), so the preview is how the
 * admin verifies what they typed before saving.
 */
export default function ImageUrlField({
  label,
  value,
  onChange,
  required = false,
  error,
  placeholder = "https://... or /uploads/...",
  hint,
}) {
  const id = useId();
  const [previewError, setPreviewError] = useState(false);
  const url = typeof value === "string" ? value.trim() : "";

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-ink-700">
        {label}
        {required && <span className="text-amber-600"> *</span>}
      </label>
      <input
        id={id}
        type="text"
        value={value ?? ""}
        onChange={(e) => {
          setPreviewError(false);
          onChange(e.target.value);
        }}
        placeholder={placeholder}
        aria-invalid={Boolean(error)}
        className={[
          "w-full rounded-xl border bg-white py-2.5 pl-3.5 pr-3.5 text-sm text-ink-900 placeholder:text-ink-300",
          "transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
          error ? "border-red-400" : "border-ink-200 focus:border-amber-400",
        ].join(" ")}
      />
      {hint && !error && <p className="text-xs text-ink-400">{hint}</p>}
      {error && <p className="text-xs font-medium text-red-600">{error}</p>}

      {url && !previewError ? (
        <div className="mt-1 overflow-hidden rounded-lg border border-ink-100">
          {/* Resolve so a relative upload path (/uploads/...) previews
              correctly - it would otherwise 404 against the dev server. */}
          <img
            src={resolveMediaUrl(url)}
            alt={label}
            onError={() => setPreviewError(true)}
            className="h-28 w-full object-cover"
          />
        </div>
      ) : url ? (
        <div className="mt-1 flex h-28 flex-col items-center justify-center gap-1 rounded-lg border border-dashed border-ink-200 text-ink-400">
          <ImageOff className="h-5 w-5" aria-hidden="true" />
          <p className="text-xs">Preview unavailable for this URL</p>
        </div>
      ) : null}
    </div>
  );
}
