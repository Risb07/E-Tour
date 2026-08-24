import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Compass } from "lucide-react";
import { fetchSectorById, fetchSubSectorsForSector } from "../services/sectorService";
import { ROUTE_PATHS } from "../constants/routes";
import Breadcrumb from "../components/ui/Breadcrumb";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";
import ErrorState from "../components/common/ErrorState";
import { resolveMediaUrl } from "../utils/media";

/** BRD 3.3 - Sub-Sector page: icons for every sub-sector of the chosen sector. */
export default function SubSectorPage() {
  const { sectorId } = useParams();
  const navigate = useNavigate();
  const [sector, setSector] = useState(null);
  const [subSectors, setSubSectors] = useState([]);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");
    Promise.all([fetchSectorById(sectorId), fetchSubSectorsForSector(sectorId)])
      .then(([sectorRes, subSectorsRes]) => {
        if (cancelled) return;
        setSector(sectorRes);
        setSubSectors(subSectorsRes);
        setStatus("succeeded");
      })
      .catch(() => {
        if (!cancelled) setStatus("failed");
      });
    return () => {
      cancelled = true;
    };
  }, [sectorId]);

  if (status === "loading") return <Loader label="Loading sectors..." />;
  if (status === "failed") return <ErrorState variant="server" />;

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <Breadcrumb items={[{ label: "Tours", to: ROUTE_PATHS.HOME }, { label: sector?.name || "Sector" }]} />
      <h1 className="mt-3 font-display text-2xl font-bold text-ink-900 sm:text-3xl">{sector?.name}</h1>
      {sector?.description && <p className="mt-1 max-w-2xl text-sm text-ink-500">{sector.description}</p>}

      {subSectors.length === 0 ? (
        <EmptyState
          icon={Compass}
          title="No sub-sectors yet"
          description="This sector doesn't have any sub-sectors configured yet."
        />
      ) : (
        <div className="mt-8 grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-5">
          {subSectors.map((sub) => (
            <button
              key={sub.subSectorId}
              onClick={() => navigate(ROUTE_PATHS.subSectorProducts(sectorId, sub.subSectorId))}
              className="group flex flex-col items-center gap-3 rounded-card bg-white p-5 text-center shadow-soft transition-all hover:-translate-y-1 hover:shadow-lifted"
            >
              <div className="flex h-16 w-16 items-center justify-center overflow-hidden rounded-full bg-amber-50">
                {sub.iconUrl ? (
                  <img src={resolveMediaUrl(sub.iconUrl)} alt="" className="h-full w-full object-cover" />
                ) : (
                  <Compass className="h-7 w-7 text-amber-500" />
                )}
              </div>
              <span className="text-sm font-semibold text-ink-900">{sub.name}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
