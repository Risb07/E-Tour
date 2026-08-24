/**
 * Full-area loading indicator. Use inside a page/section, not globally -
 * each feature owns its own loading state per the module's data flow.
 */
export default function Loader({ label = "Loading..." }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-ink-500">
      <span
        className="h-8 w-8 animate-spin rounded-full border-[3px] border-ink-200 border-t-amber-500"
        aria-hidden="true"
      />
      <p className="text-sm">{label}</p>
    </div>
  );
}
