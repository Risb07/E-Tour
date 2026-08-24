import { useEffect, useMemo, useState } from "react";
import { Search, Pencil, Trash2, ArrowUp, ArrowDown, ArrowUpDown } from "lucide-react";
import Pagination from "../ui/Pagination";
import EmptyState from "../common/EmptyState";

/**
 * Generic admin data table: client-side search, column sorting and
 * pagination over a fully-fetched list (the catalog APIs return complete
 * lists, so filtering happens in the browser).
 *
 * columns: [{ key, label, sortable (default true), render(row), className }]
 */
export default function AdminTable({
  columns,
  rows = [],
  rowKey = "id",
  searchKeys = [],
  searchPlaceholder = "Search...",
  onEdit,
  onDelete,
  emptyTitle = "Nothing here yet",
  emptyDescription = "Add your first record to get started.",
  isLoading = false,
  pageSize = 10,
}) {
  const [search, setSearch] = useState("");
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(0);

  useEffect(() => {
    setPage(0);
  }, [search, sortKey, sortDir, rows.length]);

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return rows;
    return rows.filter((row) =>
      searchKeys.some((key) => {
        const value = row?.[key];
        return value !== undefined && value !== null && String(value).toLowerCase().includes(term);
      })
    );
  }, [rows, search, searchKeys]);

  const sorted = useMemo(() => {
    if (!sortKey) return filtered;
    const dir = sortDir === "asc" ? 1 : -1;
    return [...filtered].sort((a, b) => {
      const va = a?.[sortKey];
      const vb = b?.[sortKey];
      if (va === null || va === undefined) return 1;
      if (vb === null || vb === undefined) return -1;
      if (typeof va === "number" && typeof vb === "number") return (va - vb) * dir;
      return String(va).localeCompare(String(vb), undefined, { numeric: true }) * dir;
    });
  }, [filtered, sortKey, sortDir]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize));
  const currentPage = Math.min(page, totalPages - 1);
  const paged = sorted.slice(currentPage * pageSize, currentPage * pageSize + pageSize);

  function toggleSort(key) {
    if (sortKey === key) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortDir("asc");
    }
  }

  return (
    <div className="rounded-card bg-white shadow-soft">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-ink-100 p-4">
        <div className="relative w-full max-w-xs">
          <Search
            className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-300"
            aria-hidden="true"
          />
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={searchPlaceholder}
            aria-label={searchPlaceholder}
            className="w-full rounded-xl border border-ink-200 bg-white py-2.5 pl-10 pr-3.5 text-sm text-ink-900 placeholder:text-ink-300 transition-colors focus:border-amber-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400"
          />
        </div>
        {search && (
          <p className="text-sm text-ink-500">
            {filtered.length} result{filtered.length === 1 ? "" : "s"}
          </p>
        )}
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-ink-100 text-xs font-semibold uppercase tracking-wide text-ink-500">
              {columns.map((col) => {
                const sortable = col.sortable !== false;
                const active = sortKey === col.key;
                return (
                  <th key={col.key} className="whitespace-nowrap px-4 py-3">
                    {sortable ? (
                      <button
                        type="button"
                        onClick={() => toggleSort(col.key)}
                        className="inline-flex items-center gap-1.5 hover:text-ink-800"
                      >
                        {col.label}
                        {active ? (
                          sortDir === "asc" ? (
                            <ArrowUp className="h-3.5 w-3.5 text-amber-600" aria-hidden="true" />
                          ) : (
                            <ArrowDown className="h-3.5 w-3.5 text-amber-600" aria-hidden="true" />
                          )
                        ) : (
                          <ArrowUpDown className="h-3.5 w-3.5 text-ink-300" aria-hidden="true" />
                        )}
                      </button>
                    ) : (
                      col.label
                    )}
                  </th>
                );
              })}
              {(onEdit || onDelete) && <th className="px-4 py-3 text-right">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              Array.from({ length: 4 }).map((_, i) => (
                <tr key={i} className="border-b border-ink-50">
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-4">
                      <div className="h-3.5 w-24 animate-pulse rounded bg-ink-100" />
                    </td>
                  ))}
                  {(onEdit || onDelete) && (
                    <td className="px-4 py-4">
                      <div className="ml-auto h-3.5 w-14 animate-pulse rounded bg-ink-100" />
                    </td>
                  )}
                </tr>
              ))
            ) : paged.length === 0 ? (
              <tr>
                <td colSpan={columns.length + ((onEdit || onDelete) ? 1 : 0)}>
                  <EmptyState
                    title={search ? "No matches" : emptyTitle}
                    description={search ? "Try a different search term." : emptyDescription}
                  />
                </td>
              </tr>
            ) : (
              paged.map((row) => (
                <tr key={row[rowKey]} className="border-b border-ink-50 last:border-0 hover:bg-ink-50/50">
                  {columns.map((col) => (
                    <td
                      key={col.key}
                      className={`max-w-xs truncate px-4 py-3.5 text-ink-800 ${col.className || ""}`}
                    >
                      {col.render ? col.render(row) : row?.[col.key] ?? "—"}
                    </td>
                  ))}
                  {(onEdit || onDelete) && (
                    <td className="px-4 py-3.5">
                      <div className="flex justify-end gap-1">
                        {onEdit && (
                          <button
                            type="button"
                            onClick={() => onEdit(row)}
                            aria-label="Edit"
                            className="rounded-lg p-2 text-ink-400 hover:bg-ink-50 hover:text-ink-800"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                        )}
                        {onDelete && (
                          <button
                            type="button"
                            onClick={() => onDelete(row)}
                            aria-label="Delete"
                            className="rounded-lg p-2 text-ink-400 hover:bg-red-50 hover:text-red-600"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {!isLoading && sorted.length > pageSize && (
        <div className="border-t border-ink-100 p-4">
          <Pagination page={currentPage} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
