import { useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllNavMenuItems,
  createNavMenuItem,
  updateNavMenuItem,
  deleteNavMenuItem,
} from "../../services/navMenuService";

const EMPTY_FORM = {
  label: "",
  link: "",
  sortOrder: "",
  active: true,
  parentItemId: "",
};

export default function AdminNavMenuPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllNavMenuItems,
      create: createNavMenuItem,
      update: updateNavMenuItem,
      remove: deleteNavMenuItem,
    }),
    []
  );
  const { items: items, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Menu item",
  });

  const parentOptions = items
    .filter((i) => i.navMenuItemId !== editing || editing === "new")
    .map((i) => ({ value: String(i.navMenuItemId), label: i.label }));

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(item) {
    setForm({
      label: item.label || "",
      link: item.link || "",
      sortOrder: item.sortOrder ?? "",
      active: Boolean(item.active),
      parentItemId: item.parentItem?.navMenuItemId ? String(item.parentItem.navMenuItemId) : "",
    });
    setEditing(item.navMenuItemId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      label: form.label,
      link: form.link,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
      active: form.active,
      parentItem: form.parentItemId ? { navMenuItemId: Number(form.parentItemId) } : null,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(item) {
    if (!window.confirm(`Delete menu item "${item.label}"?`)) return;
    await removeItem(item.navMenuItemId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Navigation Menu</h1>
          <p className="mt-1 text-sm text-ink-500">Database-driven site menu with parent/child items.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Menu Item
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "navMenuItemId", label: "ID", className: "w-16" },
            { key: "label", label: "Label" },
            { key: "link", label: "Link", render: (i) => <span className="text-xs text-ink-500">{i.link}</span> },
            { key: "parentItem", label: "Parent", render: (i) => i.parentItem?.label || "—" },
            { key: "sortOrder", label: "Order" },
            {
              key: "active",
              label: "Status",
              render: (i) =>
                i.active ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={items}
          rowKey="navMenuItemId"
          searchKeys={["label", "link"]}
          searchPlaceholder="Search menu items..."
          isLoading={status === "loading"}
          emptyTitle="No menu items yet"
          emptyDescription="Add your first menu item to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add menu item" : "Edit menu item"} onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Label"
            required
            value={form.label}
            onChange={(v) => setForm((f) => ({ ...f, label: v }))}
          />
          <Field
            label="Link"
            required
            value={form.link}
            onChange={(v) => setForm((f) => ({ ...f, link: v }))}
            placeholder="/home or https://..."
          />
          <Field
            label="Parent item"
            type="select"
            options={parentOptions}
            value={form.parentItemId}
            onChange={(v) => setForm((f) => ({ ...f, parentItemId: v }))}
            placeholder="None (top level)"
          />
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
            value={form.active}
            onChange={(v) => setForm((f) => ({ ...f, active: v }))}
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
