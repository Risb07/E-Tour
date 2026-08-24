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
  fetchCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from "../../services/categoryService";

const EMPTY_FORM = {
  categoryName: "",
  description: "",
  imageUrl: "",
  categoryCode: "",
  isFeatured: "N",
  status: true,
  parentCategoryId: "",
};

export default function AdminCategoriesPage() {
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const crud = useMemo(
    () => ({
      fetchAll: fetchCategories,
      create: createCategory,
      update: updateCategory,
      remove: deleteCategory,
    }),
    []
  );
  const { items: categories, status, isMutating, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Category",
  });

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(category) {
    setForm({
      categoryName: category.categoryName || "",
      description: category.description || "",
      imageUrl: category.imageUrl || "",
      categoryCode: category.categoryCode || "",
      isFeatured: category.isFeatured || "N",
      status: Boolean(category.status),
      parentCategoryId: category.parentCategory?.categoryId ? String(category.parentCategory.categoryId) : "",
    });
    setEditing(category.categoryId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      categoryName: form.categoryName,
      description: form.description,
      imageUrl: form.imageUrl,
      categoryCode: form.categoryCode,
      isFeatured: form.isFeatured,
      status: form.status,
      parentCategoryId: form.parentCategoryId ? Number(form.parentCategoryId) : null,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) setEditing(null);
  }

  async function handleDelete(category) {
    if (!window.confirm(`Delete category "${category.categoryName}"?`)) return;
    await removeItem(category.categoryId);
  }

  const parentOptions = categories
    .filter((c) => c.categoryId !== editing || editing === "new")
    .map((c) => ({ value: String(c.categoryId), label: c.categoryName }));

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Categories</h1>
          <p className="mt-1 text-sm text-ink-500">Manage tour categories and their hierarchy.</p>
        </div>
        <Button icon={Plus} onClick={openCreate}>
          Add Category
        </Button>
      </div>

      <div className="mt-6">
        <AdminTable
          columns={[
            { key: "categoryId", label: "ID", className: "w-16" },
            { key: "categoryName", label: "Name" },
            {
              key: "parentCategory",
              label: "Parent",
              render: (c) => c.parentCategory?.categoryName || "—",
            },
            {
              key: "categoryCode",
              label: "Code",
              render: (c) => <Badge variant="accent">{c.categoryCode}</Badge>,
            },
            {
              key: "isFeatured",
              label: "Featured",
              render: (c) => (c.isFeatured === "Y" ? <Badge variant="success">Y</Badge> : <Badge variant="info">N</Badge>),
            },
            {
              key: "status",
              label: "Status",
              render: (c) =>
                c.status ? <Badge variant="success">Active</Badge> : <Badge variant="danger">Inactive</Badge>,
            },
          ]}
          rows={categories}
          rowKey="categoryId"
          searchKeys={["categoryName", "categoryCode"]}
          searchPlaceholder="Search categories..."
          isLoading={status === "loading"}
          emptyTitle="No categories yet"
          emptyDescription="Add your first category to get started."
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <AdminModal open={Boolean(editing)} title={editing === "new" ? "Add category" : "Edit category"} onClose={() => setEditing(null)}>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field
            label="Category name"
            required
            value={form.categoryName}
            onChange={(v) => setForm((f) => ({ ...f, categoryName: v }))}
          />
          <Field
            label="Parent category"
            type="select"
            options={parentOptions}
            value={form.parentCategoryId}
            onChange={(v) => setForm((f) => ({ ...f, parentCategoryId: v }))}
            placeholder="None (top level)"
          />
          <Field
            label="Category code"
            type="select"
            required
            options={[
              { value: "DOM", label: "DOM - Domestic" },
              { value: "ADV", label: "ADV - Adventure" },
              { value: "INT", label: "INT - International" },
            ]}
            value={form.categoryCode}
            onChange={(v) => setForm((f) => ({ ...f, categoryCode: v }))}
          />
          <Field
            label="Featured"
            type="select"
            required
            options={[
              { value: "Y", label: "Y - Featured" },
              { value: "N", label: "N - Not featured" },
            ]}
            value={form.isFeatured}
            onChange={(v) => setForm((f) => ({ ...f, isFeatured: v }))}
          />
          <Field
            label="Active"
            type="checkbox"
            value={form.status}
            onChange={(v) => setForm((f) => ({ ...f, status: v }))}
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
