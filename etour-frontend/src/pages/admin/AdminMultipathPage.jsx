import { useEffect, useMemo, useState } from "react";
import { Plus, Eye, Play, Link2, AlertTriangle, CheckCircle2 } from "lucide-react";
import Button from "../../components/common/Button";
import Field from "../../components/admin/Field";
import AdminModal from "../../components/admin/AdminModal";
import AdminTable from "../../components/admin/AdminTable";
import Badge from "../../components/ui/Badge";
import { useToast } from "../../hooks/useToast";
import { useAdminCrud } from "../../hooks/useAdminCrud";
import {
  fetchMultipathRules,
  createMultipathRule,
  updateMultipathRule,
  deleteMultipathRule,
  previewMultipath,
  applyMultipath,
} from "../../services/multipathRuleService";
import { fetchCategories } from "../../services/categoryService";
import { fetchAllSubSectors } from "../../services/sectorService";

const MATCH_FIELDS = [
  { value: "TOUR_CODE", label: "Tour code (ADV / INT / DEV / DOM)" },
  { value: "TITLE", label: "Tour title" },
  { value: "BASE_PRICE", label: "Base price" },
  { value: "DURATION_DAYS", label: "Duration in days" },
  { value: "ALL", label: "All active tours" },
];

const OPERATORS = [
  { value: "EQUALS", label: "equals" },
  { value: "CONTAINS", label: "contains" },
  { value: "GREATER_THAN", label: "is greater than" },
  { value: "LESS_THAN", label: "is less than" },
  { value: "BETWEEN", label: "is between" },
  { value: "ANY", label: "any (matches everything)" },
];

// Which operators make sense for which field - offering "contains" on a price
// only invites rules that silently never match.
const OPERATORS_FOR = {
  TOUR_CODE: ["EQUALS", "CONTAINS"],
  TITLE: ["CONTAINS", "EQUALS"],
  BASE_PRICE: ["GREATER_THAN", "LESS_THAN", "BETWEEN", "EQUALS"],
  DURATION_DAYS: ["LESS_THAN", "GREATER_THAN", "BETWEEN", "EQUALS"],
  ALL: ["ANY"],
};

const EMPTY_FORM = {
  name: "",
  matchField: "TOUR_CODE",
  matchOperator: "EQUALS",
  matchValue: "",
  matchValueTo: "",
  targetCategoryId: "",
  targetSubSectorId: "",
  priority: 0,
  active: true,
};

/**
 * Rule-driven "multipath" generation.
 *
 * A tour is multipath when it is reachable by several routes. Rather than
 * ticking categories on every tour by hand, rules describe the intent once
 * ("every INT tour belongs under International") and the generator applies it
 * to every matching tour - including ones added later.
 */
