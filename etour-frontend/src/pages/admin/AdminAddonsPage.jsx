import { useEffect, useMemo, useState } from "react";
import { Plus } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import TourPicker from "../../components/admin/TourPicker";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllTours,
  fetchTourAddons,
  createTourAddon,
  updateTourAddon,
  deleteTourAddon,
} from "../../services/tourService";
import { PRICE_TYPE_LABELS } from "../../constants/enums";
import { formatCurrency } from "../../utils/format";

// Mirrors the backend PriceType enum. The labels are the same ones the
// booking step shows next to each add-on's price.
const PRICE_TYPE_OPTIONS = [
  { value: "PER_PERSON", label: "Per person" },
  { value: "PER_ROOM", label: "Per room" },
  { value: "PER_BOOKING", label: "Per booking" },
];

const EMPTY_FORM = {
  addonName: "",
  description: "",
  price: "",
  priceType: "PER_PERSON",
  isOptional: true,
  displayOrder: 0,
};

/**
 * Optional extras sold alongside a tour (airport transfer, travel insurance,
 * a guided add-on day, and so on).
 *
 * These are what the customer sees on the first booking step; whatever is
 * selected there is priced into the booking total, carried onto the payment
 * summary and itemised on the invoice. Fields map one-to-one onto
 * TourAddonDto, so this page needs no new backend endpoint.
 */
export default function AdminAddonsPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: () => (selectedTourId ? fetchTourAddons(selectedTourId) : Promise.resolve([])),
      create: (payload) => createTourAddon(selectedTourId, payload),
      update: (id, payload) => updateTourAddon(selectedTourId, id, payload),
      remove: (id) => deleteTourAddon(selectedTourId, id),
    }),
    [selectedTourId]
  );

  const { items: addons, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Add-on",
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
    // Put a new add-on at the end of the current list by default.
    setForm({ ...EMPTY_FORM, displayOrder: addons.length });
    setEditing("new");
  }

  function openEdit(addon) {
    setForm({
      addonName: addon.addonName || "",
      description: addon.description || "",
      price: addon.price ?? "",
      priceType: addon.priceType || "PER_PERSON",
      isOptional: addon.isOptional !== false,
      displayOrder: addon.displayOrder ?? 0,
    });
    setEditing(addon.addonId);
  }

  async function handleSubmit(e) {
    e.preventDefault();

    const payload = {
      addonName: form.addonName.trim(),
      description: form.description?.trim() || null,
      price: Number(form.price || 0),
      priceType: form.priceType,
      isOptional: form.isOptional,
      displayOrder: Number(form.displayOrder || 0),
    };

    // The server rejects a non-positive price (@Positive on TourAddon), so
    // catch it here rather than spending a round trip on it.
    if (payload.price <= 0) {
      showToast("Price must be greater than zero.", "error");
      return;
    }

    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(addon) {
    if (!window.confirm(`Delete the "${addon.addonName}" add-on?`)) return;
    await removeItem(addon.addonId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Add-ons</h1>
          <p className="mt-1 text-sm text-ink-500">
            Optional extras offered on the first booking step. Anything the customer selects is added
            to the booking total and itemised on the invoice.
          </p>
        </div>
        <Button icon={Plus} onClick={openCreate} disabled={!selectedTourId}>
          Add add-on
        </Button>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      <div className="mt-6">
        <AdminTable
          isLoading={status === "loading"}
          rows={addons}
          rowKey="addonId"
          searchKeys={["addonName", "description"]}
          searchPlaceholder="Search add-ons..."
          emptyTitle="No add-ons yet"
          emptyDescription="Tours with no add-ons simply skip the extras section during booking."
          columns={[
            {
              key: "addonName",
              label: "Add-on",
              render: (a) => (
                <div>
                  <span className="font-medium text-ink-900">{a.addonName}</span>
                  {a.description && <span className="block text-xs text-ink-400">{a.description}</span>}
                </div>
              ),
            },
            {
              key: "price",
              label: "Price",
              render: (a) => (
                <div>
                  <span className="font-semibold text-ink-900">{formatCurrency(a.price)}</span>
                  <span className="block text-xs text-ink-400">
                    {PRICE_TYPE_LABELS[a.priceType] || a.priceType}
                  </span>
                </div>
              ),
            },
            {
              key: "isOptional",
              label: "Type",
              render: (a) => (
                <Badge variant={a.isOptional === false ? "warning" : "info"}>
                  {a.isOptional === false ? "Included" : "Optional"}
                </Badge>
              ),
            },
            {
              key: "displayOrder",
              label: "Order",
              render: (a) => <span className="text-ink-500">{a.displayOrder ?? 0}</span>,
            },
          ]}
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal
        open={Boolean(editing)}
        title={editing === "new" ? "Add add-on" : "Edit add-on"}
        onClose={() => setEditing(null)}
      >
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Field
            label="Name"
            required
            value={form.addonName}
            onChange={(v) => setForm((f) => ({ ...f, addonName: v }))}
            placeholder="Airport transfer"
          />

          <Field
            label="Description"
            type="textarea"
            rows={3}
            value={form.description}
            onChange={(v) => setForm((f) => ({ ...f, description: v }))}
            placeholder="Shown under the add-on name on the booking page."
          />

          <Field
            label="Price (₹)"
            type="number"
            required
            min={1}
            step="0.01"
            value={form.price}
            onChange={(v) => setForm((f) => ({ ...f, price: v }))}
          />

          <Field
            label="Price type"
            type="select"
            required
            value={form.priceType}
            onChange={(v) => setForm((f) => ({ ...f, priceType: v }))}
            options={PRICE_TYPE_OPTIONS}
            hint="Shown next to the price. The customer chooses the quantity at booking time, and the total is price x quantity."
          />

          <Field
            label="Display order"
            type="number"
            min={0}
            value={form.displayOrder}
            onChange={(v) => setForm((f) => ({ ...f, displayOrder: v }))}
            hint="Lower numbers appear first."
          />

          {/* Field's checkbox branch renders no hint, so the explanation
              lives in the label itself. */}
          <Field
            label="Optional extra (untick to mark it as part of the package)"
            type="checkbox"
            value={form.isOptional}
            onChange={(v) => setForm((f) => ({ ...f, isOptional: v }))}
          />

          <div className="mt-2 flex justify-end gap-2">
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
