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
  fetchAllSectors,
  createSector,
  updateSector,
  deleteSector,
} from "../../services/sectorService";
import { resolveMediaUrl } from "../../utils/media";

const EMPTY_FORM = {
  name: "",
  description: "",
  iconUrl: "",
  imageUrl: "",
  active: true,
  sortOrder: "",
  tourCount: "",
};

export default function AdminSectorsPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllSectors,
      create: createSector,
      update: updateSector,
      remove: deleteSector,
    }),
    []
  );
  const { items: sectors, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Sector",
  });

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(sector) {
    setForm({
      name: sector.name || "",
      description: sector.description || "",
      iconUrl: sector.iconUrl || "",
      imageUrl: sector.imageUrl || "",
      active: Boolean(sector.active),
      sortOrder: sector.sortOrder ?? "",
      tourCount: sector.tourCount ?? "",
    });
    setEditing(sector.sectorId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      name: form.name,
      description: form.description,
      iconUrl: form.iconUrl,
      imageUrl: form.imageUrl,
      active: form.active,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
      tourCount: form.tourCount ? Number(form.tourCount) : null,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(sector) {
    if (!window.confirm(`Delete sector "${sector.name}"?`)) return;
    await removeItem(sector.sectorId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Sectors</h1>
          <p className="mt-1 text-sm text-ink-500">Home page sector icons shown on the site.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Sector
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "sectorId", label: "ID", className: "w-16" },
            { key: "name", label: "Name" },
            {
              key: "iconUrl",
              label: "Icon",
              render: (s) =>
                s.iconUrl ? (
                  <img src={resolveMediaUrl(s.iconUrl)} alt={s.name} className="h-8 w-8 rounded-lg border border-ink-100 object-cover" />
                ) : (
                  "—"
                ),
            },
            { key: "sortOrder", label: "Order" },
            { key: "tourCount", label: "Tours" },
            {
              key: "active",
              label: "Status",
              render: (s) =>
                s.active ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={sectors}
          rowKey="sectorId"
          searchKeys={["name"]}
          searchPlaceholder="Search sectors..."
          isLoading={status === "loading"}
          emptyTitle="No sectors yet"
          emptyDescription="Add your first sector to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add sector" : "Edit sector"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Name"
            required
            value={form.name}
            onChange={(v) => setForm((f) => ({ ...f, name: v }))}
          />
          <Field
            label="Sort order"
            type="number"
            min={0}
            value={form.sortOrder}
            onChange={(v) => setForm((f) => ({ ...f, sortOrder: v }))}
          />
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Icon URL"
              value={form.iconUrl}
              onChange={(v) => setForm((f) => ({ ...f, iconUrl: v }))}
              hint="Small icon used on the home page sector row."
            />
          </div>
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Image URL"
              value={form.imageUrl}
              onChange={(v) => setForm((f) => ({ ...f, imageUrl: v }))}
              hint="Hero image for the sector page."
            />
          </div>
          <Field
            label="Tour count"
            type="number"
            min={0}
            value={form.tourCount}
            onChange={(v) => setForm((f) => ({ ...f, tourCount: v }))}
            hint="Displayed on the home page. Can be set manually."
          />
          <Field
            label="Active"
            type="checkbox"
            value={form.active}
            onChange={(v) => setForm((f) => ({ ...f, active: v }))}
          />
          <div className="sm:col-span-2">
            <Field
              label="Description"
              type="textarea"
              rows={3}
              value={form.description}
              onChange={(v) => setForm((f) => ({ ...f, description: v }))}
            />
          </div>
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
