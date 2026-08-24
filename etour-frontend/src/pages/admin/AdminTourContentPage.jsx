import { useEffect, useState } from "react";
import { Trash2, Save } from "lucide-react";
import { fetchAllTours } from "../../services/tourService";
import {
  fetchTourContent,
  saveTourContent,
  deleteTourContent,
} from "../../services/tourContentService";
import { TOUR_CONTENT_LABELS } from "../../constants/enums";
import { useToast } from "../../hooks/useToast";
import Button from "../../components/common/Button";
import Loader from "../../components/common/Loader";
import Badge from "../../components/ui/Badge";
import TourPicker from "../../components/admin/TourPicker";

// The four tabs the tour page groups under "Good to know". Order matches the
// order they are rendered in there.
const CONTENT_TYPES = ["PASSPORT_VISA", "WEATHER", "DOS_DONTS", "TERMS_CONDITIONS"];

const PLACEHOLDERS = {
  PASSPORT_VISA: "Visa requirements, passport validity, documents to carry...",
  WEATHER: "What the weather is like at this time of year, what to pack...",
  DOS_DONTS: "Local customs, dress codes, things to avoid...",
  TERMS_CONDITIONS: "Cancellation policy, payment terms, liability...",
};

/**
 * BRD 3.5 "Good to know" tab. Each tour has at most one entry per content
 * type, so this is a fixed set of four editors rather than a create/delete
 * list - saving one is an upsert on the backend, and clearing one removes it
 * from the tour page.
 */
export default function AdminTourContentPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [entries, setEntries] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [status, setStatus] = useState("loading");
  const [savingType, setSavingType] = useState(null);
  const { showToast } = useToast();

  useEffect(() => {
    fetchAllTours()
      .then((data) => {
        setTours(data);
        if (data.length > 0) setSelectedTourId(String(data[0].tourId));
      })
      .catch(() => showToast("Couldn't load tours.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function loadContent(tourId) {
    if (!tourId) return;
    setStatus("loading");
    fetchTourContent(tourId)
      .then((data) => {
        setEntries(data);
        // Seed each editor from what is already saved so the admin edits the
        // live text rather than typing over a blank box.
        const next = {};
        CONTENT_TYPES.forEach((type) => {
          next[type] = data.find((e) => e.contentType === type)?.contentText || "";
        });
        setDrafts(next);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(() => {
    if (selectedTourId) loadContent(selectedTourId);
  }, [selectedTourId]);

  function entryFor(type) {
    return entries.find((e) => e.contentType === type) || null;
  }

  function isDirty(type) {
    return (drafts[type] || "") !== (entryFor(type)?.contentText || "");
  }

  async function handleSave(type) {
    const text = (drafts[type] || "").trim();
    if (!text) {
      showToast("Add some text before saving, or use Remove to clear this tab.", "error");
      return;
    }
    setSavingType(type);
    try {
      await saveTourContent(selectedTourId, { contentType: type, contentText: text, languageCode: "en" });
      showToast(`${TOUR_CONTENT_LABELS[type]} saved.`, "success");
      loadContent(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't save this section.", "error");
    } finally {
      setSavingType(null);
    }
  }

  async function handleRemove(type) {
    const entry = entryFor(type);
    if (!entry) return;
    if (!window.confirm(`Remove "${TOUR_CONTENT_LABELS[type]}" from this tour?`)) return;
    try {
      await deleteTourContent(selectedTourId, entry.tourContentId);
      showToast(`${TOUR_CONTENT_LABELS[type]} removed.`, "success");
      loadContent(selectedTourId);
    } catch (err) {
      showToast(err.message || "Couldn't remove this section.", "error");
    }
  }

  return (
    <div>
      <div>
        <h1 className="font-display text-2xl font-bold text-ink-900">Good to Know</h1>
        <p className="mt-1 text-sm text-ink-500">
          Passport &amp; visa, weather, do&apos;s &amp; don&apos;ts and terms shown under &quot;Good to know&quot; on the
          tour page. Leave a section empty to hide it.
        </p>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      {status === "loading" && <Loader label="Loading content..." />}

      {status === "succeeded" && (
        <div className="mt-6 flex flex-col gap-5">
          {CONTENT_TYPES.map((type) => {
            const entry = entryFor(type);
            return (
              <div key={type} className="rounded-card bg-white p-5 shadow-soft">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <h2 className="font-display text-base font-bold text-ink-900">
                    {TOUR_CONTENT_LABELS[type]}
                  </h2>
                  {entry ? (
                    <Badge variant="success">Live on tour page</Badge>
                  ) : (
                    <Badge variant="info">Not shown</Badge>
                  )}
                </div>

                <textarea
                  rows={4}
                  value={drafts[type] || ""}
                  onChange={(e) => setDrafts((d) => ({ ...d, [type]: e.target.value }))}
                  placeholder={PLACEHOLDERS[type]}
                  className="mt-3 w-full rounded-xl border border-ink-200 bg-white px-3.5 py-2.5 text-sm text-ink-900 placeholder:text-ink-300 focus:border-amber-400 focus:outline-none"
                />

                <div className="mt-3 flex items-center justify-end gap-2">
                  {entry && (
                    <Button variant="ghost" icon={Trash2} onClick={() => handleRemove(type)}>
                      Remove
                    </Button>
                  )}
                  <Button
                    icon={Save}
                    onClick={() => handleSave(type)}
                    isLoading={savingType === type}
                    disabled={!isDirty(type)}
                  >
                    {entry ? "Update" : "Save"}
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
