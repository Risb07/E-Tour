import { createContext, useState, useEffect, useCallback, useMemo } from "react";
import { fetchContentEntries } from "../services/contentService";

export const ContentContext = createContext(null);

/**
 * BRD 2.1 - every label, message and image on the site comes from the
 * `content` database table. This loads the whole active set once on app start
 * (it is small and changes rarely) and exposes lookup helpers, so no component
 * needs its own request or its own hard-coded copy.
 *
 * Design rule: when a key is missing from the database the helpers return
 * null/[] rather than invented placeholder text, so a component can hide the
 * section entirely. Showing made-up copy would hide the fact that a row is
 * missing.
 */
export function ContentProvider({ children, language = "en" }) {
  const [entries, setEntries] = useState([]);
  const [status, setStatus] = useState("loading");

  const load = useCallback(async () => {
    setStatus("loading");
    try {
      const data = await fetchContentEntries(language);
      setEntries(Array.isArray(data) ? data : []);
      setStatus("succeeded");
    } catch {
      // Content is presentational - a failure here must not blank the whole
      // app, so components fall back to hiding optional sections.
      setEntries([]);
      setStatus("failed");
    }
  }, [language]);

  useEffect(() => {
    load();
  }, [load]);

  /** Raw row for a key, or null. */
  const entry = useCallback(
    (key) => entries.find((e) => e.contentKey === key) || null,
    [entries]
  );

  /** Text value for a key, or null when the database has no such row. */
  const text = useCallback((key) => entry(key)?.contentValue ?? null, [entry]);

  /** Media URL for a key, or null. */
  const media = useCallback((key) => entry(key)?.mediaUrl ?? null, [entry]);

  /**
   * All rows for a page, ordered by displayOrder. Used for repeating blocks
   * (FAQs, "why choose us" cards, footer links) where the number of items is
   * itself data, not layout.
   */
  const list = useCallback(
    (pageName) =>
      entries
        .filter((e) => (e.pageName || "").toLowerCase() === pageName.toLowerCase())
        .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0)),
    [entries]
  );

  /**
   * Rows whose key starts with a prefix (e.g. "faq."), ordered. Lets one page
   * hold several independent repeating groups.
   */
  const group = useCallback(
    (keyPrefix) =>
      entries
        .filter((e) => e.contentKey?.startsWith(keyPrefix))
        .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0)),
    [entries]
  );

  const value = useMemo(
    () => ({ entries, status, language, entry, text, media, list, group, reload: load }),
    [entries, status, language, entry, text, media, list, group, load]
  );

  return <ContentContext.Provider value={value}>{children}</ContentContext.Provider>;
}
