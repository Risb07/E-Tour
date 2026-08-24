import { useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import ImageUrlField from "../../components/admin/ImageUrlField";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllAdBanners,
  createAdBanner,
  updateAdBanner,
  deleteAdBanner,
} from "../../services/adBannerService";
import { resolveMediaUrl } from "../../utils/media";

const EMPTY_FORM = {
  title: "",
  imageUrl: "",
  linkUrl: "",
  position: "",
  status: true,
};

export default function AdminAdBannersPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllAdBanners,
      create: createAdBanner,
      update: updateAdBanner,
      remove: deleteAdBanner,
    }),
    []
  );
  const { items: banners, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Ad banner",
  });

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(banner) {
    setForm({
      title: banner.title || "",
      imageUrl: banner.imageUrl || "",
      linkUrl: banner.linkUrl || "",
      position: banner.position || "",
      status: Boolean(banner.status),
    });
    setEditing(banner.adId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      title: form.title,
      imageUrl: form.imageUrl,
      linkUrl: form.linkUrl,
      position: form.position,
      status: form.status,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(banner) {
    if (!window.confirm(`Delete banner "${banner.title}"?`)) return;
    await removeItem(banner.adId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Ad Banners</h1>
          <p className="mt-1 text-sm text-ink-500">Showcase page advertisement banners.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Banner
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "adId", label: "ID", className: "w-16" },
            { key: "title", label: "Title" },
            {
              key: "imageUrl",
              label: "Image",
              render: (b) =>
                b.imageUrl ? (
                  <img src={resolveMediaUrl(b.imageUrl)} alt={b.title} className="h-10 w-24 rounded-lg border border-ink-100 object-cover" />
                ) : (
                  "—"
                ),
            },
            {
              key: "position",
              label: "Position",
              render: (b) => <Badge variant="accent">{b.position}</Badge>,
            },
            {
              key: "linkUrl",
              label: "Link",
              render: (b) => (b.linkUrl ? <span className="text-xs text-ink-500">{b.linkUrl}</span> : "—"),
            },
            {
              key: "status",
              label: "Status",
              render: (b) =>
                b.status ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={banners}
          rowKey="adId"
          searchKeys={["title", "position"]}
          searchPlaceholder="Search banners..."
          isLoading={status === "loading"}
          emptyTitle="No banners yet"
          emptyDescription="Add your first ad banner to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add banner" : "Edit banner"} onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Title"
            required
            value={form.title}
            onChange={(v) => setForm((f) => ({ ...f, title: v }))}
          />
          <Field
            label="Position"
            required
            value={form.position}
            onChange={(v) => setForm((f) => ({ ...f, position: v }))}
            placeholder="e.g. LEFT, RIGHT, TOP"
            hint="Free-text slot name used by the showcase page."
          />
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Image URL"
              value={form.imageUrl}
              onChange={(v) => setForm((f) => ({ ...f, imageUrl: v }))}
            />
          </div>
          <Field
            label="Link URL"
            value={form.linkUrl}
            onChange={(v) => setForm((f) => ({ ...f, linkUrl: v }))}
            placeholder="https://..."
          />
          <Field
            label="Active"
            type="checkbox"
            value={form.status}
            onChange={(v) => setForm((f) => ({ ...f, status: v }))}
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
