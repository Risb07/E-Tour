import { formatCurrency } from "../../utils/format";
import Badge from "../ui/Badge";

/**
 * BRD 3.5 "Cost" tab. Renders the active TourCost sheet as a pricing table.
 *
 * The backend TourCost entity stores basePrice / singlePersonCost /
 * extraPersonCost / childWithBedCost / childWithoutBedCost. Triple occupancy
 * is not a separate stored column - it is twin sharing plus one extra person,
 * which is exactly what extraPersonCost represents, so it's shown as a
 * derived row rather than inventing a field the admin can't actually set.
 */
export default function TourCostTable({ costs, gstRate = 0.05 }) {
  if (!costs || costs.length === 0) {
    return <p className="py-10 text-center text-sm text-ink-400">Detailed pricing for this tour is coming soon.</p>;
  }

  // Costs arrive newest-first from the API; the first active sheet is current.
  const cost = costs.find((c) => c.status === 1) || costs[0];

  const base = Number(cost.basePrice ?? 0);
  const single = cost.singlePersonCost != null ? Number(cost.singlePersonCost) : null;
  const extra = cost.extraPersonCost != null ? Number(cost.extraPersonCost) : null;
  const childWithBed = cost.childWithBedCost != null ? Number(cost.childWithBedCost) : null;
  const childNoBed = cost.childWithoutBedCost != null ? Number(cost.childWithoutBedCost) : null;

  const rows = [
    { label: "Twin sharing", sublabel: "Per person, 2 to a room", amount: base },
    single != null && {
      label: "Single occupancy",
      sublabel: "Per person, room to yourself",
      amount: single,
      highlight: single > base,
    },
    extra != null && {
      label: "Triple occupancy",
      sublabel: "Twin sharing + 1 extra bed, per extra person",
      amount: extra,
    },
    extra != null && { label: "Extra person", sublabel: "Additional adult sharing a room", amount: extra },
    childWithBed != null && { label: "Child (with bed)", sublabel: "Age 2-12, own bed", amount: childWithBed },
    childNoBed != null && { label: "Child (no bed)", sublabel: "Age 2-12, shares a bed", amount: childNoBed },
    { label: "Infant", sublabel: "Under 2 years", amount: 0 },
  ].filter(Boolean);

  // Worked example on the standard twin-sharing fare so the GST and final
  // payable amount are concrete rather than abstract percentages.
  const discount = 0;
  const taxable = base - discount;
  const gst = taxable * gstRate;
  const finalPrice = taxable + gst;

  return (
    <div className="flex flex-col gap-6">
      <div className="overflow-x-auto rounded-card bg-white shadow-soft">
        <table className="w-full text-left text-sm">
          <caption className="sr-only">Tour pricing by occupancy type</caption>
          <thead className="bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
            <tr>
              <th scope="col" className="px-4 py-3">Occupancy</th>
              <th scope="col" className="px-4 py-3 text-right">Price per person</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100">
            {rows.map((row) => (
              <tr key={row.label} className="hover:bg-ink-50">
                <td className="px-4 py-3">
                  <span className="font-medium text-ink-900">{row.label}</span>
                  <span className="block text-xs text-ink-400">{row.sublabel}</span>
                </td>
                <td className="px-4 py-3 text-right font-semibold text-ink-900">
                  {row.amount === 0 ? <Badge variant="success">Free</Badge> : formatCurrency(row.amount)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="rounded-card bg-white p-5 shadow-soft">
        <h4 className="text-xs font-bold uppercase tracking-wide text-ink-400">
          Example total &middot; one adult, twin sharing
        </h4>
        <dl className="mt-3 flex flex-col gap-2 text-sm">
          <Row label="Tour fare" value={formatCurrency(taxable)} />
          {discount > 0 && <Row label="Discount" value={`- ${formatCurrency(discount)}`} accent />}
          <Row label={`GST (${(gstRate * 100).toFixed(0)}%)`} value={formatCurrency(gst)} />
          <div className="mt-1 flex items-center justify-between border-t border-ink-100 pt-3">
            <dt className="font-semibold text-ink-900">Final price</dt>
            <dd className="font-display text-lg font-bold text-ink-900">{formatCurrency(finalPrice)}</dd>
          </div>
        </dl>
        <p className="mt-3 text-xs text-ink-400">
          Your actual total depends on how many travellers you book and their ages, and is confirmed
          before payment.
        </p>
      </div>

      {(cost.validFrom || cost.validTo) && (
        <p className="text-xs text-ink-400">
          Pricing valid {cost.validFrom ? `from ${cost.validFrom}` : ""}
          {cost.validTo ? ` until ${cost.validTo}` : ""}.
        </p>
      )}
    </div>
  );
}

function Row({ label, value, accent = false }) {
  return (
    <div className="flex items-center justify-between">
      <dt className="text-ink-500">{label}</dt>
      <dd className={accent ? "font-medium text-emerald-600" : "text-ink-900"}>{value}</dd>
    </div>
  );
}
