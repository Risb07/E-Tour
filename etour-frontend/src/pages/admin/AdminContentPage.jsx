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
  fetchContentEntries,
  createContent,
  updateContent,
  deleteContent,
} from "../../services/contentService";

const EMPTY_FORM = {
  contentKey: "",
  pageName: "",
  languageCode: "en",
  contentValue: "",
  mediaUrl: "",
  linkUrl: "",
  displayOrder: "",
  status: true,
};

export default function AdminContentPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchContentEntries,
      create: createContent,
      update: updateContent,
      remove: deleteContent,
    }),
    []
  );
  const { items: entries, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Content entry",
  });

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(entry) {
    setForm({
      contentKey: entry.contentKey || "",
      pageName: entry.pageName || "",
      languageCode: entry.languageCode || "en",
      contentValue: entry.contentValue || "",
      mediaUrl: entry.mediaUrl || "",
      linkUrl: entry.linkUrl || "",
      displayOrder: entry.displayOrder ?? "",
      status: Boolean(entry.status),
    });
    setEditing(entry.contentId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      contentKey: form.contentKey,
      pageName: form.pageName,
      languageCode: form.languageCode,
      contentValue: form.contentValue,
      mediaUrl: form.mediaUrl,
      linkUrl: form.linkUrl,
      displayOrder: form.displayOrder ? Number(form.displayOrder) : null,
      status: form.status,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(entry) {
    if (!window.confirm(`Delete content "${entry.contentKey}" (${entry.languageCode})?`)) return;
    await removeItem(entry.contentId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Content</h1>
          <p className="mt-1 text-sm text-ink-500">Database-driven page text and labels with multilingual support.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Content
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "contentId", label: "ID", className: "w-16" },
            { key: "contentKey", label: "Key" },
            { key: "pageName", label: "Page", render: (c) => c.pageName || "—" },
            {
              key: "languageCode",
              label: "Lang",
              render: (c) => <Badge variant="accent">{c.languageCode}</Badge>,
            },
            {
              key: "contentValue",
              label: "Value",
              render: (c) => (
                <span className="block max-w-[22rem] truncate" title={c.contentValue}>
                  {c.contentValue}
                </span>
              ),
            },
            {
              key: "status",
              label: "Status",
              render: (c) =>
                c.status ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={entries}
          rowKey="contentId"
          searchKeys={["contentKey", "pageName", "languageCode", "contentValue"]}
          searchPlaceholder="Search content..."
          isLoading={status === "loading"}
          emptyTitle="No content entries yet"
          emptyDescription="Add your first content entry to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add content" : "Edit content"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Content key"
            required
            value={form.contentKey}
            onChange={(v) => setForm((f) => ({ ...f, contentKey: v }))}
            placeholder="e.g. home.heroTitle"
          />
          <Field
            label="Page name"
            value={form.pageName}
            onChange={(v) => setForm((f) => ({ ...f, pageName: v }))}
            placeholder="e.g. HOME"
          />
          <Field
            label="Language code"
            value={form.languageCode}
            onChange={(v) => setForm((f) => ({ ...f, languageCode: v }))}
            placeholder="en"
          />
          <Field
            label="Display order"
            type="number"
            min={0}
            value={form.displayOrder}
            onChange={(v) => setForm((f) => ({ ...f, displayOrder: v }))}
          />
          <div className="sm:col-span-2">
            <Field
              label="Content value"
              type="textarea"
              rows={4}
              required
              value={form.contentValue}
              onChange={(v) => setForm((f) => ({ ...f, contentValue: v }))}
            />
          </div>
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Media URL"
              value={form.mediaUrl}
              onChange={(v) => setForm((f) => ({ ...f, mediaUrl: v }))}
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
