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
import {
  fetchRoomCharges,
  createRoomCharge,
  updateRoomCharge,
  deleteRoomCharge,
} from "../../services/roomChargeService";
import { formatCurrency } from "../../utils/format";

// Mirrors the backend Occupancy enum.
const OCCUPANCY_OPTIONS = [
  { value: "TWIN", label: "Twin sharing (2 per room)" },
  { value: "SINGLE", label: "Single occupancy (private room)" },
  { value: "TRIPLE", label: "Triple occupancy (3rd bed)" },
  { value: "EXTRA_BED", label: "Extra bed (additional adult)" },
];

const OCCUPANCY_LABELS = Object.fromEntries(OCCUPANCY_OPTIONS.map((o) => [o.value, o.label]));

const EMPTY_FORM = {
  occupancy: "SINGLE",
  charge: "",
  description: "",
  active: true,
};

/**
 * Room-sharing charges per tour (BRD 3.7 room options).
 *
 * The amount is a SUPPLEMENT added on top of the per-person fare, so twin
 * sharing is normally 0 and single occupancy carries the extra cost.
 */
export default function AdminRoomChargesPage() {
  const [tours, setTours] = useState([]);
  const [selectedTourId, setSelectedTourId] = useState("");
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: () => (selectedTourId ? fetchRoomCharges(selectedTourId, true) : Promise.resolve([])),
      create: (payload) => createRoomCharge(selectedTourId, payload),
      update: (id, payload) => updateRoomCharge(id, payload),
      remove: deleteRoomCharge,
    }),
    [selectedTourId]
  );

  const { items: charges, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Room charge",
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

  function openEdit(charge) {
    setForm({
      occupancy: charge.occupancy,
      charge: charge.charge ?? "",
      description: charge.description || "",
      active: charge.active !== false,
    });
    setEditing(charge.roomChargeId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      occupancy: form.occupancy,
      charge: Number(form.charge || 0),
      description: form.description?.trim() || null,
      active: form.active,
    };

    const ok =
      editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(charge) {
    if (!window.confirm(`Delete the ${OCCUPANCY_LABELS[charge.occupancy]} charge?`)) return;
    await removeItem(charge.roomChargeId);
  }

  // Occupancy is part of the row's identity, so it can't change on edit -
  // options already configured are hidden when creating a new one.
  const usedOccupancies = new Set(charges.map((c) => c.occupancy));
  const availableOccupancies = OCCUPANCY_OPTIONS.filter((o) => !usedOccupancies.has(o.value));

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Room Charges</h1>
          <p className="mt-1 text-sm text-ink-500">
            Extra cost per person for each room-sharing option. Added on top of the tour fare.
          </p>
        </div>
        <Button
          icon={Plus}
          onClick={openCreate}
          disabled={!selectedTourId || availableOccupancies.length === 0}
        >
          Add charge
        </Button>
      </div>

      <div className="mt-6">
        <TourPicker tours={tours} selectedTourId={selectedTourId} onChange={setSelectedTourId} />
      </div>

      {selectedTourId && availableOccupancies.length === 0 && (
        <p className="mt-3 text-xs text-ink-400">
          All four room options are configured for this tour. Edit an existing row to change its price.
        </p>
      )}

      <div className="mt-6">
        <AdminTable
          isLoading={status === "loading"}
          rows={charges}
          rowKey="roomChargeId"
          searchKeys={["occupancy", "description"]}
          searchPlaceholder="Search room options..."
          emptyTitle="No room charges yet"
          emptyDescription="Without charges, room options are priced from the tour cost sheet instead."
          columns={[
            {
              key: "occupancy",
              label: "Room option",
              render: (c) => (
                <div>
                  <span className="font-medium text-ink-900">
                    {OCCUPANCY_LABELS[c.occupancy] || c.occupancy}
                  </span>
                  {c.description && <span className="block text-xs text-ink-400">{c.description}</span>}
                </div>
              ),
            },
            {
              key: "charge",
              label: "Supplement",
              render: (c) =>
                Number(c.charge) > 0 ? (
                  <span className="font-semibold text-ink-900">+ {formatCurrency(c.charge)}</span>
                ) : (
                  <span className="text-ink-400">No extra charge</span>
                ),
            },
            {
              key: "active",
              label: "Status",
              render: (c) => (
                <Badge variant={c.active ? "success" : "info"}>{c.active ? "Active" : "Hidden"}</Badge>
              ),
            },
          ]}
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal
        open={Boolean(editing)}
        title={editing === "new" ? "Add room charge" : "Edit room charge"}
        onClose={() => setEditing(null)}
      >
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Field
            label="Room option"
            type="select"
            required
            value={form.occupancy}
            onChange={(v) => setForm((f) => ({ ...f, occupancy: v }))}
            options={
              editing === "new"
                ? availableOccupancies
                : OCCUPANCY_OPTIONS.filter((o) => o.value === form.occupancy)
            }
            disabled={editing !== "new"}
            hint={
              editing !== "new"
                ? "The room option can't be changed - delete and re-add instead."
                : undefined
            }
          />

          <Field
            label="Supplement per person (₹)"
            type="number"
            required
            min={0}
            step="0.01"
            value={form.charge}
            onChange={(v) => setForm((f) => ({ ...f, charge: v }))}
            hint="Extra amount on top of the tour fare. Enter 0 for no extra charge."
          />

          <Field
            label="Description"
            value={form.description}
            onChange={(v) => setForm((f) => ({ ...f, description: v }))}
            hint="Optional. Shown to the customer, e.g. 'Private room, sea facing'."
          />

          <Field
            label="Active"
            type="checkbox"
            value={form.active}
            onChange={(v) => setForm((f) => ({ ...f, active: v }))}
            hint="Hidden options can't be selected during booking."
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
