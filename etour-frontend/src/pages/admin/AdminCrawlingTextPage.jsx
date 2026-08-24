import { useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllCrawlingTexts,
  createCrawlingText,
  updateCrawlingText,
  deleteCrawlingText,
} from "../../services/crawlingTextService";

const EMPTY_FORM = { text: "", sortOrder: "", isActive: true };

export default function AdminCrawlingTextPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllCrawlingTexts,
      create: createCrawlingText,
      update: updateCrawlingText,
      remove: deleteCrawlingText,
    }),
    []
  );
  const { items: texts, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Crawling text",
  });

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(item) {
    setForm({
      text: item.text || "",
      sortOrder: item.sortOrder ?? "",
      isActive: Boolean(item.isActive),
    });
    setEditing(item.crawlingTextId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      text: form.text,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
      isActive: form.isActive,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(item) {
    if (!window.confirm(`Delete this crawling text?`)) return;
    await removeItem(item.crawlingTextId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Crawling Text</h1>
          <p className="mt-1 text-sm text-ink-500">Scrolling ticker messages on the home page.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Text
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "crawlingTextId", label: "ID", className: "w-16" },
            {
              key: "text",
              label: "Text",
              render: (t) => <span className="block max-w-[26rem] truncate" title={t.text}>{t.text}</span>,
            },
            { key: "sortOrder", label: "Order" },
            {
              key: "isActive",
              label: "Status",
              render: (t) =>
                t.isActive ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={texts}
          rowKey="crawlingTextId"
          searchKeys={["text"]}
          searchPlaceholder="Search texts..."
          isLoading={status === "loading"}
          emptyTitle="No crawling texts yet"
          emptyDescription="Add your first ticker message to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add text" : "Edit text"} onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <Field
              label="Text"
              type="textarea"
              rows={3}
              required
              value={form.text}
              onChange={(v) => setForm((f) => ({ ...f, text: v }))}
            />
          </div>
          <Field
            label="Sort order"
            type="number"
            min={0}
            value={form.sortOrder}
            onChange={(v) => setForm((f) => ({ ...f, sortOrder: v }))}
          />
          <Field
            label="Active"
            type="checkbox"
            value={form.isActive}
            onChange={(v) => setForm((f) => ({ ...f, isActive: v }))}
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
