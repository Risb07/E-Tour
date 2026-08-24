import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import ImageUrlField from "../../components/admin/ImageUrlField";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllSectors,
  fetchAllSubSectors,
  createSubSector,
  updateSubSector,
  deleteSubSector,
} from "../../services/sectorService";
import { resolveMediaUrl } from "../../utils/media";

const EMPTY_FORM = {
  sectorId: "",
  name: "",
  description: "",
  iconUrl: "",
  imageUrl: "",
  active: true,
  sortOrder: "",
};

export default function AdminSubSectorsPage() {
  const [sectors, setSectors] = useState([]);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllSubSectors,
      create: createSubSector,
      update: updateSubSector,
      remove: deleteSubSector,
    }),
    []
  );
  const { items: subSectors, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Sub-sector",
  });

  useEffect(() => {
    fetchAllSectors()
      .then(setSectors)
      .catch(() => showToast("Couldn't load sectors.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const sectorOptions = sectors.map((s) => ({ value: String(s.sectorId), label: s.name }));

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(subSector) {
    setForm({
      sectorId: subSector.sector?.sectorId ? String(subSector.sector.sectorId) : "",
      name: subSector.name || "",
      description: subSector.description || "",
      iconUrl: subSector.iconUrl || "",
      imageUrl: subSector.imageUrl || "",
      active: Boolean(subSector.active),
      sortOrder: subSector.sortOrder ?? "",
    });
    setEditing(subSector.subSectorId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      sector: form.sectorId ? { sectorId: Number(form.sectorId) } : null,
      name: form.name,
      description: form.description,
      iconUrl: form.iconUrl,
      imageUrl: form.imageUrl,
      active: form.active,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(subSector) {
    if (!window.confirm(`Delete sub-sector "${subSector.name}"?`)) return;
    await removeItem(subSector.subSectorId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Sub-Sectors</h1>
          <p className="mt-1 text-sm text-ink-500">Sub-categories within each sector.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Sub-Sector
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "subSectorId", label: "ID", className: "w-16" },
            { key: "name", label: "Name" },
            {
              key: "sector",
              label: "Sector",
              render: (s) => s.sector?.name || "—",
            },
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
            {
              key: "active",
              label: "Status",
              render: (s) =>
                s.active ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={subSectors}
          rowKey="subSectorId"
          searchKeys={["name"]}
          searchPlaceholder="Search sub-sectors..."
          isLoading={status === "loading"}
          emptyTitle="No sub-sectors yet"
          emptyDescription="Add your first sub-sector to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add sub-sector" : "Edit sub-sector"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Sector"
            type="select"
            required
            options={sectorOptions}
            value={form.sectorId}
            onChange={(v) => setForm((f) => ({ ...f, sectorId: v }))}
          />
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
          <Field
            label="Active"
            type="checkbox"
            value={form.active}
            onChange={(v) => setForm((f) => ({ ...f, active: v }))}
          />
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Icon URL"
              value={form.iconUrl}
              onChange={(v) => setForm((f) => ({ ...f, iconUrl: v }))}
            />
          </div>
          <div className="sm:col-span-2">
            <ImageUrlField
              label="Image URL"
              value={form.imageUrl}
              onChange={(v) => setForm((f) => ({ ...f, imageUrl: v }))}
            />
          </div>
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
