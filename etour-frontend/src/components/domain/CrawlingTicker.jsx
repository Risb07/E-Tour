import { useEffect, useState } from "react";
import { fetchCrawlingTexts } from "../../services/crawlingTextService";

/** BRD 3.2 - Home page crawling (scrolling) ticker, sourced entirely from the database. */
export default function CrawlingTicker() {
  const [texts, setTexts] = useState([]);

  useEffect(() => {
    fetchCrawlingTexts()
      .then(setTexts)
      .catch(() => setTexts([]));
  }, []);

  if (texts.length === 0) return null;

  const combined = texts.map((t) => t.text).join("     •     ");

  return (
    <div className="overflow-hidden border-b border-ink-100 bg-ink-900 py-2 text-white">
      <style>{`
        @keyframes etour-crawl {
          from { transform: translateX(-30%); }
          to { transform: translateX(-100%); }
        }
      `}</style>
      <p
        className="whitespace-nowrap text-xs font-medium tracking-wide"
        style={{ animation: "etour-crawl 80s linear infinite", display: "inline-block", paddingLeft: "100%" }}
      >
        {combined}
      </p>
    </div>
  );
}
