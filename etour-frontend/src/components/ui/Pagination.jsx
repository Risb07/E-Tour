import { ChevronLeft, ChevronRight } from "lucide-react";

/** `page` is 0-based (matches Spring's Pageable), displayed as 1-based. */
export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pageNumbers = Array.from({ length: totalPages }, (_, i) => i).filter(
    (p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 1
  );

  return (
    <nav className="flex items-center justify-center gap-1.5" aria-label="Pagination">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="flex h-9 w-9 items-center justify-center rounded-full text-ink-600 hover:bg-ink-100 disabled:opacity-40"
        aria-label="Previous page"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>

      {pageNumbers.map((p, idx) => (
        <span key={p} className="flex items-center gap-1.5">
          {idx > 0 && pageNumbers[idx - 1] !== p - 1 && <span className="px-1 text-ink-300">…</span>}
          <button
            onClick={() => onPageChange(p)}
            className={[
              "flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold",
              p === page ? "bg-amber-500 text-ink-900" : "text-ink-600 hover:bg-ink-100",
            ].join(" ")}
          >
            {p + 1}
          </button>
        </span>
      ))}

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="flex h-9 w-9 items-center justify-center rounded-full text-ink-600 hover:bg-ink-100 disabled:opacity-40"
        aria-label="Next page"
      >
        <ChevronRight className="h-4 w-4" />
      </button>
    </nav>
  );
}
