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
import { fetchAllTours } from "../../services/tourService";
import { fetchTourCostsForTour, createTourCost, updateTourCost, deleteTourCost } from "../../services/tourCostService";
import { formatCurrency, formatDate } from "../../utils/format";

const EMPTY_FORM = {
  basePrice: "",
  singlePersonCost: "",
  extraPersonCost: "",
  childWithBedCost: "",
  childWithoutBedCost: "",
  validFrom: "",
  validTo: "",
  status: true,
};

export default function AdminTourCostsPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: () => (selectedTourId ? fetchTourCostsForTour(selectedTourId) : Promise.resolve([])),
      create: (payload) => createTourCost(selectedTourId, payload),
      update: (id, payload) => updateTourCost(id, selectedTourId, payload),
      remove: deleteTourCost,
    }),
    [selectedTourId]
  );
  const { items: costs, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Cost sheet",
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

  function openEdit(cost) {
    setForm({
      basePrice: cost.basePrice ?? "",
      singlePersonCost: cost.singlePersonCost ?? "",
      extraPersonCost: cost.extraPersonCost ?? "",
      childWithBedCost: cost.childWithBedCost ?? "",
      childWithoutBedCost: cost.childWithoutBedCost ?? "",
      validFrom: cost.validFrom || "",
      validTo: cost.validTo || "",
      status: cost.status !== 0,
    });
    setEditing(cost.costId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      basePrice: Number(form.basePrice),
      singlePersonCost: form.singlePersonCost ? Number(form.singlePersonCost) : null,
      extraPersonCost: form.extraPersonCost ? Number(form.extraPersonCost) : null,
      childWithBedCost: form.childWithBedCost ? Number(form.childWithBedCost) : null,
      childWithoutBedCost: form.childWithoutBedCost ? Number(form.childWithoutBedCost) : null,
      validFrom: form.validFrom,
      validTo: form.validTo,
      status: form.status ? 1 : 0,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(cost) {
    if (!window.confirm(`Delete cost sheet valid ${formatDate(cost.validFrom)} - ${formatDate(cost.validTo)}?`)) return;
    await removeItem(cost.costId);
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Tour Costs</h1>
          <p className="mt-1 text-sm text-ink-500">Manage pricing sheets (base/single/extra/child) and validity windows per tour.</p>
        </div>
        <Button icon={Plus} onClick={openCreate} disabled={!selectedTourId}>
          Add Cost Sheet
        </Button>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "costId", label: "ID", className: "w-16" },
            { key: "basePrice", label: "Base", render: (c) => formatCurrency(c.basePrice) },
            { key: "singlePersonCost", label: "Single", render: (c) => formatCurrency(c.singlePersonCost) },
            { key: "extraPersonCost", label: "Extra", render: (c) => formatCurrency(c.extraPersonCost) },
            { key: "childWithBedCost", label: "Child +Bed", render: (c) => formatCurrency(c.childWithBedCost) },
            { key: "childWithoutBedCost", label: "Child -Bed", render: (c) => formatCurrency(c.childWithoutBedCost) },
            {
              key: "validFrom",
              label: "Valid From",
              render: (c) => formatDate(c.validFrom),
            },
            {
              key: "validTo",
              label: "Valid To",
              render: (c) => formatDate(c.validTo),
            },
            {
              key: "status",
              label: "Status",
              render: (c) =>
                c.status === 1 ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={costs}
          rowKey="costId"
          searchKeys={["basePrice"]}
          searchPlaceholder="Search cost sheets..."
          isLoading={status === "loading"}
          emptyTitle={selectedTourId ? "No cost sheets for this tour" : "Select a tour to view cost sheets"}
          emptyDescription="Add a pricing sheet using the button above."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add cost sheet" : "Edit cost sheet"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Base price (₹)"
            type="number"
            required
            min={0}
            step="0.01"
            value={form.basePrice}
            onChange={(v) => setForm((f) => ({ ...f, basePrice: v }))}
          />
          <Field
            label="Single person cost (₹)"
            type="number"
            min={0}
            step="0.01"
            value={form.singlePersonCost}
            onChange={(v) => setForm((f) => ({ ...f, singlePersonCost: v }))}
          />
          <Field
            label="Extra person cost (₹)"
            type="number"
            min={0}
            step="0.01"
            value={form.extraPersonCost}
            onChange={(v) => setForm((f) => ({ ...f, extraPersonCost: v }))}
          />
          <Field
            label="Child with bed (₹)"
            type="number"
            min={0}
            step="0.01"
            value={form.childWithBedCost}
            onChange={(v) => setForm((f) => ({ ...f, childWithBedCost: v }))}
          />
          <Field
            label="Child without bed (₹)"
            type="number"
            min={0}
            step="0.01"
            value={form.childWithoutBedCost}
            onChange={(v) => setForm((f) => ({ ...f, childWithoutBedCost: v }))}
          />
          <Field
            label="Valid from"
            type="date"
            required
            value={form.validFrom}
            onChange={(v) => setForm((f) => ({ ...f, validFrom: v }))}
          />
          <Field
            label="Valid to"
            type="date"
            required
            value={form.validTo}
            onChange={(v) => setForm((f) => ({ ...f, validTo: v }))}
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