export default function AdminMultipathPage() {
  const [categories, setCategories] = useState([]);
  const [subSectors, setSubSectors] = useState([]);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [preview, setPreview] = useState(null);
  const [isRunning, setIsRunning] = useState(false);
  const { showToast } = useToast();

  const crud = useMemo(
    () => ({
      fetchAll: fetchMultipathRules,
      create: createMultipathRule,
      update: updateMultipathRule,
      remove: deleteMultipathRule,
    }),
    []
  );

  const { items: rules, status, isMutating, load, createItem, updateItem, removeItem } = useAdminCrud({
    ...crud,
    noun: "Rule",
  });

  useEffect(() => {
    fetchCategories()
      .then(setCategories)
      .catch(() => showToast("Couldn't load categories.", "error"));
    fetchAllSubSectors()
      .then(setSubSectors)
      .catch(() => setSubSectors([]));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setForm(EMPTY_FORM);
    setEditing("new");
  }

  function openEdit(rule) {
    setForm({
      name: rule.name || "",
      matchField: rule.matchField || "TOUR_CODE",
      matchOperator: rule.matchOperator || "EQUALS",
      matchValue: rule.matchValue ?? "",
      matchValueTo: rule.matchValueTo ?? "",
      targetCategoryId: rule.targetCategoryId ? String(rule.targetCategoryId) : "",
      targetSubSectorId: rule.targetSubSectorId ? String(rule.targetSubSectorId) : "",
      priority: rule.priority ?? 0,
      active: rule.active !== false,
    });
    setEditing(rule.ruleId);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      name: form.name.trim(),
      matchField: form.matchField,
      matchOperator: form.matchOperator,
      matchValue: form.matchValue === "" ? null : String(form.matchValue),
      matchValueTo: form.matchValueTo === "" ? null : String(form.matchValueTo),
      targetCategoryId: Number(form.targetCategoryId),
      targetSubSectorId: form.targetSubSectorId ? Number(form.targetSubSectorId) : null,
      priority: Number(form.priority || 0),
      active: form.active,
    };
    const ok = editing === "new" ? await createItem(payload) : await updateItem(editing, payload);
    if (ok) {
      setEditing(null);
      // Any rule change invalidates the preview shown on screen.
      setPreview(null);
    }
  }

  async function handleDelete(rule) {
    if (
      !window.confirm(
        `Delete rule "${rule.name}"?\n\nLinks it already created stay in place — deleting the rule only stops it running again.`
      )
    )
      return;
    await removeItem(rule.ruleId);
    setPreview(null);
  }

  async function handlePreview() {
    setIsRunning(true);
    try {
      const result = await previewMultipath();
      setPreview({ ...result, applied: false });
      showToast(
        result.newCategoryLinks + result.newProducts === 0
          ? "Nothing to change — everything is already linked."
          : `${result.newCategoryLinks} category link(s) and ${result.newProducts} product(s) would be created.`,
        "info"
      );
    } catch (err) {
      showToast(err.message || "Preview failed.", "error");
    } finally {
      setIsRunning(false);
    }
  }

  async function handleApply() {
    if (!window.confirm("Apply all active rules now? Existing links are left untouched.")) return;
    setIsRunning(true);
    try {
      const result = await applyMultipath();
      setPreview({ ...result, applied: true });
      showToast(
        `Applied: ${result.newCategoryLinks} category link(s), ${result.newProducts} product(s) created.`,
        "success"
      );
      load();
    } catch (err) {
      showToast(err.message || "Apply failed.", "error");
    } finally {
      setIsRunning(false);
    }
  }

  const allowedOperators = OPERATORS.filter((o) =>
    (OPERATORS_FOR[form.matchField] || []).includes(o.value)
  );
  const needsValue = form.matchField !== "ALL" && form.matchOperator !== "ANY";
  const needsUpperBound = form.matchOperator === "BETWEEN";

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl font-bold text-ink-900">Multipath Rules</h1>
          <p className="mt-1 max-w-2xl text-sm text-ink-500">
            Automatically place tours on extra navigation paths. Describe the intent once and it
            applies to every matching tour, including ones you add later.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" icon={Eye} onClick={handlePreview} isLoading={isRunning}>
            Preview
          </Button>
          <Button icon={Play} onClick={handleApply} isLoading={isRunning} disabled={rules.length === 0}>
            Apply rules
          </Button>
        </div>
      </div>

      <div className="mt-6 flex items-start gap-3 rounded-card border border-ink-100 bg-white p-4">
        <Link2 className="mt-0.5 h-5 w-5 shrink-0 text-amber-500" aria-hidden="true" />
        <div className="text-sm text-ink-600">
          <p className="font-semibold text-ink-900">One tour, many paths — never a copy</p>
          <p className="mt-0.5">
            Rules create <em>links</em> between an existing tour and a category. The tour itself is
            never duplicated, so its price and availability stay identical on every path. Running
            this twice is safe — links that already exist are skipped.
          </p>
        </div>
      </div>

      <h2 className="mt-8 font-display text-base font-bold text-ink-900">Rules</h2>
      <div className="mt-3">
        <AdminTable
          isLoading={status === "loading"}
          rows={rules}
          rowKey="ruleId"
          searchKeys={["name", "targetCategoryName"]}
          searchPlaceholder="Search rules..."
          emptyTitle="No rules yet"
          emptyDescription="Add a rule, preview what it would do, then apply it."
          columns={[
            { key: "name", label: "Rule" },
            {
              key: "condition",
              label: "When",
              render: (r) => (
                <span className="text-xs text-ink-600">
                  {MATCH_FIELDS.find((f) => f.value === r.matchField)?.label || r.matchField}{" "}
                  <b>{OPERATORS.find((o) => o.value === r.matchOperator)?.label || r.matchOperator}</b>{" "}
                  {r.matchValue}
                  {r.matchValueTo ? ` – ${r.matchValueTo}` : ""}
                </span>
              ),
            },
            {
              key: "target",
              label: "Then add to",
              render: (r) => (
                <div className="text-xs">
                  <Badge variant="info">{r.targetCategoryName}</Badge>
                  {r.targetSubSectorName && (
                    <span className="mt-1 block text-ink-400">+ product in {r.targetSubSectorName}</span>
                  )}
                </div>
              ),
            },
            {
              key: "active",
              label: "Status",
              render: (r) => (
                <Badge variant={r.active ? "success" : "info"}>{r.active ? "Active" : "Paused"}</Badge>
              ),
            },
          ]}
          onEdit={openEdit}
          onDelete={handleDelete}
        />
      </div>

      <div className="mt-4">
        <Button variant="outline" icon={Plus} onClick={openCreate}>
          Add rule
        </Button>
      </div>

      {preview && <PreviewPanel preview={preview} />}

      <AdminModal
        open={Boolean(editing)}
        title={editing === "new" ? "Add rule" : "Edit rule"}
        onClose={() => setEditing(null)}
        wide
      >
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <Field
              label="Rule name"
              required
              value={form.name}
              onChange={(v) => setForm((f) => ({ ...f, name: v }))}
              placeholder="e.g. International tours under Europe"
              hint="Only for your reference in this list."
            />
          </div>

          <Field
            label="When a tour's..."
            type="select"
            required
            value={form.matchField}
            onChange={(v) =>
              setForm((f) => ({
                ...f,
                matchField: v,
                // Keep the operator valid for the newly chosen field.
                matchOperator: (OPERATORS_FOR[v] || ["EQUALS"])[0],
              }))
            }
            options={MATCH_FIELDS}
          />

          <Field
            label="...operator"
            type="select"
            required
            value={form.matchOperator}
            onChange={(v) => setForm((f) => ({ ...f, matchOperator: v }))}
            options={allowedOperators}
          />

          {needsValue && (
            <Field
              label={needsUpperBound ? "From" : "Value"}
              required
              value={form.matchValue}
              onChange={(v) => setForm((f) => ({ ...f, matchValue: v }))}
              placeholder={form.matchField === "TOUR_CODE" ? "INT" : "e.g. 50000"}
            />
          )}
          {needsUpperBound && (
            <Field
              label="To"
              required
              value={form.matchValueTo}
              onChange={(v) => setForm((f) => ({ ...f, matchValueTo: v }))}
            />
          )}

          <Field
            label="Add tour to category"
            type="select"
            required
            value={form.targetCategoryId}
            onChange={(v) => setForm((f) => ({ ...f, targetCategoryId: v }))}
            options={categories.map((c) => ({
              value: String(c.categoryId),
              label: c.categoryName,
            }))}
          />

          <Field
            label="Also publish under sub-sector"
            type="select"
            value={form.targetSubSectorId}
            onChange={(v) => setForm((f) => ({ ...f, targetSubSectorId: v }))}
            options={[
              { value: "", label: "None (category only)" },
              ...subSectors.map((s) => ({ value: String(s.subSectorId), label: s.name })),
            ]}
            hint="Optional. Creates a product so the Sectors path works too."
          />

          <Field
            label="Priority"
            type="number"
            value={form.priority}
            onChange={(v) => setForm((f) => ({ ...f, priority: v }))}
            hint="Lower runs first. Only affects listing order."
          />

          <Field
            label="Active"
            type="checkbox"
            value={form.active}
            onChange={(v) => setForm((f) => ({ ...f, active: v }))}
            hint="Paused rules are skipped when applying."
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

function PreviewPanel({ preview }) {
  const pending = preview.changes?.filter((c) => !c.alreadyLinked) || [];
  const existing = (preview.changes?.length || 0) - pending.length;

  return (
    <div className="mt-8 rounded-card bg-white p-6 shadow-card">
      <div className="flex items-center gap-2">
        {preview.applied ? (
          <CheckCircle2 className="h-5 w-5 text-emerald-600" aria-hidden="true" />
        ) : (
          <Eye className="h-5 w-5 text-amber-600" aria-hidden="true" />
        )}
        <h2 className="font-display text-base font-bold text-ink-900">
          {preview.applied ? "Applied" : "Preview (nothing has been changed)"}
        </h2>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Stat label="Rules run" value={preview.rulesEvaluated} />
        <Stat label="Tours checked" value={preview.toursEvaluated} />
        <Stat
          label={preview.applied ? "Links created" : "Links to create"}
          value={preview.newCategoryLinks}
          accent
        />
        <Stat
          label={preview.applied ? "Products created" : "Products to create"}
          value={preview.newProducts}
          accent
        />
      </div>

      {preview.rulesWithNoMatches?.length > 0 && (
        <div className="mt-4 flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 p-3">
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" aria-hidden="true" />
          <p className="text-xs text-ink-700">
            <b>These rules matched no tours:</b> {preview.rulesWithNoMatches.join(", ")}. Usually the
            value is wrong, or the matching tours aren&apos;t Active yet.
          </p>
        </div>
      )}

      {pending.length > 0 && (
        <>
          <h3 className="mt-5 text-xs font-bold uppercase tracking-wide text-ink-400">
            {preview.applied ? "Changes made" : "Changes that would be made"}
          </h3>
          <div className="mt-2 max-h-72 overflow-y-auto rounded-xl border border-ink-100">
            <table className="w-full text-left text-xs">
              <thead className="sticky top-0 bg-ink-50 text-ink-500">
                <tr>
                  <th className="px-3 py-2">Tour</th>
                  <th className="px-3 py-2">Gains</th>
                  <th className="px-3 py-2">Via rule</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {pending.map((c, i) => (
                  <tr key={i}>
                    <td className="px-3 py-2 font-medium text-ink-900">{c.tourTitle}</td>
                    <td className="px-3 py-2">
                      <Badge variant={c.linkType === "PRODUCT" ? "accent" : "info"}>
                        {c.linkType === "PRODUCT" ? "Product" : "Category"}
                      </Badge>{" "}
                      <span className="text-ink-600">{c.targetName}</span>
                    </td>
                    <td className="px-3 py-2 text-ink-400">{c.ruleName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {existing > 0 && (
        <p className="mt-3 text-xs text-ink-400">
          {existing} link{existing === 1 ? "" : "s"} already existed and {preview.applied ? "were" : "would be"} skipped.
        </p>
      )}

      {pending.length === 0 && (
        <p className="mt-4 text-sm text-ink-500">
          Everything your rules describe is already linked — nothing to do.
        </p>
      )}
    </div>
  );
}

function Stat({ label, value, accent = false }) {
  return (
    <div className="rounded-xl bg-ink-50 p-3">
      <p className={`font-display text-xl font-bold ${accent ? "text-amber-600" : "text-ink-900"}`}>
        {value}
      </p>
      <p className="text-[11px] text-ink-500">{label}</p>
    </div>
  );
}
