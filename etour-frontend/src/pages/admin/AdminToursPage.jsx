import { useEffect, useMemo, useState } from "react";
import { Plus, AlertTriangle } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchAllTours,
  createTour,
  updateTour,
  deleteTour,
} from "../../services/tourService";
import { fetchCategories } from "../../services/categoryService";
import { TOUR_CODE_LABELS, TOUR_STATUS_LABELS } from "../../constants/enums";

const EMPTY_FORM = {
  title: "",
  description: "",
  durationDays: "",
  basePrice: "",
  tourCode: "",
  status: "DRAFT",
  categoryIds: [],
};

export default function AdminToursPage() {
  const [categories, setCategories] = useState([]);
  const [editing, setEditing] = useState(null); // null = closed, "new" = create, number = edit id
  const [form, setForm] = useState(EMPTY_FORM);
  const [isLoadingCategories, setIsLoadingCategories] = useState(false);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: fetchAllTours,
      create: createTour,
      update: updateTour,
      remove: deleteTour,
    }),
    []
  );
  const { items: tours, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Tour",
  });

  useEffect(() => {
    setIsLoadingCategories(true);
    fetchCategories()
      .then(setCategories)
      .catch(() => showToast("Couldn't load categories.", "error"))
      .finally(() => setIsLoadingCategories(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(tour) {
    setForm({
      title: tour.title || "",
      description: tour.description || "",
      durationDays: tour.durationDays ?? "",
      basePrice: tour.basePrice ?? "",
      tourCode: tour.tourCode || "",
      status: tour.status || "DRAFT",
      categoryIds: (tour.categories || []).map((c) => String(c.categoryId)),
    });
    setEditing(tour.tourId);
  }

  function toggleCategory(categoryId) {
    setForm((f) => ({
      ...f,
      categoryIds: f.categoryIds.includes(categoryId)
        ? f.categoryIds.filter((id) => id !== categoryId)
        : [...f.categoryIds, categoryId],
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      title: form.title,
      description: form.description,
      durationDays: Number(form.durationDays),
      basePrice: Number(form.basePrice),
      tourCode: form.tourCode,
      status: form.status,
      categories: form.categoryIds.map((id) => ({ categoryId: Number(id) })),
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(tour) {
    if (!window.confirm(`Delete tour "${tour.title}"? This also removes its schedules and costs.`)) return;
    await removeItem(tour.tourId);
  }

  const hiddenCount = tours.filter((t) => t.status !== "ACTIVE").length;

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Tours</h1>
          <p className="mt-1 text-sm text-ink-500">Manage the bookable tour catalog.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Tour
        </Button>
      </div>

      {/* Only ACTIVE tours are returned by search, so a tour sitting in DRAFT
          is assigned to its categories but invisible on the public site. That
          is the single most common cause of "my category page is empty". */}
      {hiddenCount > 0 && (
        <div className="mt-6 flex items-start gap-3 rounded-card border border-amber-200 bg-amber-50 p-4">
          <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0 text-amber-600" aria-hidden="true" />
          <div className="text-sm">
            <p className="font-semibold text-ink-900">
              {hiddenCount} tour{hiddenCount === 1 ? " is" : "s are"} hidden from customers
            </p>
            <p className="mt-0.5 text-ink-600">
              Only tours with status <strong>Active</strong> appear in search results and category
              pages. Edit a tour and set its status to Active to publish it.
            </p>
          </div>
        </div>
      )}

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "tourId", label: "ID", className: "w-16" },
            { key: "title", label: "Title" },
            {
              key: "tourCode",
              label: "Code",
              render: (t) => <Badge variant="accent">{t.tourCode}</Badge>,
            },
            { key: "durationDays", label: "Days" },
            {
              key: "basePrice",
              label: "Base Price",
              render: (t) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(t.basePrice),
            },
            {
              key: "status",
              label: "Status",
              render: (t) => {
                const meta = TOUR_STATUS_LABELS[t.status] || TOUR_STATUS_LABELS.DRAFT;
                const isPublic = t.status === "ACTIVE";
                return (
                  <div>
                    <Badge variant={meta.variant}>{meta.label}</Badge>
                    {/* Only ACTIVE tours appear in search and category
                        listings. Saying so here prevents the "my tour is in a
                        category but the page is empty" confusion. */}
                    {!isPublic && (
                      <span className="mt-1 block text-[11px] text-ink-400">Hidden from customers</span>
                    )}
                  </div>
                );
              },
            },
          ]}
          rows={tours}
          rowKey="tourId"
          searchKeys={["title", "tourCode"]}
          searchPlaceholder="Search tours..."
          isLoading={status === "loading"}
          emptyTitle="No tours yet"
          emptyDescription="Add your first tour to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add tour" : "Edit tour"} onClose={() => setEditing(null)} wide>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Title"
            required
            value={form.title}
            onChange={(v) => setForm((f) => ({ ...f, title: v }))}
          />
          <Field
            label="Tour code"
            type="select"
            required
            options={Object.entries(TOUR_CODE_LABELS).map(([value, label]) => ({ value, label }))}
            value={form.tourCode}
            onChange={(v) => setForm((f) => ({ ...f, tourCode: v }))}
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
            label="Base price (₹)"
            type="number"
            required
            min={0}
            step="0.01"
            value={form.basePrice}
            onChange={(v) => setForm((f) => ({ ...f, basePrice: v }))}
          />
          <Field
            label="Status"
            type="select"
            options={["ACTIVE", "INACTIVE", "DRAFT"]}
            value={form.status}
            onChange={(v) => setForm((f) => ({ ...f, status: v }))}
          />
          <div className="flex flex-col gap-1.5 sm:col-span-2">
            <span className="text-sm font-medium text-ink-700">
              Categories {!isLoadingCategories && <span className="font-normal text-ink-400">({form.categoryIds.length} selected)</span>}
            </span>
            {isLoadingCategories ? (
              <p className="text-sm text-ink-400">Loading categories...</p>
            ) : categories.length === 0 ? (
              <p className="text-sm text-ink-400">No categories available yet.</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {categories.map((c) => {
                  const checked = form.categoryIds.includes(String(c.categoryId));
                  return (
                    <button
                      key={c.categoryId}
                      type="button"
                      onClick={() => toggleCategory(String(c.categoryId))}
                      className={`rounded-pill border px-3 py-1.5 text-xs font-semibold transition-colors ${
                        checked
                          ? "border-amber-500 bg-amber-50 text-amber-700"
                          : "border-ink-200 bg-white text-ink-600 hover:border-amber-400"
                      }`}
                    >
                      {c.categoryName}
                    </button>
                  );
                })}
              </div>
            )}
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
