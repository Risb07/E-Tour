import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import ImageUrlField from "../../components/admin/ImageUrlField";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import TourPicker from "../../components/admin/TourPicker";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import { fetchAllTours, fetchTourMedia, addTourMedia, deleteTourMedia } from "../../services/tourService";

const MEDIA_TYPES = ["IMAGE", "VIDEO", "PDF", "DOCUMENT", "AUDIO", "MAP"];
const TAB_CONTEXTS = ["GALLERY", "VIDEO", "MAP", "BROCHURE"];

const EMPTY_FORM = {
  mediaType: "IMAGE",
  filePath: "",
  mimeType: "",
  tabContext: "GALLERY",
  displayOrder: "",
};

export default function AdminMediaPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: () => (selectedTourId ? fetchTourMedia(selectedTourId) : Promise.resolve([])),
      create: (payload) => addTourMedia(selectedTourId, payload),
      update: null,
      remove: (mediaId) => deleteTourMedia(selectedTourId, mediaId),
    }),
    [selectedTourId]
  );
  const { items: media, status, isMutating, createItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Media item",
  });

  useEffect(() => {
    fetchAllTours()
      .then((data) => {
        setTours(data);
        if (data.length > 0) setSelectedTourId(String(data[0].tourId));
      })
      .catch(() => showToast("Couldn't load tours.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      mediaType: form.mediaType,
      filePath: form.filePath,
      mimeType: form.mimeType || null,
      tabContext: form.tabContext,
      displayOrder: form.displayOrder ? Number(form.displayOrder) : 0,
    };
    const ok = await createItem(payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(item) {
    if (!window.confirm(`Delete this ${item.mediaType.toLowerCase()} item?`)) return;
    await removeItem(item.mediaId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Media</h1>
          <p className="mt-1 text-sm text-ink-500">Manage gallery images, videos, maps and brochures per tour.</p>
        </div>
        <Button icon={Plus} onClick={openCreate} disabled={!selectedTourId}>
          Add Media
        </Button>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "mediaId", label: "ID", className: "w-16" },
            {
              key: "mediaType",
              label: "Type",
              render: (m) => (
                <Badge variant={m.mediaType === "IMAGE" ? "success" : "info"}>{m.mediaType}</Badge>
              ),
            },
            {
              key: "filePath",
              label: "File",
              render: (m) => (
                <span className="block max-w-[18rem] truncate text-xs text-ink-500" title={m.filePath}>
                  {m.filePath}
                </span>
              ),
            },
            {
              key: "tabContext",
              label: "Tab",
              render: (m) => <Badge variant="accent">{m.tabContext}</Badge>,
            },
            { key: "displayOrder", label: "Order" },
            { key: "mimeType", label: "MIME", render: (m) => m.mimeType || "—" },
          ]}
          rows={media}
          rowKey="mediaId"
          searchKeys={["filePath", "mediaType"]}
          searchPlaceholder="Search media..."
          isLoading={status === "loading"}
          emptyTitle={selectedTourId ? "No media for this tour" : "Select a tour to view media"}
          emptyDescription="Add gallery images, videos, maps or brochures."
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={editing === "new"} title="Add media item" onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Media type"
            type="select"
            required
            options={MEDIA_TYPES}
            value={form.mediaType}
            // Point the tab at the matching type automatically. MAP and VIDEO
            // items left on the default GALLERY tab still work (the tour page
            // filters those tabs on mediaType), but the listing then reads as
            // if they belong somewhere they don't.
            onChange={(v) =>
              setForm((f) => ({
                ...f,
                mediaType: v,
                tabContext: TAB_CONTEXTS.includes(v) ? v : f.tabContext,
              }))
            }
          />
          <Field
            label="Tab"
            type="select"
            options={TAB_CONTEXTS}
            value={form.tabContext}
            onChange={(v) => setForm((f) => ({ ...f, tabContext: v }))}
          />
          <div className="sm:col-span-2">
            {form.mediaType === "IMAGE" ? (
              <ImageUrlField
                label="File URL"
                required
                value={form.filePath}
                onChange={(v) => setForm((f) => ({ ...f, filePath: v }))}
              />
            ) : (
              <Field
                label="File URL"
                required
                value={form.filePath}
                onChange={(v) => setForm((f) => ({ ...f, filePath: v }))}
                placeholder="https://... or /uploads/..."
                hint={
                  form.mediaType === "MAP"
                    ? "A Google Maps embed URL is shown inline; a route image is rendered; any other map link becomes an 'Open route map' button."
                    : `Link to the ${form.mediaType.toLowerCase()} file.`
                }
              />
            )}
          </div>
          <Field
            label="MIME type"
            value={form.mimeType}
            onChange={(v) => setForm((f) => ({ ...f, mimeType: v }))}
            placeholder="e.g. image/jpeg"
          />
          <Field
            label="Display order"
            type="number"
            min={0}
            value={form.displayOrder}
            onChange={(v) => setForm((f) => ({ ...f, displayOrder: v }))}
          />
          <div className="mt-2 flex justify-end gap-2 sm:col-span-2">
            <Button variant="ghost" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isMutating}>
              Save
            </Button>
          </div>
        </form>
      </AdminModal>
    </div>
  );
}
