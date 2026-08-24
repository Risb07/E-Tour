import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Package, Clock } from "lucide-react";
import { fetchSectorById, fetchSubSectorById, fetchProductsForSubSector } from "../services/sectorService";
import { formatCurrency } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import { useToast } from "../hooks/useToast";
import Breadcrumb from "../components/ui/Breadcrumb";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";
import ErrorState from "../components/common/ErrorState";
import { resolveMediaUrl } from "../utils/media";

/**
 * BRD 3.4 - Product page: products of the chosen sub-sector. Clicking a
 * product that's linked to a real bookable Tour goes to the Tour page;
 * unlinked products (this marketing catalog predates the booking model and
 * an admin hasn't connected them yet) show a "coming soon" state instead.
 */
export default function ProductPage() {
  const { sectorId, subSectorId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [sector, setSector] = useState(null);
  const [subSector, setSubSector] = useState(null);
  const [products, setProducts] = useState([]);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");
    Promise.all([fetchSectorById(sectorId), fetchSubSectorById(subSectorId), fetchProductsForSubSector(subSectorId)])
      .then(([sectorRes, subSectorRes, productsRes]) => {
        if (cancelled) return;
        setSector(sectorRes);
        setSubSector(subSectorRes);
        setProducts(productsRes);
        setStatus("succeeded");
      })
      .catch(() => {
        if (!cancelled) setStatus("failed");
      });
    return () => {
      cancelled = true;
    };
  }, [sectorId, subSectorId]);

  function handleSelect(product) {
    if (product.tour?.tourId) {
      navigate(ROUTE_PATHS.tourDetails(product.tour.tourId));
    } else {
      showToast("This tour isn't bookable online yet - check back soon.", "info");
    }
  }

  if (status === "loading") return <Loader label="Loading products..." />;
  if (status === "failed") return <ErrorState variant="server" />;

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <Breadcrumb
        items={[
          { label: "Tours", to: ROUTE_PATHS.HOME },
          { label: sector?.name || "Sector", to: ROUTE_PATHS.sectorSubSectors(sectorId) },
          { label: subSector?.name || "Sub-sector" },
        ]}
      />
      <h1 className="mt-3 font-display text-2xl font-bold text-ink-900 sm:text-3xl">{subSector?.name}</h1>

      {products.length === 0 ? (
        <EmptyState icon={Package} title="No products yet" description="Nothing has been added to this sub-sector yet." />
      ) : (
        <div className="mt-8 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {products.map((product) => (
            <button
              key={product.productId}
              onClick={() => handleSelect(product)}
              className="group flex flex-col overflow-hidden rounded-card bg-white text-left shadow-soft transition-all hover:-translate-y-1 hover:shadow-lifted"
            >
              <div className="aspect-[4/3] w-full overflow-hidden bg-ink-100">
                {product.imageUrl ? (
                  <img
                    src={resolveMediaUrl(product.imageUrl)}
                    alt={product.name}
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-ink-300">
                    <Package className="h-10 w-10" />
                  </div>
                )}
              </div>
              <div className="flex flex-1 flex-col gap-1.5 p-4">
                <h3 className="font-semibold text-ink-900">{product.name}</h3>
                <span className="flex items-center gap-1.5 text-xs text-ink-500">
                  <Clock className="h-3.5 w-3.5" />
                  {product.durationNights}N/{product.durationDays}D
                </span>
                <div className="mt-auto flex items-center justify-between pt-2">
                  <span className="font-display text-lg font-bold text-ink-900">
                    {formatCurrency(product.baseCost)}
                  </span>
                  {!product.tour?.tourId && (
                    <span className="rounded-pill bg-ink-100 px-2.5 py-1 text-[11px] font-semibold text-ink-500">
                      Coming soon
                    </span>
                  )}
                </div>
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
