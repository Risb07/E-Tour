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
  fetchAllSubSectors,
  fetchAllProducts,
  createProduct,
  updateProduct,
  deleteProduct,
} from "../../services/sectorService";
import { fetchAllTours } from "../../services/tourService";
import { formatCurrency } from "../../utils/format";

const EMPTY_FORM = {
  subSectorId: "",
  name: "",
  description: "",
  imageUrl: "",
  baseCost: "",
  durationDays: "",
  durationNights: "",
  tourCode: "",
  startDate: "",
  endDate: "",
  active: true,
  sortOrder: "",
  tourId: "",
};

export default function AdminProductsPage() {
  const [subSectors, setSubSectors] = useState([]);
  const [tours, setTours] = useState([]);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllProducts,
      create: createProduct,
      update: updateProduct,
      remove: deleteProduct,
    }),
    []
  );
  const { items: products, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Product",
  });

  useEffect(() => {
    Promise.all([fetchAllSubSectors(), fetchAllTours()])
      .then(([subs, tourList]) => {
        setSubSectors(subs);
        setTours(tourList);
      })
      .catch(() => showToast("Couldn't load sub-sectors or tours.", "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const subSectorOptions = subSectors.map((s) => ({ value: String(s.subSectorId), label: s.name }));
  const tourOptions = tours.map((t) => ({ value: String(t.tourId), label: `${t.title} (#${t.tourId})` }));

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(product) {
    setForm({
      subSectorId: product.subSector?.subSectorId ? String(product.subSector.subSectorId) : "",
      name: product.name || "",
      description: product.description || "",
      imageUrl: product.imageUrl || "",
      baseCost: product.baseCost ?? "",
      durationDays: product.durationDays ?? "",
      durationNights: product.durationNights ?? "",
      tourCode: product.tourCode || "",
      startDate: product.startDate || "",
      endDate: product.endDate || "",
      active: Boolean(product.active),
      sortOrder: product.sortOrder ?? "",
      tourId: product.tour?.tourId ? String(product.tour.tourId) : "",
    });
    setEditing(product.productId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      subSector: form.subSectorId ? { subSectorId: Number(form.subSectorId) } : null,
      name: form.name,
      description: form.description,
      imageUrl: form.imageUrl,
      baseCost: Number(form.baseCost),
      durationDays: Number(form.durationDays),
      durationNights: Number(form.durationNights),
      tourCode: form.tourCode,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      active: form.active,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : null,
      tour: form.tourId ? { tourId: Number(form.tourId) } : null,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(product) {
    if (!window.confirm(`Delete product "${product.name}"?`)) return;
    await removeItem(product.productId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Products</h1>
          <p className="mt-1 text-sm text-ink-500">Marketing catalog products under each sub-sector, optionally linked to a bookable tour.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Product
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "productId", label: "ID", className: "w-16" },
            { key: "name", label: "Name" },
            {
              key: "subSector",
              label: "Sub-Sector",
              render: (p) => p.subSector?.name || "—",
            },
            { key: "baseCost", label: "Cost", render: (p) => formatCurrency(p.baseCost) },
            { key: "durationDays", label: "Days" },
            { key: "durationNights", label: "Nights" },
            {
              key: "tour",
              label: "Linked Tour",
              render: (p) =>
                p.tour ? <Badge variant="success">#{p.tour.tourId}</Badge> : <Badge variant="info">Not linked</Badge>,
            },
            {
              key: "active",
              label: "Status",
              render: (p) =>
                p.active ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={products}
          rowKey="productId"
          searchKeys={["name", "tourCode"]}
          searchPlaceholder="Search products..."
          isLoading={status === "loading"}
          emptyTitle="No products yet"
          emptyDescription="Add your first product to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add product" : "Edit product"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Sub-sector"
            type="select"
            required
            options={subSectorOptions}
            value={form.subSectorId}
            onChange={(v) => setForm((f) => ({ ...f, subSectorId: v }))}
          />
          <Field
            label="Name"
            required
            value={form.name}
            onChange={(v) => setForm((f) => ({ ...f, name: v }))}
          />
          <Field
            label="Base cost (₹)"
            type="number"
            required
            min={0}
            step="0.01"
            value={form.baseCost}
            onChange={(v) => setForm((f) => ({ ...f, baseCost: v }))}
          />
          <Field
            label="Tour code"
            value={form.tourCode}
            onChange={(v) => setForm((f) => ({ ...f, tourCode: v }))}
            placeholder="e.g. ADV001"
          />
          <Field
            label="Duration (days)"
            type="number"
            required
            min={1}
            value={form.durationDays}
            onChange={(v) => setForm((f) => ({ ...f, durationDays: v }))}
          />
          <Field
            label="Duration (nights)"
            type="number"
            required
            min={0}
            value={form.durationNights}
            onChange={(v) => setForm((f) => ({ ...f, durationNights: v }))}
          />
          <Field
            label="Start date"
            type="date"
            value={form.startDate}
            onChange={(v) => setForm((f) => ({ ...f, startDate: v }))}
          />
          <Field
            label="End date"
            type="date"
            value={form.endDate}
            onChange={(v) => setForm((f) => ({ ...f, endDate: v }))}
          />
          <Field
            label="Linked bookable tour"
            type="select"
            options={tourOptions}
            value={form.tourId}
            onChange={(v) => setForm((f) => ({ ...f, tourId: v }))}
            placeholder="None (shows as coming soon)"
            hint="Link this product to a bookable tour to enable the Book button."
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
